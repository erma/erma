package com.orbitz.monitoring.lib.interceptor;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.EventMonitor;

/**
 * This is an implementation of the {@link java.lang.Thread.UncaughtExceptionHandler} interface that
 * fires an EventMonitor whenever a Thread terminates due to an unhandled Throwable.
 *
 * @author Matt O'Keefe
 */
public class ThreadDefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static class EventFiringThreadExceptionHandler implements ThreadExceptionHandler {

        @Override
        public Monitor handleException(Thread thread, Throwable throwable) {
            EventMonitor eventMonitor = new EventMonitor("ThreadTerminationDueToUncaughtThrowable");
            eventMonitor.set("threadName", thread.getName());
            eventMonitor.set("threadClass", thread.getClass().getName());
            eventMonitor.set("stackTrace", ExceptionUtils.getStackTrace(throwable));
            eventMonitor.fire();
            return eventMonitor;
        }
    }

    public static interface ThreadExceptionHandler {
        Monitor handleException(Thread thread, Throwable throwable);
    }

    private ThreadExceptionHandler handler;
    
    public ThreadDefaultUncaughtExceptionHandler() {
        this(new EventFiringThreadExceptionHandler());
    }
    
    public ThreadDefaultUncaughtExceptionHandler(ThreadExceptionHandler handler) {
        this.handler = handler;
    }

    /**
     * Method invoked when the given thread terminates due to the
     * given uncaught Throwable.
     * <p>Any exception thrown by this method will be ignored by the
     * Java Virtual Machine.
     *
     * @param thread the Thread
     * @param throwable the Throwable
     */
    public void uncaughtException(Thread thread, Throwable throwable) {
        handler.handleException(thread, throwable);
    }
    
    public ThreadExceptionHandler getHandler() {
        return handler;
    }
}
