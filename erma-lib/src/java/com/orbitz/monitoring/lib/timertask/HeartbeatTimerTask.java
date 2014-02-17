package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.EventMonitor;

import java.util.Collection;
import java.util.Collections;

/**
 * HeartbeatTimerTask fires an EventMonitor named MonitoringEngineManager.lifecycle every time
 * it is executed by a java.util.Timer.  The Monitor will have an attribute named "eventType" with
 * a value of "heartbeat".
 *
 * @since 3.5
 * 
 * @author Matt O'Keefe
 */
public class HeartbeatTimerTask extends MonitorEmittingTimerTask {

    /**
     * Sends a heartbeat event.
     */
    public Collection<EventMonitor> emitMonitors() {
        EventMonitor monitor = new EventMonitor("MonitoringEngineManager.lifecycle", MonitoringLevel.ESSENTIAL);
        monitor.set("eventType", "heartbeat");
        monitor.fire();
        return Collections.singleton(monitor);
    }
}
