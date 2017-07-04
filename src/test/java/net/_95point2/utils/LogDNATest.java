package net._95point2.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDNATest 
{
	/**
	 * 
	 * Signup for LogDNA then run the test like this to try it out.
	 * 
	 *    mvn test -DLOGDNA_INGEST_KEY="<my_logdna_ingest_key"
	 */
	@Test
	public void testLogDNALogbackConf() throws InterruptedException
	{
	    Logger logger = LoggerFactory.getLogger(LogDNATest.class);

	    logger.info("Okay");
	    
	    logger.warn("Hmmm, that seemed odd...");
	    
	    logger.error("Ah Sh*t!", new RuntimeException("Bang!"));
	    
	    logger.info("Well, Bye!");
	}
}
