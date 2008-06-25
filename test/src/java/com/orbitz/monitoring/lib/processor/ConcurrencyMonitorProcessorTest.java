package com.orbitz.monitoring.lib.processor;

import junit.framework.TestCase;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

import java.util.Map;

/**
 * Unit tests for the ConcurrencyMonitorProcessor.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class ConcurrencyMonitorProcessorTest extends TestCase {
    // ** PRIVATE DATA ********************************************************

    private ConcurrencyMonitorProcessor concurrencyMonitorProcessor = new ConcurrencyMonitorProcessor();
    private MockMonitorProcessor mockMonitorProcessor = new MockMonitorProcessor();

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {

        super.setUp();

        MonitorProcessor[] processors = new MonitorProcessor[2];
        processors[0] = concurrencyMonitorProcessor;
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
    public void testConcurrency() {

        TransactionMonitor monitor = new TransactionMonitor("Test");
        monitor.done();
        assertEquals("concurrency s/b 1", 1, monitor.getAsInt("concurrencyCount"));
        mockMonitorProcessor.clear();


        monitor = new TransactionMonitor("Test");
        TransactionMonitor monitor2 = new TransactionMonitor("Test");
        monitor2.done();
        assertEquals("concurrency for Test s/b 2", 2, monitor2.getAsInt("concurrencyCount"));
        monitor.done();
        assertEquals("concurrency for Test s/b 1", 1, monitor.getAsInt("concurrencyCount"));
        Map map = concurrencyMonitorProcessor.getAll();
        int c = ((Integer) map.get("Test")).intValue();
        assertEquals("concurrency for Test s/b 0", 0, c);        
        mockMonitorProcessor.clear();
    }

}
