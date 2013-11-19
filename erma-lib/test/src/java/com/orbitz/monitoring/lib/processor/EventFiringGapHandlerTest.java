package com.orbitz.monitoring.lib.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.lib.processor.LatencyMonitoringCoverageMonitorProcessor.EventFiringGapHandler;

public class EventFiringGapHandlerTest extends EventFiringGapHandler {

    @Test
    public void withChildren() {
       EventFiringGapHandler handler = new EventFiringGapHandler();
       String parentName = "ParentName";
       String leftChildName = "LeftName";
       String rightChildName = "RightName";
       long latency = 1000;
       TransactionMonitor parent = new TransactionMonitor(parentName);
       parent.set(Attribute.LATENCY, latency);
       TransactionMonitor leftChild = new TransactionMonitor(leftChildName);
       TransactionMonitor rightChild = new TransactionMonitor(rightChildName);
       EventMonitor gapEvent = (EventMonitor) handler.handleGap(parent, leftChild, rightChild, latency);
       assertEquals("MonitoringCoverageGap", gapEvent.getAsString(Attribute.NAME));
       assertEquals(parentName, gapEvent.getAsString("monitorName"));
       assertEquals(leftChildName, gapEvent.getAsString("leftChild"));
       assertEquals(rightChildName, gapEvent.getAsString("rightChild"));
       assertEquals(latency, gapEvent.getAsLong(Attribute.LATENCY));
    }

    @Test
    public void withoutChildren() {
       EventFiringGapHandler handler = new EventFiringGapHandler();
       String parentName = "ParentName";
       long latency = 1000;
       TransactionMonitor parent = new TransactionMonitor(parentName);
       parent.set(Attribute.LATENCY, latency);
       EventMonitor gapEvent = (EventMonitor) handler.handleGap(parent, null, null, latency);
       assertEquals("MonitoringCoverageGap", gapEvent.getAsString(Attribute.NAME));
       assertEquals(parentName, gapEvent.getAsString("monitorName"));
       assertNull(gapEvent.get("leftChild"));
       assertNull(gapEvent.get("rightChild"));
       assertEquals(latency, gapEvent.getAsLong(Attribute.LATENCY));
    }
}
