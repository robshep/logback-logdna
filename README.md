# LogDNA Appender for Logback

LogDNA is a hosted logging platform: https://logdna.com

This small library provides an appender for [logback](https://logback.qos.ch) (a popular logging subsystem for the JVM). 

The appender pushes log entries to LogDNA via HTTPS

## How To

Logback uses XML file in known locations with the most common being `classpath:/logback.xml`

This is what a logback.xml might look like:

    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>

        <!-- Take both of these LOGDNA appenders -->
        <!-- this one is a plain old HTTP transport -->
        <appender name="LOGDNA-HTTP" class="net._95point2.utils.LogDNAAppender">
        <appName>LogDNA-Logback-Test</appName>
        <ingestKey>${LOGDNA_INGEST_KEY}</ingestKey>
        <includeStacktrace>true</includeStacktrace>
        </appender>

        <!-- ... and this one should be attached to the root -->
        <appender name="LOGDNA" class="ch.qos.logback.classic.AsyncAppender">
          <appender-ref ref="LOGDNA-HTTP" />
        </appender>


      <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
          <pattern>%m%n</pattern>
          <charset>utf8</charset>
        </encoder>
      </appender> 

        <root level="INFO">
            <appender-ref ref="CONSOLE" />
          <appender-ref ref="LOGDNA" /><!-- the async one -->
        </root>
    </configuration>
    
This setup uses an asynchronous wrapper to deal with log shipping on another thread 
and has some sensible options for dealing with buffer management. [Read more here](https://logback.qos.ch/manual/appenders.html#AsyncAppender)

## Configure

You can go to town on most other logback configurations but the LogDNA only has a couple of settings
    
* `<appName>LogDNA-Logback-Test</appName>` set this for good log management in LogDNA
* `<ingestKey>${LOGDNA_INGEST_KEY}</ingestKey>` signup to LogDNA and find this in your account profile
* `<includeStacktrace>true</includeStacktrace>` this library can send multiline stacktraces (see image) - Raw syslog transport cannot
    
## More Info

* The log line displays the thread, the logger (class) and the message

* The HTTP Transport is done by the very lightweight [DavidWebb](https://github.com/hgoebl/DavidWebb) REST Library and so doesn't introduce bulky dependencies


## Screenshot of the LogDNA log viewer

![Optional Text](../master/src/test/resources/logdna.png)
