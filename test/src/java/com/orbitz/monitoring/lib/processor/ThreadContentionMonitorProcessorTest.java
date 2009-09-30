package com.orbitz.monitoring.lib.processor;

import junit.framework.TestCase;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.lib.BaseMonitoringEngineManager;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * Unit tests for the ThreadContentionMonitorProcessor.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class ThreadContentionMonitorProcessorTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private ThreadContentionMonitorProcessor _processor = new ThreadContentionMonitorProcessor();

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _processor.setEnabled(true);
        MockMonitorProcessorFactory mockMonitorProcessorFactory =
                new MockMonitorProcessorFactory(_processor);
        MockDecomposer mockDecomposer = new MockDecomposer();
        BaseMonitoringEngineManager monitoringEngineManager =
                new BaseMonitoringEngineManager(mockMonitorProcessorFactory, mockDecomposer);
        monitoringEngineManager.startup();
    }

    protected void tearDown()
            throws Exception {
        super.tearDown();

        MonitoringEngine.getInstance().shutdown();
    }

    // ** TEST METHODS ********************************************************
    public void testStuff() {

        TransactionMonitor monitor = new TransactionMonitor("foo");
        final Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        monitor.done();
        assertEquals(1, monitor.getAsInt("waitedCount"));
        assertTrue(monitor.getAsLong("waitedTime")>=100);
        assertEquals(0, monitor.getAsInt("blockedCount"));
        assertEquals(0, monitor.getAsLong("blockedTime"));

        new Thread(new Runnable() {
            public void run() {
                synchronized (lock) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //make sure other thread has time to acquire lock
        try {
            Thread.currentThread().sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        monitor = new TransactionMonitor("foo");
        //should have to block here
        synchronized (lock) {
            //no wait
        }
        monitor.done();

        assertEquals(0, monitor.getAsInt("waitedCount"));
        assertEquals(0, monitor.getAsLong("waitedTime"));
        assertEquals(1, monitor.getAsInt("blockedCount"));
        assertTrue(monitor.getAsLong("blockedTime")>0);
    }
}
