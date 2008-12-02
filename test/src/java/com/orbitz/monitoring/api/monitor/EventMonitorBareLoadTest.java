package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;

public class EventMonitorBareLoadTest extends TestCase {
    private boolean shouldRun = false;

    private long iterations = 10000;
    private int users = 10;
    private long startTime;
    private long endTime;

    public void setUp() {
        MonitoringEngine mEngine = MonitoringEngine.getInstance();
        mEngine.setProcessorFactory(
                new MockMonitorProcessorFactory(new MonitorProcessor[0]));
        mEngine.startup();
    }

    private void eventMonitorLoad() {
        for (int i = 0; i < iterations; i++) {
            new EventMonitor("foo").fire();
        }
    }

    public void testEventMonitorLoad() {
        if(shouldRun) {
            startTime = System.currentTimeMillis();
            eventMonitorLoad();
            endTime = System.currentTimeMillis();

            logResults(singleThreaded("EventMonitor"), iterations);
        }
    }

    public void testEventMonitorParallelLoad() throws Exception {
        if(shouldRun) {
            Thread threads[] = new Thread[users];

            for (int i = 0; i < users; i++) {
                threads[i] = new Thread(new Runnable() {
                    public void run() {
                        eventMonitorLoad();
                    }
                });
            }

            startTime = System.currentTimeMillis();
            for (int i = 0; i < users; i++) {
                threads[i].start();
            }
            for (int i = 0; i < users; i++) {
                threads[i].join();
            }
            endTime = System.currentTimeMillis();

            logResults(parallel("EventMonitor"), iterations);
        }
    }

    private void transactionMonitorLoad() {
        for (int i = 0; i < iterations; i++) {
            new TransactionMonitor("foo").done();
        }
    }

    public void testTransactionMonitorLoad() {
        if(shouldRun) {
            startTime = System.currentTimeMillis();
            transactionMonitorLoad();
            endTime = System.currentTimeMillis();

            logResults(singleThreaded("TransactionMonitor"), iterations);
        }
    }

    public void testTransactionMonitorParallelLoad() throws Exception {
        if(shouldRun) {
            Thread threads[] = new Thread[users];

            for (int i = 0; i < users; i++) {
                threads[i] = new Thread(new Runnable() {
                    public void run() {
                        transactionMonitorLoad();
                    }
                });
            }

            startTime = System.currentTimeMillis();
            for (int i = 0; i < users; i++) {
                threads[i].start();
            }
            for (int i = 0; i < users; i++) {
                threads[i].join();
            }
            endTime = System.currentTimeMillis();

            logResults(parallel("TransactionMonitor"), iterations);
        }
    }


    private void transactionMonitorParentChildLoad() {
        for (int i = 0; i < iterations; i++) {
            TransactionMonitor parent = new TransactionMonitor("parent");
            TransactionMonitor child = new TransactionMonitor("child");
            child.done();
            parent.done();
        }
    }

    public void testTransactionMonitorParentChildLoad() {
        if(shouldRun) {
            startTime = System.currentTimeMillis();
            transactionMonitorParentChildLoad();
            endTime = System.currentTimeMillis();

            logResults(singleThreaded("TransactionMonitor parent->child"), 2 * iterations);
        }
    }

    public void testTransactionMonitorParentChildParallelLoad() throws Exception {
        if(shouldRun) {
            Thread threads[] = new Thread[users];

            for (int i = 0; i < users; i++) {
                threads[i] = new Thread(new Runnable() {
                    public void run() {
                        transactionMonitorParentChildLoad();
                    }
                });
            }

            startTime = System.currentTimeMillis();
            for (int i = 0; i < users; i++) {
                threads[i].start();
            }
            for (int i = 0; i < users; i++) {
                threads[i].join();
            }
            endTime = System.currentTimeMillis();

            logResults(parallel("TransactionMonitor parent->child"), iterations);
        }
    }

    private String singleThreaded(String name) {
        return name + " - 1 user, " + iterations + " iterations";
    }

    private String parallel(String name) {
        return name + " - " + users + " users, " + iterations + " iterations per user";
    }

    private void logResults(String message, long divisor) {
        System.out.println(message + ", avg time per monitor: " +
                ((float) (endTime - startTime) / divisor) + " ms");
    }
}
