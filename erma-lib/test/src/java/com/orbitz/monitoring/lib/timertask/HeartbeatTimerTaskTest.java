package com.orbitz.monitoring.lib.timertask;

import java.util.LinkedList;
import java.util.TimerTask;

import junit.framework.TestCase;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.lib.BaseMonitoringEngineManager;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;

/**
 * Unit tests for the HeartbeatTimerTask.
 * @author Matt O'Keefe
 */
public class HeartbeatTimerTaskTest extends TestCase {

    private MockMonitorProcessor _mockMonitorProcessor =
            new MockMonitorProcessor();
    private HeartbeatTimerTask _heartbeatTimerTask =
            new HeartbeatTimerTask();

    protected void setUp() throws Exception {
        super.setUp();

        MockMonitorProcessorFactory mockMonitorProcessorFactory =
                new MockMonitorProcessorFactory(_mockMonitorProcessor);
        MockDecomposer mockDecomposer = new MockDecomposer();
        LinkedList<TimerTask> timerTasks = new LinkedList<TimerTask>();
        timerTasks.add(_heartbeatTimerTask);
        BaseMonitoringEngineManager monitoringEngineManager =
                new BaseMonitoringEngineManager(mockMonitorProcessorFactory, mockDecomposer);
        monitoringEngineManager.setTimerTasks(timerTasks);
        monitoringEngineManager.startup();
    }

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
