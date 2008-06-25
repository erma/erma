package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;

/**
 * Unit tests for the ResultCodeAnnotatingMonitorProcessor.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class ResultCodeAnnotatingMonitorProcessorTest extends TestCase {
    // ** PRIVATE DATA ********************************************************

    private MockMonitorProcessor mockMonitorProcessor = new MockMonitorProcessor();
    private ResultCodeAnnotatingMonitorProcessor processor = new ResultCodeAnnotatingMonitorProcessor();

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {

        super.setUp();

        MonitorProcessor[] processors = new MonitorProcessor[2];
        processors[0] = processor;
        processors[1] = mockMonitorProcessor;
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
    public void testStuff() {

        TransactionMonitor monitor = new TransactionMonitor("foo");
        monitor.done();
        assertEquals("success", mockMonitorProcessor.extractProcessObjects()[0].getAsString("resultCode"));

        mockMonitorProcessor.clear();
        monitor = new TransactionMonitor("foo");
        monitor.set("resultCode", "bar");
        monitor.done();
        assertEquals("bar", mockMonitorProcessor.extractProcessObjects()[0].getAsString("resultCode"));

        mockMonitorProcessor.clear();
        monitor = new TransactionMonitor("foo");
        monitor.failedDueTo(new IllegalArgumentException());
        monitor.done();
        assertEquals("java.lang.IllegalArgumentException", mockMonitorProcessor.extractProcessObjects()[0].getAsString("resultCode"));


        mockMonitorProcessor.clear();
        monitor = new TransactionMonitor("foo");
        monitor.failedDueTo(new RuntimeException(new IllegalArgumentException()));
        monitor.done();
        assertEquals("java.lang.IllegalArgumentException", mockMonitorProcessor.extractProcessObjects()[0].getAsString("resultCode"));

        EventMonitor event = new EventMonitor("foo");
        event.set("failureThrowable", new IllegalArgumentException());
        event.fire();
        assertFalse(event.hasAttribute("resultCode"));
    }
}
