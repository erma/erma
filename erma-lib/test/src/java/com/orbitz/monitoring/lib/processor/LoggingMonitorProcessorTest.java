package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.lib.BaseMonitoringEngineManager;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Unit tests for the LoggingMonitorProcessor.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Operations Architecture
 */
public class LoggingMonitorProcessorTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private LoggingMonitorProcessor _processor = new LoggingMonitorProcessor();
    private TestAppender _testAppender = new TestAppender();
    private String _testVmid = "JVM_ID_NOT_SET";

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();
        MonitoringEngine.getInstance().shutdown();
        BaseMonitoringEngineManager monitoringEngineManager =
                new BaseMonitoringEngineManager(new MockMonitorProcessorFactory(_processor),
                        new MockDecomposer());

        monitoringEngineManager.startup();

        _processor.startup();

        // log4j will not allow the same appender to be added multiple times
        //LogManager.resetConfiguration();
        Logger logger = Logger.getLogger(LoggingMonitorProcessor.class.getName());
        logger.addAppender(_testAppender);
        logger.setLevel(Level.ALL);

        MonitoringEngine.getInstance().clearCurrentThread();
    }

    protected void tearDown()
            throws Exception {
        super.tearDown();

        MonitoringEngine.getInstance().shutdown();

        _testAppender.reset();
    }

    // ** TEST METHODS ********************************************************

    public void testRenderMonitor() {
        _processor.setLogMonitorCreated(true);
        _processor.setLogMonitorStarted(true);

        TransactionMonitor monitor = new TransactionMonitor("testEvent");
        monitor.set(Monitor.VMID, _testVmid);
        monitor.set(Monitor.HOSTNAME, "localhost");
        monitor.done();

        String createdExpected = "com.orbitz.monitoring.api.monitor.TransactionMonitor" +
                "\n\t-> createdAt = " + monitor.get("createdAt") +
                "\n\t-> name = testEvent" +
                "\n\t-> sequenceId = m" +
                "\n\t-> threadId = " + Integer.toHexString(Thread.currentThread().hashCode());

        String startedExpected = "com.orbitz.monitoring.api.monitor.TransactionMonitor" +
                "\n\t-> createdAt = " + monitor.get("createdAt") +
                "\n\t-> failed = true" +
                "\n\t-> name = testEvent" +
                "\n\t-> sequenceId = m" +
                "\n\t-> startTime = " + monitor.get("startTime") +
                "\n\t-> threadId = " + Integer.toHexString(Thread.currentThread().hashCode());

        String processExpected = "com.orbitz.monitoring.api.monitor.TransactionMonitor" +
                "\n\t-> createdAt = " + monitor.get("createdAt") +
                "\n\t-> endTime = " + monitor.get("endTime") +
                "\n\t-> failed = true" +
                "\n\t-> hostname = " + monitor.get("hostname") +
                "\n\t-> latency = " + monitor.get("latency") +
                "\n\t-> name = testEvent" +
                "\n\t-> sequenceId = m" +
                "\n\t-> startTime = " + monitor.get("startTime") +
                "\n\t-> threadId = " + Integer.toHexString(Thread.currentThread().hashCode()) +
                "\n\t-> vmid = " + _testVmid;

        LoggingEvent logEvent = (LoggingEvent) _testAppender.getEvents().get(0);
        String logMsg = (String) logEvent.getMessage();
        
        assertEquals("monitorCreated: " + createdExpected, logMsg);

        logEvent = (LoggingEvent) _testAppender.getEvents().get(1);
        logMsg = (String) logEvent.getMessage();
        assertEquals("monitorStarted: " + startedExpected, logMsg);

        logEvent = (LoggingEvent) _testAppender.getEvents().get(2);
        logMsg = (String) logEvent.getMessage();
        assertEquals("process: " + processExpected, logMsg);
    }
}
