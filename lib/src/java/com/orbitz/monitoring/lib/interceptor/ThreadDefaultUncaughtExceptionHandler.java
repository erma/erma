package com.orbitz.monitoring.lib.interceptor;

import com.orbitz.monitoring.api.monitor.EventMonitor;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * This is an implementation of the {@link java.lang.Thread.UncaughtExceptionHandler} interface that
 * fires an EventMonitor whenever a Thread terminates due to an unhandled Throwable.
 *
 * The event that is fired will likely be routed to the SOC as an alarm.
 *
 * @author Matt O'Keefe
 */
public class ThreadDefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

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
        EventMonitor eventMonitor = new EventMonitor("ThreadTerminationDueToUncaughtThrowable");
        eventMonitor.set("threadName", thread.getName());
        eventMonitor.set("threadClass", thread.getClass().getName());
        eventMonitor.set("stackTrace", getStackTrace(throwable));
        eventMonitor.fire();
    }

    private String getStackTrace(Throwable t) {
        StringWriter out = new StringWriter();
        t.printStackTrace(new PrintWriter(out));
        return out.toString();
    }
}
