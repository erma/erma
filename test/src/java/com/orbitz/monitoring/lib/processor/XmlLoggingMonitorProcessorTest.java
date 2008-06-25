package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Unit tests for the XmlLoggingMonitorProcessor.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class XmlLoggingMonitorProcessorTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private XmlLoggingMonitorProcessor _processor = new XmlLoggingMonitorProcessor();

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        MonitoringEngine.getInstance().setProcessorFactory(
                new MockMonitorProcessorFactory(_processor));
        MonitoringEngine.getInstance().setDecomposer(new MockDecomposer());
        MonitoringEngine.getInstance().startup();
    }

    protected void tearDown()
            throws Exception {
        super.tearDown();

        MonitoringEngine.getInstance().shutdown();
    }

    // ** TEST METHODS ********************************************************

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
