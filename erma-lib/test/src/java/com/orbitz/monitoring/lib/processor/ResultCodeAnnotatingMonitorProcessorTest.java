package com.orbitz.monitoring.lib.processor;

import junit.framework.TestCase;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AbstractCompositeMonitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;

/**
 * Unit tests for the ResultCodeAnnotatingMonitorProcessor.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class ResultCodeAnnotatingMonitorProcessorTest extends TestCase {
    private ResultCodeAnnotatingMonitorProcessor processor = new ResultCodeAnnotatingMonitorProcessor();

    public void testNotCompositeMonitor() {
        Monitor monitor = new AbstractMonitor() {};
        processor.process(monitor);
        assertEquals(0, monitor.getAll().size());
    }
    
    public void testCompositeMonitorWithResult() {
        Monitor monitor = new AbstractCompositeMonitor("") {};
        monitor.set("resultCode", "randomString");
        int keysCountBefore = monitor.getAll().size(); 
        processor.process(monitor);
        //Nothing Added
        assertEquals(keysCountBefore, monitor.getAll().size());
        //Nothing Changed
        assertEquals("randomString", monitor.getAsString("resultCode"));
    }
    
    public void testCompositeMonitorWithoutResult() {
        Monitor monitor = new AbstractCompositeMonitor("") {};
        int keysCountBefore = monitor.getAll().size(); 
        processor.process(monitor);
        //Result Code added
        assertEquals(keysCountBefore + 1, monitor.getAll().size());
        //Result Code is success
        assertEquals("success", monitor.getAsString("resultCode"));
    }
    
    public void testCompositeMonitorWithSingleDepthFailure() {
        Monitor monitor = new AbstractCompositeMonitor("") {};
        Throwable exception = new Throwable();
        monitor.set("failureThrowable", exception);
        int keysCountBefore = monitor.getAll().size(); 
        processor.process(monitor);
        //Result Code added
        assertEquals(keysCountBefore + 1, monitor.getAll().size());
        //Result code is name of throwable
        assertEquals(exception.getClass().getName(), monitor.getAsString("resultCode"));
    }
    
    public void testCompositeMonitorWithMultiDepthFailure() {
        Monitor monitor = new AbstractCompositeMonitor("") {};
        Throwable inner = new Throwable();
        Throwable outer = new Throwable(inner);
        monitor.set("failureThrowable", outer);
        int keysCountBefore = monitor.getAll().size(); 
        processor.process(monitor);
        //Result Code added
        assertEquals(keysCountBefore + 1, monitor.getAll().size());
        //Result code is name of throwable
        assertEquals(inner.getClass().getName(), monitor.getAsString("resultCode"));
    }
}
