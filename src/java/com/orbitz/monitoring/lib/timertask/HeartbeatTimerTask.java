package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.MonitoringLevel;

import java.util.TimerTask;

/**
 * HeartbeatTimerTask fires an EventMonitor named MonitoringEngineManager.lifecycle every time
 * it is executed by a java.util.Timer.  The Monitor will have an attribute named "eventType" with
 * a value of "heartbeat".
 *
 * @since 3.5
 * 
 * @author Matt O'Keefe
 */
public class HeartbeatTimerTask extends TimerTask {


    /**
     * Sends a heartbeat event.
     */
    public void run() {

        EventMonitor monitor = new EventMonitor("MonitoringEngineManager.lifecycle", MonitoringLevel.ESSENTIAL);
        monitor.set("eventType", "heartbeat");
        monitor.fire();
    }
}
