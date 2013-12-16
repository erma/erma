package com.orbitz.monitoring.lib.timertask;

import junit.framework.TestCase;

import com.orbitz.monitoring.api.Monitor;

/**
 * Unit tests for the HeartbeatTimerTask.
 * @author Matt O'Keefe
 */
public class HeartbeatTimerTaskTest extends TestCase {

    private HeartbeatTimerTask task = new HeartbeatTimerTask();

    public void testHeartbeat() {
        Monitor monitor = task.emitMonitors().iterator().next();
        assertEquals("Didn't fire a lifecycle event",
                monitor.get(Monitor.NAME), "MonitoringEngineManager.lifecycle");
        assertEquals("Didn't find eventType=heartbeat attribute",
                monitor.get("eventType"), "heartbeat");
    }
}
