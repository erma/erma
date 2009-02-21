package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * This is an implementation of the {@link com.orbitz.monitoring.api.MonitorProcessor} interface that
 * checks to see if latency > a given threshold and no child monitors are contained within a given
 * TransactionMonitor.
 *
 * @author Matt O'Keefe
 */

public class LatencyMonitoringCoverageMonitorProcessor
        extends MonitorProcessorAdapter {

    private static final Logger log = Logger.getLogger(LatencyMonitoringCoverageMonitorProcessor.class);

    private static final long DEFAULT_THRESHOLD = 5000;

    private long threshold = DEFAULT_THRESHOLD;

    // ** PUBLIC METHODS ******************************************************
    public void process(Monitor monitor) {

        try {
            if (TransactionMonitor.class.isAssignableFrom(monitor.getClass())) {
                TransactionMonitor parent = (TransactionMonitor) monitor;
                long latency = parent.getAsLong(Attribute.LATENCY);
                if (latency > threshold) {
                    ArrayList list = new ArrayList();
                    Iterator iter = parent.getChildMonitors().iterator();
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        if (TransactionMonitor.class.isAssignableFrom(next.getClass())) {
                            list.add(next);
                        }
                    }
                    TransactionMonitor leftChild = null;
                    TransactionMonitor rightChild = null;
                    int listSize = list.size();
                    if (listSize == 0) {
                        fireMonitoringGapEvent(parent, leftChild, rightChild, latency);
                    } else {
                        Date parentStartDate = (Date) parent.get(Attribute.START_TIME);
                        Date parentEndDate = (Date) parent.get(Attribute.END_TIME);
                        if (listSize == 1) {
                            rightChild = (TransactionMonitor) list.get(0);
                            leftChild = rightChild;
                            Date rightChildStartDate = (Date) rightChild.get(Attribute.START_TIME);
                            Date leftChildEndDate = (Date) leftChild.get(Attribute.END_TIME);
                            latency = rightChildStartDate.getTime() - parentStartDate.getTime();
                            if (latency > threshold) {
                                fireMonitoringGapEvent(parent, null, rightChild, latency);
                            }
                            latency = parentEndDate.getTime() - leftChildEndDate.getTime();
                            if (latency > threshold) {
                                fireMonitoringGapEvent(parent, leftChild, null, latency);
                            }
                        } else if (listSize > 1) {
                            rightChild = (TransactionMonitor) list.get(0);
                            Date startDateB = (Date) rightChild.get(Attribute.START_TIME);
                            if (startDateB.getTime() - parentStartDate.getTime() > threshold) {
                                fireMonitoringGapEvent(parent, null, rightChild, latency);
                            }
                            int aIdx = 0;
                            int bIdx = 1;
                            while (bIdx < list.size()) {
                                leftChild = (TransactionMonitor) list.get(aIdx);
                                rightChild = (TransactionMonitor) list.get(bIdx);
                                Date endDateA = (Date) leftChild.get(Attribute.END_TIME);
                                startDateB = (Date) rightChild.get(Attribute.START_TIME);
                                latency = startDateB.getTime() - endDateA.getTime();
                                if (latency > threshold) {
                                    fireMonitoringGapEvent(parent, leftChild, rightChild, latency);
                                }
                                aIdx++;
                                bIdx++;
                            }
                            Date endDateB = (Date) rightChild.get(Attribute.END_TIME);
                            latency = parentEndDate.getTime() - endDateB.getTime();
                            if (latency > threshold) {
                                fireMonitoringGapEvent(parent, rightChild, null, latency);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("failed to check monitoring coverage; application is unaffected", e);
        }
    }

    private void fireMonitoringGapEvent(TransactionMonitor tMon, TransactionMonitor leftChild, TransactionMonitor rightChild, long latency) {

        EventMonitor eventMonitor = new EventMonitor("MonitoringCoverageGap");
        eventMonitor.set("monitorName", tMon.get(Attribute.NAME));
        eventMonitor.set("leftChild", leftChild == null ? null : leftChild.get(Attribute.NAME));
        eventMonitor.set("rightChild", rightChild == null ? null : rightChild.get(Attribute.NAME));
        eventMonitor.set(Attribute.LATENCY, latency);
        eventMonitor.fire();
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

}
