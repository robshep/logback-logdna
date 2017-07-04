# LogDNA Appender for Logback

LogDNA is a hosted logging platform: https://logdna.com

This small library provides an appender for [logback](https://logback.qos.ch) (a popular logging subsystem for the JVM). 

The appender pushes log entries to LogDNA via HTTPS

Logback's thread-bound storage, the MDC, is sent to LogDNA as metadata to be indexed and then searchable. (See screenshot, and more below) 

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

## Get it using Maven

1. Add a link to this project's repository in your pom

`project > repositories > `

    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>bintray-robshep-oss</id>
        <name>bintray</name>
        <url>http://dl.bintray.com/robshep/oss</url>
    </repository>

2. Add the dependency

`project > dependencies > `

    <dependency>
      <groupId>net._95point2.utils</groupId>
      <artifactId>logback-logdna</artifactId>
      <version>1.1.0</version>
    </dependency>

#### Or, plain ol' download

Or just download the Jar and it's dependencies from https://github.com/robshep/logback-logdna/releases

## Configure

You can go to town on most other logback configurations but the LogDNA only has a couple of settings (shown as the default)
    
* `<appName>LogDNA-Logback-Test</appName>` set this for good log management in LogDNA
* `<ingestKey>${LOGDNA_INGEST_KEY}</ingestKey>` signup to LogDNA and find this in your account profile
* `<includeStacktrace>true</includeStacktrace>` this library can send multiline stacktraces (see image) - Raw syslog transport cannot
* `<sendMDC>true</sendMDC>` copies over logback's Mapped Diagnostic Context [(MDC)](https://logback.qos.ch/manual/mdc.html) as LogDNA Metadata which are then indexed and searchable.
    
## More Info

* The log line displays the thread, the logger (class) and the message
* LogDNA's metadata is populated with the logger as an indexable/searchable property.
* The HTTP Transport is done by the very lightweight [DavidWebb](https://github.com/hgoebl/DavidWebb) REST Library and so doesn't introduce bulky dependencies

## Using the MDC for Meta Data

The combination of logback's MDC and LogDNA's metadata support is pretty powerful and means you can correlate web-requests, or a userId, or something else that happens in a thread in your application.  

For example, doing this _anywhere_ in your application...

	MDC.put("customerId", "C-1");
	MDC.put("requestId", "cafebabe1");

... means that you can then go and search for say, **that** customer in logDNA like this:

	meta.customerId:"C-1"


	

## Screenshot of the LogDNA log viewer, including extra metadata

![The Spoils of these toils](../master/src/test/resources/logdna-meta.png)
