package com.orbitz.monitoring.lib.processor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.mockito.Mockito;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.lib.processor.LatencyMonitoringCoverageMonitorProcessor.EventFiringGapHandler;
import com.orbitz.monitoring.lib.processor.LatencyMonitoringCoverageMonitorProcessor.GapHandler;

/**
 * Unit tests for the LatencyMonitoringCoverageMonitorProcessor.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class LatencyMonitoringCoverageMonitorProcessorTest extends TestCase {
    private LatencyMonitoringCoverageMonitorProcessor processor;
    private GapHandler handler;
    private long latency;

    protected void setUp()
            throws Exception {
        handler = Mockito.mock(GapHandler.class);
        processor = new LatencyMonitoringCoverageMonitorProcessor(handler);
        latency = processor.getThreshold() + 5;
    }
    
    public void testDefaults() {
        LatencyMonitoringCoverageMonitorProcessor processor = new LatencyMonitoringCoverageMonitorProcessor();
        assertTrue(processor.getGapHandler() instanceof EventFiringGapHandler);
    }
    
    public void testNotTransaction() {
        Monitor monitor = new AbstractMonitor() {};
        
        processor.process(monitor);
        
        Mockito.verifyZeroInteractions(handler);
    }

    public void testNoChildrenWithCoverage() {
        TransactionMonitor monitor = new TransactionMonitor("Test"){};
        monitor.set(Attribute.LATENCY, 0);
        
        processor.process(monitor);
        
        Mockito.verifyZeroInteractions(handler);
    }

    public void testNoChildrenWithoutCoverage() {
        Date parentStart = new Date();
        Date parentEnd = new Date(parentStart.getTime() + latency);
        TransactionMonitor parent = createParent(parentStart, parentEnd);
        parent.set(Attribute.LATENCY, latency);
        
        processor.process(parent);
        
        Mockito.verify(handler).handleGap(parent, null, null, latency);
    }

    public void testOneChildWithCoverage() {
        Date parentStart = new Date();
        Date parentEnd = new Date(parentStart.getTime() + latency);
        Date childStart = new Date(parentEnd.getTime() - 5);
        Date childEnd = parentEnd;
        
        TransactionMonitor parent = createParent(parentStart, parentEnd);
        addChild(parent, childStart, childEnd);
        parent.set(Attribute.LATENCY, latency);

        processor.process(parent);
        
        Mockito.verifyZeroInteractions(handler);
    }

    public void testOneChildWithoutLHCoverage() {
        Date parentStart = new Date();
        Date parentEnd = new Date(parentStart.getTime() + latency);
        Date childStart = new Date(parentEnd.getTime() - 1);
        Date childEnd = parentEnd;
        
        TransactionMonitor parent = createParent(parentStart, parentEnd);
        TransactionMonitor child = addChild(parent, childStart, childEnd);
        parent.set(Attribute.LATENCY, latency);

        processor.process(parent);
        
        Mockito.verify(handler).handleGap(parent, null, child, latency - 1);
    }

    public void testOneChildWithoutRHCoverage() {
        Date parentStart = new Date();
        Date parentEnd = new Date(parentStart.getTime() + latency);
        Date childStart = parentStart;
        Date childEnd = new Date(parentStart.getTime() + 1);
        
        TransactionMonitor parent = createParent(parentStart, parentEnd);
        TransactionMonitor child = addChild(parent, childStart, childEnd);
        parent.set(Attribute.LATENCY, latency);

        processor.process(parent);
        
        Mockito.verify(handler).handleGap(parent, child, null, latency - 1);
    }

    public void testTwoChildrenWithCoverage() {
        Date parentStart = new Date();
        Date parentEnd = new Date(parentStart.getTime() + latency);
        Date child1Start = parentStart;
        Date child1End = new Date(parentStart.getTime() + 3);
        Date child2Start = new Date(parentEnd.getTime() - 3);
        Date child2End = parentEnd;
        
        TransactionMonitor parent = createParent(parentStart, parentEnd);
        addChild(parent, child1Start, child1End);
        addChild(parent, child2Start, child2End);
        parent.set(Attribute.LATENCY, latency);

        processor.process(parent);
        
        Mockito.verifyZeroInteractions(handler);
    }

    public void testTwoChildrenWithoutMiddleCoverage() {
        Date parentStart = new Date();
        Date parentEnd = new Date(parentStart.getTime() + latency);
        Date child1Start = parentStart;
        Date child1End = new Date(parentStart.getTime() + 1);
        Date child2Start = new Date(parentEnd.getTime() - 1);
        Date child2End = parentEnd;
        
        TransactionMonitor parent = createParent(parentStart, parentEnd);
        TransactionMonitor child1 = addChild(parent, child1Start, child1End);
        TransactionMonitor child2 = addChild(parent, child2Start, child2End);
        parent.set(Attribute.LATENCY, latency);

        processor.process(parent);
        
        Mockito.verify(handler).handleGap(parent, child1, child2, latency - 2);
    }

    public void testTwoChildrenWithoutLHCoverage() {
        Date parentStart = new Date();
        Date parentEnd = new Date(parentStart.getTime() + latency);
        Date child1Start = new Date(parentEnd.getTime() - 2);
        Date child1End = new Date(child1Start.getTime() + 1);
        Date child2Start = child1End;
        Date child2End = parentEnd;

        TransactionMonitor parent = createParent(parentStart, parentEnd);
        TransactionMonitor child1 = addChild(parent, child1Start, child1End);
        addChild(parent, child2Start, child2End);
        parent.set(Attribute.LATENCY, latency);

        processor.process(parent);
        
        Mockito.verify(handler).handleGap(parent, null, child1, latency - 2);
    }

    public void testTwoChildrenWithoutRHCoverage() {
        Date parentStart = new Date();
        Date parentEnd = new Date(parentStart.getTime() + latency);
        Date child1Start = parentStart;
        Date child1End = new Date(child1Start.getTime() + 1);
        Date child2Start = child1End;
        Date child2End = new Date(child1End.getTime() + 1);
        
        TransactionMonitor parent = createParent(parentStart, parentEnd);
        addChild(parent, child1Start, child1End);
        TransactionMonitor child2 = addChild(parent, child2Start, child2End);
        parent.set(Attribute.LATENCY, latency);

        processor.process(parent);
        
        Mockito.verify(handler).handleGap(parent, child2, null, latency - 2);
    }

    private TransactionMonitor createParent(Date parentStart, Date parentEnd) {
        Map<String, Object> parentAttrs = new HashMap<String, Object>();
        parentAttrs.put(Attribute.START_TIME, parentStart);
        parentAttrs.put(Attribute.END_TIME, parentEnd);
        
        TransactionMonitor parent = new TransactionMonitor("Parent", parentAttrs);
        return parent;
    }
    
    private TransactionMonitor addChild(TransactionMonitor parent, Date childStart, Date childEnd) {
        Map<String, Object> childAttrs = new HashMap<String, Object>();
        childAttrs.put(Attribute.START_TIME, childStart);
        childAttrs.put(Attribute.END_TIME, childEnd);
        
        TransactionMonitor child = new TransactionMonitor("Child", childAttrs);
        parent.addChildMonitor(child);
        
        return child;
    }
    
    public void testMonitorInBadState() {
        TestAppender appender = new TestAppender();
        Logger logger = Logger.getLogger(LatencyMonitoringCoverageMonitorProcessor.class.getName());
        logger.addAppender(appender);
        logger.setLevel(Level.ALL);
        Date parentStart = new Date();
        Date parentEnd = new Date(parentStart.getTime() + latency);
        TransactionMonitor parent = createParent(parentStart, parentEnd);
        //Must have latency to be processed
        //parent.set(Attribute.LATENCY, latency);
        
        processor.process(parent);
        
        LoggingEvent loggingEvent = appender.getEvents().get(0);
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertEquals("failed to check monitoring coverage; application is unaffected", loggingEvent.getMessage());
    }

}
