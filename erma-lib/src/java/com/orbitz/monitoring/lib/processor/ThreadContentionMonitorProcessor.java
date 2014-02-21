package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * This is an implementation of the {@link com.orbitz.monitoring.api.MonitorProcessor} interface 
 * that reports synchronization statistics for the current thread within the scope of a 
 * TransactionMonitor.
 * <br/>
 * Metrics are derived from java.lang.management.ThreadInfo.
 *
 * @author Matt O'Keefe
 *
 * @@org.springframework.jmx.export.metadata.ManagedResource
 * (description="This MonitorProcessor can be dis/enabled.")
 */
@ManagedResource(description = "This MonitorProcessor can be dis/enabled.")
public class ThreadContentionMonitorProcessor
    extends MonitorProcessorAdapter {

  private static final Logger log = Logger.getLogger(ThreadContentionMonitorProcessor.class);

  private boolean enabled = false;

  // ** PUBLIC METHODS ******************************************************
  public void monitorStarted(Monitor monitor) {
    if (enabled && monitor instanceof  TransactionMonitor) {
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
    if (enabled && monitor instanceof  TransactionMonitor) {
      if (monitor.hasAttribute("startBlockedCount") 
          && monitor.hasAttribute("startBlockedCount") 
          && monitor.hasAttribute("startBlockedCount") 
          && monitor.hasAttribute("startBlockedCount")) {
        TransactionMonitor tMon = (TransactionMonitor) monitor;
        ThreadMXBean tmxbean = ManagementFactory.getThreadMXBean();      
        long id = Thread.currentThread().getId();
        ThreadInfo thInfo = tmxbean.getThreadInfo(id);
        tMon.set("blockedCount", thInfo.getBlockedCount() - tMon.getAsLong("startBlockedCount"));
        tMon.set("blockedTime",  thInfo.getBlockedTime()  - tMon.getAsLong("startBlockedTime"));
        tMon.set("waitedCount",  thInfo.getWaitedCount()  - tMon.getAsLong("startWaitedCount"));
        tMon.set("waitedTime",   thInfo.getWaitedTime()   - tMon.getAsLong("startWaitedTime"));
      }
    }
  }

  /**
   * @return boolean
   */
  @ManagedAttribute(description = "true if this MonitorProcessor is enabled")
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * @param enabled boolean enabled status
   */
  @ManagedAttribute(description = "set to true to enable this MonitorProcessor")
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
