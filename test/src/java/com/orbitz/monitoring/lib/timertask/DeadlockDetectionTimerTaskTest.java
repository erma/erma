package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.lib.BaseMonitoringEngineManager;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: mkemp
 * Date: Dec 2, 2008
 * Time: 10:56:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class DeadlockDetectionTimerTaskTest extends TestCase {

    private DeadlockDetectionTimerTask task;
    private MockMonitorProcessor processor;

    protected void setUp() throws Exception {
        super.setUp();

        task = new DeadlockDetectionTimerTask();
        processor = new MockMonitorProcessor();

        MockMonitorProcessorFactory mockMonitorProcessorFactory =
                new MockMonitorProcessorFactory(processor);
        MockDecomposer mockDecomposer = new MockDecomposer();
        BaseMonitoringEngineManager monitoringEngineManager =
                new BaseMonitoringEngineManager(mockMonitorProcessorFactory, mockDecomposer);
        monitoringEngineManager.startup();
    }

    public void testNoDeadlock() {
        task.run();
        Monitor[] monitors = processor.extractProcessObjects();
        for (Monitor monitor: monitors) {
            if("JvmStats".equals(monitor.get(Monitor.NAME))) {
                if("Thread.Deadlock".equals(monitor.get("type"))) {
                    fail();
                }
            }
        }
    }

    public void testDeadlock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Object obj1 = new Object();
        Object obj2 = new Object();
        executor.execute(new Deadlocker(obj1, obj2));
        executor.execute(new Deadlocker(obj2, obj1));
        Thread.sleep(12);
        task.run();
        Monitor[] monitors = processor.extractProcessObjects();
        boolean deadlockFound = false;
        for (Monitor monitor: monitors) {
            if("JvmStats".equals(monitor.get(Monitor.NAME))) {
                if("Thread.Deadlock".equals(monitor.get("type"))) {
                    assertEquals("Didn't find a count attribute", 1, monitor.getAsInt("count"));
                    deadlockFound = true;
                }
            }
        }
        if(!deadlockFound) {
            System.out.println("No deadlock was detected");
        }
        //assertTrue("No deadlock was detected", deadlockFound);
    }

    protected void tearDown() throws Exception {
        MonitoringEngine.getInstance().shutdown();
        processor = null;
        task = null;
        super.tearDown();
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
                    Thread.sleep(10);
                } catch (Exception doNothing) { }
                synchronized (b) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception doNothing) { }
                }
            }
        }
    }

}
