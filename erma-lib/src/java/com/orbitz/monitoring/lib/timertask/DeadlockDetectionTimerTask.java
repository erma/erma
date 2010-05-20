package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.MonitoringLevel;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Timer task that will fire a monitor of deadlocked
 * threads are detected.
 */
public class DeadlockDetectionTimerTask implements Runnable {

    /**
     * Default constructor. Enables thread contention
     * monitoring.
     */
    public DeadlockDetectionTimerTask() {
        super();
        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if(threadBean.isThreadContentionMonitoringSupported()) {
            threadBean.setThreadContentionMonitoringEnabled(true);
        }
    }

    /**
     * Implementaion of {@link java.lang.Runnable#run}.
     */
    public void run() {
        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if(threadBean.isThreadContentionMonitoringEnabled()) {
            if(threadBean.findMonitorDeadlockedThreads() != null) {
                final EventMonitor monitor = new EventMonitor("JvmStats", MonitoringLevel.ESSENTIAL);
                monitor.set("type", "Thread.Deadlock");
                monitor.set("count", 1);
                monitor.fire();
            }
        }
    }
}
