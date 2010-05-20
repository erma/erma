package com.orbitz.monitoring.lib.interceptor;

import junit.framework.TestCase;
import com.orbitz.monitoring.lib.processor.CPUProfilingMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * Unit tests for the ThreadDefaultUncaughtExceptionHandlerTe.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class ThreadDefaultUncaughtExceptionHandlerTest extends TestCase {
    // ** PRIVATE DATA ********************************************************

    private MockMonitorProcessor _mockMonitorProcessor = new MockMonitorProcessor();

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {

        super.setUp();

        MonitorProcessor[] processors = new MonitorProcessor[1];
        processors[0] = _mockMonitorProcessor;
        MonitoringEngine.getInstance().setProcessorFactory(
                new MockMonitorProcessorFactory(processors));
        MonitoringEngine.getInstance().setDecomposer(new MockDecomposer());
        MonitoringEngine.getInstance().startup();

    }

    protected void tearDown()
            throws Exception {
        super.tearDown();

        MonitoringEngine.getInstance().shutdown();
    }

    // ** TEST METHODS ********************************************************
    public void testThreadTerminationDueToUncaughtThrowable() {

        _mockMonitorProcessor.clear();
        Thread.setDefaultUncaughtExceptionHandler(new ThreadDefaultUncaughtExceptionHandler());
        new Thread(new Runnable() {
            public void run() {
                throw new RuntimeException();
            }
        }).start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Monitor[] monitors = _mockMonitorProcessor.extractProcessObjects();
        Monitor monitor = monitors[0];
        assertEquals("Didn't find a ThreadTerminationDueToUncaughtThrowable Monitor",
                monitor.get(Monitor.NAME), "ThreadTerminationDueToUncaughtThrowable");
        assertTrue("Didn't find a threadName attribute", monitor.hasAttribute("threadName"));
        assertTrue("Didn't find a threadClass attribute", monitor.hasAttribute("threadClass"));
        assertTrue("Didn't find a stackTrace attribute", monitor.hasAttribute("stackTrace"));
    }

}
