package com.orbitz.monitoring.lib.processor;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * Unit tests for the XmlLoggingMonitorProcessor.
 * @author Doug Barth
 */
public class XmlLoggingMonitorProcessorTest extends TestCase {

    private XmlLoggingMonitorProcessor _processor = new XmlLoggingMonitorProcessor();

    public void testLogging() {
        Logger logger = Logger.getLogger(XmlLoggingMonitorProcessor.class);
        TestAppender testAppender = new TestAppender();
        logger.addAppender(testAppender);
        logger.setLevel(Level.ALL);

        TransactionMonitor monitor = new TransactionMonitor("test");
        _processor.process(monitor);
        String xml = (String) ((LoggingEvent)testAppender.getEvents().get(0)).getMessage();

        // XmlMonitorRendererTest will assert the details of monitor -> xml rendering
        assertTrue("Actual: " + xml, xml.startsWith("<TransactionMonitor"));
    }
}
