package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

import org.apache.log4j.Logger;

import java.util.Date;

/**
 * This is an implementation of the {@link com.orbitz.monitoring.api.MonitorProcessor} interface 
 * that checks to see if latency > a given threshold and no child monitors are contained within a 
 * given TransactionMonitor.
 *
 * @author Matt O'Keefe
 */

public class LatencyMonitoringCoverageMonitorProcessor
    extends MonitorProcessorAdapter {

  private static final Logger log = 
      Logger.getLogger(LatencyMonitoringCoverageMonitorProcessor.class);

  private static final long DEFAULT_THRESHOLD = 5000;

  private long threshold;
  
  private GapHandler gapHandler;
  
  public LatencyMonitoringCoverageMonitorProcessor() {
    this(new EventFiringGapHandler());
  }
  
  public LatencyMonitoringCoverageMonitorProcessor(GapHandler gapHandler) {
    this(gapHandler, DEFAULT_THRESHOLD);
  }

  public LatencyMonitoringCoverageMonitorProcessor(GapHandler gapHandler, long threshold) {
    this.gapHandler = gapHandler;
    setThreshold(threshold);
  }

  public void process(Monitor monitor) {

    try {
      if (monitor instanceof TransactionMonitor) {
        TransactionMonitor parent = (TransactionMonitor) monitor;
        long latency = parent.getAsLong(Attribute.LATENCY);
        if (latency > threshold) {
          processChildMonitors(parent, latency);
        }
      }
    } catch (Exception e) {
      log.warn("failed to check monitoring coverage; application is unaffected", e);
    }
  }

  private void processChildMonitors(TransactionMonitor parent, long latency) {
    TransactionMonitor leftChild = null;
    TransactionMonitor rightChild = null;
    for (Monitor child : parent.getChildMonitors()) {
      if (child instanceof TransactionMonitor) {
        leftChild = rightChild;
        rightChild = (TransactionMonitor) child;
        checkForGap(parent, leftChild, rightChild);
      }
    }
    leftChild = rightChild;
    rightChild = null;
    checkForGap(parent, leftChild, rightChild);
  }

  private void checkForGap(TransactionMonitor parent, TransactionMonitor leftChild, 
      TransactionMonitor rightChild) {
    Date leftEnd;
    if (leftChild == null) {
      leftEnd = (Date) parent.get(Attribute.START_TIME);
    } else {
      leftEnd = (Date) leftChild.get(Attribute.END_TIME);
    }
    Date rightStart;
    if (rightChild == null) {
      rightStart = (Date) parent.get(Attribute.END_TIME);
    } else {
      rightStart = (Date) rightChild.get(Attribute.START_TIME);
    }
    long latency = rightStart.getTime() - leftEnd.getTime();
    if (latency > threshold) {
      gapHandler.handleGap(parent, leftChild, rightChild, latency);
    }
  }

  public long getThreshold() {
    return threshold;
  }

  public void setThreshold(long threshold) {
    this.threshold = threshold;
  }
  
  protected GapHandler getGapHandler() {
    return gapHandler;
  }

  public static interface GapHandler {
    public Monitor handleGap(TransactionMonitor parent, 
        TransactionMonitor leftChild, TransactionMonitor rightChild, long latencyGap);
  }
  
  public static class EventFiringGapHandler implements GapHandler {

    @Override
    public Monitor handleGap(TransactionMonitor parent,
        TransactionMonitor leftChild, TransactionMonitor rightChild,
        long latencyGap) {
      EventMonitor eventMonitor = new EventMonitor("MonitoringCoverageGap");
      eventMonitor.set("monitorName", parent.get(Attribute.NAME));
      eventMonitor.set("leftChild", leftChild == null ? null : leftChild.get(Attribute.NAME));
      eventMonitor.set("rightChild", rightChild == null ? null : rightChild.get(Attribute.NAME));
      eventMonitor.set(Attribute.LATENCY, latencyGap);
      eventMonitor.fire();
      
      return eventMonitor;
    }
  }
}
