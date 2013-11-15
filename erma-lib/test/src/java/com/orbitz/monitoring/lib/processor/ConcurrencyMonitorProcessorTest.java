package com.orbitz.monitoring.lib.processor;

import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import junit.framework.TestCase;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * Unit tests for the ConcurrencyMonitorProcessor.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class ConcurrencyMonitorProcessorTest extends TestCase {
    private ConcurrencyMonitorProcessor concurrencyMonitorProcessor = new ConcurrencyMonitorProcessor();
    
    public void tearDown() {
        ConcurrencyMonitorProcessor.clear();
    }
    
    public void testStartOffNonTransaction() {
        Monitor m = new AbstractMonitor(){};
        concurrencyMonitorProcessor.setEnabled(false);

        concurrencyMonitorProcessor.monitorStarted(m);
        
        assertTrue(concurrencyMonitorProcessor.getAll().isEmpty());
    }
    
    public void testStartOnNonTransaction() {
        Monitor m = new AbstractMonitor(){};
        concurrencyMonitorProcessor.setEnabled(true);

        concurrencyMonitorProcessor.monitorStarted(m);
        
        assertTrue(concurrencyMonitorProcessor.getAll().isEmpty());
    }
    
    public void testStartOff() {
        Monitor m = new TransactionMonitor("T1");
        concurrencyMonitorProcessor.setEnabled(false);

        concurrencyMonitorProcessor.monitorStarted(m);
        
        assertTrue(concurrencyMonitorProcessor.getAll().isEmpty());
    }
    
    public void testStartOne() {
        Monitor m = new TransactionMonitor("T1");
        concurrencyMonitorProcessor.setEnabled(true);

        concurrencyMonitorProcessor.monitorStarted(m);
        
        Map<String, Integer> all = concurrencyMonitorProcessor.getAll();
        assertEquals(1, all.size());
        assertEquals(1, all.get("T1").intValue());
    }
    
    public void testStartTwo() {
        Monitor m1 = new TransactionMonitor("T1");
        Monitor m2 = new TransactionMonitor("T1");
        concurrencyMonitorProcessor.setEnabled(true);

        concurrencyMonitorProcessor.monitorStarted(m1);
        concurrencyMonitorProcessor.monitorStarted(m2);
        
        Map<String, Integer> all = concurrencyMonitorProcessor.getAll();
        assertEquals(1, all.size());
        assertEquals(2, all.get("T1").intValue());
    }
    
    public void testProcessOffNonTransaction() {
        Monitor m = new AbstractMonitor(){};
        concurrencyMonitorProcessor.setEnabled(false);
        concurrencyMonitorProcessor.monitorStarted(m);

        concurrencyMonitorProcessor.process(m);
        
        assertTrue(concurrencyMonitorProcessor.getAll().isEmpty());
    }
    
    public void testProcessOnNonTransaction() {
        Monitor m = new AbstractMonitor(){};
        concurrencyMonitorProcessor.setEnabled(true);
        concurrencyMonitorProcessor.monitorStarted(m);

        concurrencyMonitorProcessor.process(m);
        
        assertTrue(concurrencyMonitorProcessor.getAll().isEmpty());
    }
    
    public void testProcessOff() {
        Monitor m = new TransactionMonitor("T1");
        concurrencyMonitorProcessor.setEnabled(false);
        concurrencyMonitorProcessor.monitorStarted(m);

        concurrencyMonitorProcessor.process(m);
        
        assertTrue(concurrencyMonitorProcessor.getAll().isEmpty());
    }
    
    public void testProcessOne() {
        Monitor m = new TransactionMonitor("T1");
        concurrencyMonitorProcessor.setEnabled(true);
        concurrencyMonitorProcessor.monitorStarted(m);

        concurrencyMonitorProcessor.process(m);
        assertEquals(1, m.getAsInt("concurrencyCount"));
        
        Map<String, Integer> all = concurrencyMonitorProcessor.getAll();
        assertEquals(0, all.size());
    }
    
    public void testProcessTwo() {
        Monitor m1 = new TransactionMonitor("T1");
        Monitor m2 = new TransactionMonitor("T1");
        concurrencyMonitorProcessor.setEnabled(true);
        concurrencyMonitorProcessor.monitorStarted(m1);
        concurrencyMonitorProcessor.monitorStarted(m2);

        concurrencyMonitorProcessor.process(m1);
        assertEquals(2, m1.getAsInt("concurrencyCount"));
        concurrencyMonitorProcessor.process(m2);
        assertEquals(1, m2.getAsInt("concurrencyCount"));
        
        Map<String, Integer> all = concurrencyMonitorProcessor.getAll();
        assertEquals(0, all.size());
    }
    
    public void testProcessTooMany() {
        TestAppender appender = new TestAppender();
        Logger logger = Logger.getLogger(ConcurrencyMonitorProcessor.class.getName());
        logger.addAppender(appender);
        logger.setLevel(Level.ALL);

        Monitor m1 = new TransactionMonitor("T1");
        Monitor m2 = new TransactionMonitor("T1");
        Monitor m3 = new TransactionMonitor("T1");
        concurrencyMonitorProcessor.setEnabled(true);
        concurrencyMonitorProcessor.monitorStarted(m1);
        concurrencyMonitorProcessor.monitorStarted(m2);

        concurrencyMonitorProcessor.process(m1);
        assertEquals(2, m1.getAsInt("concurrencyCount"));
        concurrencyMonitorProcessor.process(m2);
        assertEquals(1, m2.getAsInt("concurrencyCount"));
        concurrencyMonitorProcessor.process(m3);
        assertFalse(m3.hasAttribute("concurrencyCount"));
        
        Map<String, Integer> all = concurrencyMonitorProcessor.getAll();
        assertEquals(0, all.size());
        
        assertEquals(1, appender.getEvents().size());
        LoggingEvent loggingEvent = appender.getEvents().get(0);
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertEquals("No count available for Monitor named T1", loggingEvent.getMessage());
    }
}
