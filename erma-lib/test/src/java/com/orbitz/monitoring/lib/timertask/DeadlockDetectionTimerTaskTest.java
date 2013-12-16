package com.orbitz.monitoring.lib.timertask;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import com.orbitz.monitoring.api.Monitor;

public class DeadlockDetectionTimerTaskTest extends TestCase {

    /**
     * This test will create deadlocks. Deadlocks would be much less of a
     * problem if they could be resolved programmatically. As they cannot the
     * order of these tests is very important. This means there is one master
     * test method with three sub methods.
     * 
     * @throws Exception
     */
    public void testItAll() throws Exception {
        while (ManagementFactory.getThreadMXBean().findMonitorDeadlockedThreads() != null) {
            fail("Started with thread already in deadlock: " + Arrays.toString(ManagementFactory.getThreadMXBean().findMonitorDeadlockedThreads()));
        }
        
        noDeadlock();
        createDeadlock();
        deadlock();
        deadlockMonitoringOff();
    }

    public void noDeadlock() throws InterruptedException {
        DeadlockDetectionTimerTask task = new DeadlockDetectionTimerTask();

        Monitor monitor = task.emitMonitors().iterator().next();
        assertNull(monitor);
    }
    
    public void createDeadlock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Object obj1 = new Object();
        Object obj2 = new Object();
        Deadlocker deadlock1 = new Deadlocker(obj1, obj2);
        Deadlocker deadlock2 = new Deadlocker(obj2, obj1);
        executor.execute(deadlock1);
        executor.execute(deadlock2);
        Thread.sleep(12);
    }

    public void deadlock() throws Exception {
        DeadlockDetectionTimerTask task = new DeadlockDetectionTimerTask();
        Monitor monitor = task.emitMonitors().iterator().next();
        assertNotNull(monitor);
        assertEquals("JvmStats", monitor.get(Monitor.NAME));
        assertEquals("Thread.Deadlock", monitor.get("type"));
        assertEquals(1, monitor.getAsInt("count"));
    }

    public void deadlockMonitoringOff() throws Exception {
        DeadlockDetectionTimerTask task = new DeadlockDetectionTimerTask();
        ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(false);

        Monitor monitor = task.emitMonitors().iterator().next();
        assertNull(monitor);
    }

    class Deadlocker implements Runnable {

        private Object a;
        private Object b;

        public Deadlocker(Object a, Object b) {
            super();
            this.a = a;
            this.b = b;
        }
        
        public void run() {
            synchronized (a) {
                try {
                    Thread.sleep(1);
                } catch (Exception doNothing) {}
                synchronized (b) {
                    try {
                        Thread.sleep(1);
                    } catch (Exception doNothing) {}
                }
            }
        }
    }

}
