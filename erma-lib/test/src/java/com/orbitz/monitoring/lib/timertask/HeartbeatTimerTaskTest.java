package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.lib.BaseMonitoringEngineManager;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;

import java.util.LinkedList;

/**
 * Unit tests for the HeartbeatTimerTask.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class HeartbeatTimerTaskTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private MockMonitorProcessor _mockMonitorProcessor =
            new MockMonitorProcessor();
    private HeartbeatTimerTask _heartbeatTimerTask =
            new HeartbeatTimerTask();

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        MockMonitorProcessorFactory mockMonitorProcessorFactory =
                new MockMonitorProcessorFactory(_mockMonitorProcessor);
        MockDecomposer mockDecomposer = new MockDecomposer();
        LinkedList timerTasks = new LinkedList();
        timerTasks.add(_heartbeatTimerTask);
        BaseMonitoringEngineManager monitoringEngineManager =
                new BaseMonitoringEngineManager(mockMonitorProcessorFactory, mockDecomposer);
        monitoringEngineManager.setTimerTasks(timerTasks);
        monitoringEngineManager.startup();
    }

    protected void tearDown()
            throws Exception {
        super.tearDown();

        MonitoringEngine.getInstance().shutdown();
    }

    // ** TEST METHODS ********************************************************
    public void testHeartbeat() {

        _mockMonitorProcessor.clear();
        _heartbeatTimerTask.run();
        Monitor[] monitors = _mockMonitorProcessor.extractProcessObjects();
        assertEquals("Didn't fire a lifecycle event",
                monitors[0].get(Monitor.NAME), "MonitoringEngineManager.lifecycle");
        assertEquals("Didn't find eventType=heartbeat attribute",
                monitors[0].get("eventType"), "heartbeat");
    }
}
