package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;

/**
 * Unit tests for the CPUProfilingMonitorProcessor.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class CPUProfilingMonitorProcessorTest extends TestCase {
    // ** PRIVATE DATA ********************************************************

    CPUProfilingMonitorProcessor cpuProfilingMonitorProcessor =
            new CPUProfilingMonitorProcessor();
    private MockMonitorProcessor mockMonitorProcessor = new MockMonitorProcessor();

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {

        super.setUp();

        cpuProfilingMonitorProcessor.setEnabled(true);
        MonitorProcessor[] processors = new MonitorProcessor[2];
        processors[0] = cpuProfilingMonitorProcessor;
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
        consumeCPU();
        monitor.done();
        long cpuTimeMillis = monitor.getAsLong("cpuTimeMillis");
        assertTrue("CPU time apparently not measured", (cpuTimeMillis>1));
    }

    private void consumeCPU() {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start+500) {

        }
    }

}
