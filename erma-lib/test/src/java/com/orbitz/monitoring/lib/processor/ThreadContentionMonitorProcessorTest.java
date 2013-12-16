package com.orbitz.monitoring.lib.processor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * Unit tests for the ThreadContentionMonitorProcessor.
 * @author Matt O'Keefe
 */
public class ThreadContentionMonitorProcessorTest extends TestCase {
    private ThreadContentionMonitorProcessor _processor = new ThreadContentionMonitorProcessor();

    protected void setUp() throws Exception {
        super.setUp();
        _processor.setEnabled(true);
    }
    
    public void testDisabled() {
        _processor.setEnabled(false);
        TransactionMonitor monitor = new TransactionMonitor("");
        Map<String, Object> attrStart = new HashMap<String, Object>(monitor.getAll());
        _processor.monitorCreated(monitor);
        assertEquals(attrStart.size(), monitor.getAll().size());
        _processor.monitorStarted(monitor);
        assertEquals(attrStart.size(), monitor.getAll().size());
        _processor.process(monitor);
        assertEquals(attrStart.size(), monitor.getAll().size());
    }
    
    public void testNotTransactionMonitor() {
        Monitor monitor = new AbstractMonitor(){};
        Map<String, Object> attrStart = new HashMap<String, Object>(monitor.getAll());
        _processor.monitorCreated(monitor);
        assertEquals(attrStart.size(), monitor.getAll().size());
        _processor.monitorStarted(monitor);
        assertEquals(attrStart.size(), monitor.getAll().size());
        _processor.process(monitor);
        assertEquals(attrStart.size(), monitor.getAll().size());
    }
    
    public void testMonitorCreated() {
        TransactionMonitor monitor = new TransactionMonitor("") {};
        Map<String, Object> attrStart = new HashMap<String, Object>(monitor.getAll());
        _processor.monitorCreated(monitor);
        assertEquals(attrStart.size(), monitor.getAll().size());
    }
    
    public void testMonitorStarted() {
        TransactionMonitor monitor = new TransactionMonitor("") {};
        _processor.monitorStarted(monitor);
        
        ThreadMXBean tmxbean = ManagementFactory.getThreadMXBean();
        long id = Thread.currentThread().getId();
        ThreadInfo threadInfo = tmxbean.getThreadInfo(id);
        assertEquals(threadInfo.getBlockedCount(), monitor.get("startBlockedCount"));
        assertEquals(threadInfo.getBlockedTime(), monitor.get("startBlockedTime"));
        assertEquals(threadInfo.getWaitedCount(), monitor.get("startWaitedCount"));
        assertEquals(threadInfo.getWaitedTime(), monitor.get("startWaitedTime"));
    }

    public void testProccessWithoutStartHasNoImpact() {
        TransactionMonitor monitor = new TransactionMonitor("") {};
        Map<String, Object> attrStart = new HashMap<String, Object>(monitor.getAll());
        _processor.process(monitor);
        assertEquals(attrStart.size(), monitor.getAll().size());
    }

    public void testProcessWithWait() {
        ThreadMXBean tmxbean = ManagementFactory.getThreadMXBean();
        long id = Thread.currentThread().getId();
        ThreadInfo threadInfo = tmxbean.getThreadInfo(id);

        TransactionMonitor monitor = new TransactionMonitor("foo");
        monitor.set("startBlockedCount", threadInfo.getBlockedCount() - 1);
        monitor.set("startBlockedTime", threadInfo.getBlockedTime() - 1);
        monitor.set("startWaitedCount", threadInfo.getWaitedCount() - 1);
        monitor.set("startWaitedTime", threadInfo.getWaitedTime() - 1);

        _processor.process(monitor);
        
        assertEquals(1, monitor.getAsInt("waitedCount"));
        assertEquals(1, monitor.getAsInt("waitedTime"));
        assertEquals(1, monitor.getAsInt("blockedCount"));
        assertEquals(1, monitor.getAsLong("blockedTime"));
    }
}
