package com.orbitz.monitoring.lib.interceptor;

import com.orbitz.monitoring.api.monitor.TransactionMonitor;

import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author Ray Krueger
 */
public class MockTransactionMonitor extends TransactionMonitor {

    private String monitorName;
    private Class targetClass;
    private boolean succeeded;
    private boolean failed;
    private Throwable failedDueTo;
    private boolean done;

    public MockTransactionMonitor(String string) {
        super(string);
        this.monitorName = string;
    }

    public MockTransactionMonitor(String string, Map map) {
        super(string, map);
        this.monitorName = string;
    }

    public MockTransactionMonitor(Class aClass, String string) {
        super(aClass, string);
        this.monitorName = string;
        this.targetClass = aClass;
    }


    public String getMonitorName() {
        return monitorName;
    }

    public Class getTargetClass() {
        return targetClass;
    }


    public void succeeded() {
        super.succeeded();
        this.succeeded = true;
    }

    public void failed() {
        super.failed();
        this.failed = true;
    }

    public void failedDueTo(Throwable throwable) {
        super.failedDueTo(throwable);
        this.failedDueTo = throwable;
        this.failed = true;

    }

    public void done() {
        super.done();
        this.done = true;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public boolean isFailed() {
        return failed;
    }

    public Throwable getFailedDueTo() {
        return failedDueTo;
    }

    public boolean isDone() {
        return done;
    }
}
