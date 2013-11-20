package com.orbitz.monitoring.lib.interceptor;

import junit.framework.TestCase;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AbstractMonitor;
import com.orbitz.monitoring.lib.interceptor.ThreadDefaultUncaughtExceptionHandler.EventFiringThreadExceptionHandler;
import com.orbitz.monitoring.lib.interceptor.ThreadDefaultUncaughtExceptionHandler.ThreadExceptionHandler;

/**
 * Unit tests for the ThreadDefaultUncaughtExceptionHandler.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class ThreadDefaultUncaughtExceptionHandlerTest extends TestCase {
    public void testThreadTerminationDueToUncaughtThrowable() {
        final Thread thisThread = Thread.currentThread();
        final Throwable expected = new RuntimeException();
        final Monitor monitor = new AbstractMonitor() {};
        ThreadDefaultUncaughtExceptionHandler handler = new ThreadDefaultUncaughtExceptionHandler(new ThreadExceptionHandler(){

            @Override
            public Monitor handleException(Thread thread, Throwable throwable) {
                assertEquals(thisThread, thread);
                assertEquals(expected, throwable);
                monitor.set("handled", true);
                return monitor;
            }});
        handler.uncaughtException(thisThread, expected);
        assertTrue(monitor.hasAttribute("handled"));
    }
    
    public void testDefaultHandler() {
        final Thread thread = Thread.currentThread();
        final Throwable throwable = new RuntimeException();
        ThreadExceptionHandler handler = new ThreadDefaultUncaughtExceptionHandler().getHandler();
        Monitor monitor = handler.handleException(thread, throwable);
        assertEquals("Didn't find a ThreadTerminationDueToUncaughtThrowable Monitor",
        monitor.get(Monitor.NAME), "ThreadTerminationDueToUncaughtThrowable");
        assertTrue("Didn't find a threadName attribute", monitor.hasAttribute("threadName"));
        assertTrue("Didn't find a threadClass attribute", monitor.hasAttribute("threadClass"));
        assertTrue("Didn't find a stackTrace attribute", monitor.hasAttribute("stackTrace"));
    }

}
