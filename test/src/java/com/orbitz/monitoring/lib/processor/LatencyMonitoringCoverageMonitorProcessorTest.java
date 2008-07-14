package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;

/**
 * Unit tests for the LatencyMonitoringCoverageMonitorProcessor.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class LatencyMonitoringCoverageMonitorProcessorTest extends TestCase {
    // ** PRIVATE DATA ********************************************************

    private MockMonitorProcessor mockMonitorProcessor = new MockMonitorProcessor();

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {

        super.setUp();

        MonitorProcessor[] processors = new MonitorProcessor[2];
        LatencyMonitoringCoverageMonitorProcessor latencyMonitoringCoverageMonitorProcessor = new LatencyMonitoringCoverageMonitorProcessor();
        latencyMonitoringCoverageMonitorProcessor.setThreshold(500);
        processors[0] = latencyMonitoringCoverageMonitorProcessor;
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
    public void testNoChildrenWithCoverage() {

        TransactionMonitor monitor = new TransactionMonitor("Test");
        monitor.done();
        assertNoMonitoringCoverageGapDetected();
        mockMonitorProcessor.clear();
    }

    public void testNoChildrenWithoutCoverage() {

        TransactionMonitor monitor = new TransactionMonitor("Test");
        try {
            Thread.sleep(505);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        monitor.done();
        assertMonitoringCoverageGapDetected();
        mockMonitorProcessor.clear();
    }

    public void testOneChildWithCoverage() {

        TransactionMonitor parent = new TransactionMonitor("Parent");
        TransactionMonitor child = new TransactionMonitor("Child");
        child.done();
        parent.done();
        assertNoMonitoringCoverageGapDetected();
        mockMonitorProcessor.clear();
    }

    public void testOneChildWithoutLHCoverage() {

        TransactionMonitor parent = new TransactionMonitor("Parent");
        try {
            Thread.sleep(505);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TransactionMonitor child = new TransactionMonitor("Child");
        child.done();
        parent.done();
        assertMonitoringCoverageGapDetected();
        mockMonitorProcessor.clear();
    }

    public void testOneChildWithoutRHCoverage() {

        TransactionMonitor parent = new TransactionMonitor("Parent");
        TransactionMonitor child = new TransactionMonitor("Child");
        child.done();
        try {
            Thread.sleep(505);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        parent.done();
        assertMonitoringCoverageGapDetected();
        mockMonitorProcessor.clear();
    }

    public void testTwoChildrenWithCoverage() {

        TransactionMonitor parent = new TransactionMonitor("Parent");
        TransactionMonitor child1 = new TransactionMonitor("Child1");
        child1.done();
        TransactionMonitor child2 = new TransactionMonitor("Child2");
        child2.done();
        parent.done();
        assertNoMonitoringCoverageGapDetected();
        mockMonitorProcessor.clear();
    }

    public void testTwoChildrenWithoutMiddleCoverage() {

        TransactionMonitor parent = new TransactionMonitor("Parent");
        TransactionMonitor child1 = new TransactionMonitor("Child1");
        child1.done();
        try {
            Thread.sleep(505);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TransactionMonitor child2 = new TransactionMonitor("Child2");
        child2.done();
        parent.done();
        assertMonitoringCoverageGapDetected();
        mockMonitorProcessor.clear();
    }

    public void testTwoChildrenWithoutLHCoverage() {

        TransactionMonitor parent = new TransactionMonitor("Parent");
        try {
            Thread.sleep(505);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TransactionMonitor child1 = new TransactionMonitor("Child1");
        child1.done();
        TransactionMonitor child2 = new TransactionMonitor("Child2");
        child2.done();
        parent.done();
        assertMonitoringCoverageGapDetected();
        mockMonitorProcessor.clear();
    }

    public void testTwoChildrenWithoutRHCoverage() {

        TransactionMonitor parent = new TransactionMonitor("Parent");
        TransactionMonitor child1 = new TransactionMonitor("Child1");
        child1.done();
        TransactionMonitor child2 = new TransactionMonitor("Child2");
        child2.done();
        try {
            Thread.sleep(505);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        parent.done();
        assertMonitoringCoverageGapDetected();
        mockMonitorProcessor.clear();
    }

    private void assertNoMonitoringCoverageGapDetected() {
        Monitor[] monitors = mockMonitorProcessor.extractProcessObjects();
        for (int i=0; i<monitors.length; i++) {
            Monitor monitor = monitors[i];
            assertNotSame("Found a MonitoringCoverageGap event where there should not be one",
                    "MonitoringCoverageGap", monitor.get(Monitor.NAME));
        }
    }

    private void assertMonitoringCoverageGapDetected() {
        boolean foundOne = false;
        Monitor[] monitors = mockMonitorProcessor.extractProcessObjects();
        for (int i=0; i<monitors.length; i++) {
            Monitor monitor = monitors[i];
            if ("MonitoringCoverageGap".equals(monitor.get(Monitor.NAME))) {
                foundOne = true;
            }
        }
        assertTrue("Didn't find a MonitoringCoverageGap event where there should have been one", foundOne);
    }
}
