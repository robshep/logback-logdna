package net._95point2.utils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.goebl.david.Response;
import com.goebl.david.Webb;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class LogDNAAppender extends UnsynchronizedAppenderBase<ILoggingEvent>
{
	/** relying on the guard in the parent implementation to prevent recursive calls, hoping there is another ERROR destination */
	Logger emergencyLog = LoggerFactory.getLogger(LogDNAAppender.class);
	
	private String hostname;
	private Webb http;
	
	/*
	 * configurables ( plus ingestKey )
	 */
	private String appName;
	private boolean includeStacktrace = true;
	private boolean sendMDC = true;
	private String tags = "";
	
	public LogDNAAppender() {
		try {
			this.hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			this.hostname = "localhost";
		}
		
		Webb webb = Webb.create();
		webb.setBaseUri("https://logs.logdna.com/logs/ingest");
		webb.setDefaultHeader(Webb.HDR_USER_AGENT, "LogDNA Appender (95point2)");
		this.http = webb;
	}

	@Override
	protected void append(ILoggingEvent ev) 
	{
		/* not interested in consuming our own filth */
		if(ev.getLoggerName().equals(LogDNAAppender.class.getName())){
			return;
		}
		
		StringBuilder sb = new StringBuilder()
				.append("[").append(ev.getThreadName()).append("] ")
				.append(ev.getLoggerName())
				.append(" -- ")
				.append(ev.getFormattedMessage());
		
		if(ev.getThrowableProxy() != null && this.includeStacktrace){
			IThrowableProxy tp = ev.getThrowableProxy();
			sb.append("\n\n").append(tp.getClassName()).append(": ").append(tp.getMessage());
			for(StackTraceElementProxy ste : tp.getStackTraceElementProxyArray()){
				sb.append("\n\t").append(ste.getSTEAsString());
			}
		}
		
		try
		{
			JSONObject payload = new JSONObject();
			JSONArray lines = new JSONArray();
			payload.put("lines", lines );
			
			JSONObject line = new JSONObject();
			line.put("timestamp", ev.getTimeStamp());
			line.put("level", ev.getLevel().toString());
			line.put("app", this.appName);
			line.put("line", sb.toString());
			
			JSONObject meta = new JSONObject();
			meta.put("logger", ev.getLoggerName());
			line.put("meta", meta);
			
			if(this.sendMDC && !ev.getMDCPropertyMap().isEmpty()){
				for(Entry<String,String> entry : ev.getMDCPropertyMap().entrySet()){
					meta.put(entry.getKey(), entry.getValue());
				}
			}


			lines.put(line);

			StringBuilder path = new StringBuilder();
			path.append("?hostname=").append(encode(this.hostname))
					.append("&now=").append(encode(String.valueOf(System.currentTimeMillis())));

			if (tags != null) {
				path.append("&tags=").append(tags);
			}

			Response<JSONObject> response = http.post(path.toString())
					.body(payload)
					.retry(3, true)
					.asJsonObject();
			
			if(!response.isSuccess()){
				String msg = "Error posting to LogDNA ";
				msg += response.getStatusCode() + " ";
				try{ msg += response.getStatusLine(); }
				catch(Exception e){}
				emergencyLog.error(msg);
			}
		}
		catch (JSONException e) {
			emergencyLog.error(e.getMessage(), e);
		}
	}
	
	
	
	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setIngestKey(String ingestKey) {
		this.http.setDefaultHeader("apikey", ingestKey);
	}
	
	public void setSendMDC(boolean sendMDC) {
		this.sendMDC = sendMDC;
	}

	public void setIncludeStacktrace(boolean includeStacktrace) {
		this.includeStacktrace = includeStacktrace;
	}

	public void setTags(String tags) {
		this.tags = encode(tags);
	}

	private static String encode(String str) {
        try 
        {
            return URLEncoder.encode(str, "UTF-8");
        } 
        catch (UnsupportedEncodingException e) 
        {
            return str;
        }
    }
}
