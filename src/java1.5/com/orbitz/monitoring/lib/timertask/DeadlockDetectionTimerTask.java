package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.MonitoringLevel;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Created by IntelliJ IDEA.
 * User: mkemp
 * Date: Nov 5, 2008
 * Time: 4:51:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeadlockDetectionTimerTask implements Runnable {

    public DeadlockDetectionTimerTask() {
        super();
        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if(threadBean.isThreadContentionMonitoringSupported()) {
            threadBean.setThreadContentionMonitoringEnabled(true);
        }
    }

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
