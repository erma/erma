package com.orbitz.monitoring.lib.timertask;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.Collections;

import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.EventMonitor;

/**
 * Timer task that will fire a monitor of deadlocked
 * threads are detected.
 */
public class DeadlockDetectionTimerTask extends MonitorEmittingTimerTask {
    /**
     * Default constructor. Enables thread contention
     * monitoring.
     */
    public DeadlockDetectionTimerTask() {
        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if(threadBean.isThreadContentionMonitoringSupported()) {
            threadBean.setThreadContentionMonitoringEnabled(true);
        }
    }

    public Collection<EventMonitor> emitMonitors() {
        EventMonitor monitor = null;
        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if(threadBean.isThreadContentionMonitoringEnabled()) {
            long[] findMonitorDeadlockedThreads = threadBean.findMonitorDeadlockedThreads();
            if(findMonitorDeadlockedThreads != null) {
                monitor = new EventMonitor("JvmStats", MonitoringLevel.ESSENTIAL);
                monitor.set("type", "Thread.Deadlock");
                monitor.set("count", 1);
                monitor.fire();
            }
        }
        return Collections.singleton(monitor);
    }
}
