package com.orbitz.monitoring.lib.processor;

import org.apache.log4j.Logger;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

/**
 * This is an implementation of the {@link com.orbitz.monitoring.api.MonitorProcessor} interface that
 * reports synchronization statistics for the current thread within the scope of a TransactionMonitor.
 *
 * <br>
 * Metrics are derived from <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/lang/management/ThreadInfo.html">ThreadInfo</a>.
 *
 * @author Matt O'Keefe
 *
 * @@org.springframework.jmx.export.metadata.ManagedResource
 * (description="This MonitorProcessor can be dis/enabled.")
 */

public class ThreadContentionMonitorProcessor
        extends MonitorProcessorAdapter {

    private static final Logger log = Logger.getLogger(ThreadContentionMonitorProcessor.class);

    private boolean enabled = false;

    // ** PUBLIC METHODS ******************************************************
    public void monitorStarted(Monitor monitor) {

        if (enabled && TransactionMonitor.class.isAssignableFrom(monitor.getClass())) {
            ThreadMXBean tmxbean = ManagementFactory.getThreadMXBean();
            long id = Thread.currentThread().getId();
            ThreadInfo threadInfo = tmxbean.getThreadInfo(id);
            monitor.set("startBlockedCount", threadInfo.getBlockedCount());
            monitor.set("startBlockedTime", threadInfo.getBlockedTime());
            monitor.set("startWaitedCount", threadInfo.getWaitedCount());
            monitor.set("startWaitedTime", threadInfo.getWaitedTime());
        }
    }

    public void process(Monitor monitor) {

        if (enabled && TransactionMonitor.class.isAssignableFrom(monitor.getClass())) {
            TransactionMonitor transactionMonitor = (TransactionMonitor) monitor;
            ThreadMXBean tmxbean = ManagementFactory.getThreadMXBean();            
            long id = Thread.currentThread().getId();
            ThreadInfo threadInfo = tmxbean.getThreadInfo(id);
            transactionMonitor.set("blockedCount", threadInfo.getBlockedCount()-transactionMonitor.getAsLong("startBlockedCount"));
            transactionMonitor.set("blockedTime", threadInfo.getBlockedTime()-transactionMonitor.getAsLong("startBlockedTime"));
            transactionMonitor.set("waitedCount", threadInfo.getWaitedCount()-transactionMonitor.getAsLong("startWaitedCount"));
            transactionMonitor.set("waitedTime", threadInfo.getWaitedTime()-transactionMonitor.getAsLong("startWaitedTime"));
        }
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute (description="true if this MonitorProcessor is enabled")
     *
     * @return boolean
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute (description="set to true to enable this MonitorProcessor")
     *
     */
    public void setEnabled(boolean enabled) {
        ThreadMXBean tmxbean = ManagementFactory.getThreadMXBean();
        if (tmxbean.isThreadContentionMonitoringSupported()) {
            tmxbean.setThreadContentionMonitoringEnabled(enabled);
            this.enabled = enabled;
        } else {
            log.warn("Thread contention monitoring is not supported by this VM");
        }
    }
}
