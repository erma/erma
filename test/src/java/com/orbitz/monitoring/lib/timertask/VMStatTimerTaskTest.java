package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.lib.MonitoringEngineManager;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import junit.framework.TestCase;

/**
 * Unit tests for the VMStatTimerTask.
 *
 * <p>(c) 2000-06 Orbitz, LLC. All Rights Reserved.
 *
 * @author Matt O'Keefe
 */
public class VMStatTimerTaskTest extends TestCase {

    private VMStatTimerTask task;
    private MockMonitorProcessor processor;

    protected void setUp() throws Exception {
        super.setUp();

        task = new VMStatTimerTask();
        processor = new MockMonitorProcessor();

        MockMonitorProcessorFactory mockMonitorProcessorFactory =
                new MockMonitorProcessorFactory(processor);
        MockDecomposer mockDecomposer = new MockDecomposer();
        MonitoringEngineManager monitoringEngineManager =
                new MonitoringEngineManager(mockMonitorProcessorFactory, mockDecomposer);
        monitoringEngineManager.startup();
    }

    public void testVMStats() {
        task.run();
        Monitor[] monitors = processor.extractProcessObjects();
        boolean garbageCollectorStats = false;
        boolean threadStats = false;
        boolean memoryStats = false;
        boolean memoryPoolStats = false;
        for (Monitor monitor: monitors) {
            if ("GarbageCollectorStats".equals(monitor.get(Monitor.NAME))) {
                garbageCollectorStats = true;
                assertTrue("Didn't find a collectorName attribute", monitor.hasAttribute("collectorName"));
                assertTrue("Didn't find a collectionCount attribute", monitor.hasAttribute("collectionCount"));
                assertTrue("Didn't find a collectionTime attribute", monitor.hasAttribute("collectionTime"));
            } else if ("ThreadStats".equals(monitor.get(Monitor.NAME))) {
                threadStats = true;
                assertTrue("Didn't find a threadCount attribute", monitor.hasAttribute("threadCount"));
            } else if ("MemoryStats".equals(monitor.get(Monitor.NAME))) {
                memoryStats = true;
                assertTrue("Didn't find a heapMemoryUsage attribute", monitor.hasAttribute("heapMemoryUsage"));
                assertTrue("Didn't find a nonHeapMemoryUsage attribute", monitor.hasAttribute("nonHeapMemoryUsage"));
                assertTrue("Didn't find an objectPendingFinalizationCount attribute", monitor.hasAttribute("objectPendingFinalizationCount"));
            } else if ("MemoryPoolStats".equals(monitor.get(Monitor.NAME))) {
                memoryPoolStats = true;
                assertTrue("Didn't find a memoryPoolName attribute", monitor.hasAttribute("memoryPoolName"));
                assertTrue("Didn't find a memoryPoolType attribute", monitor.hasAttribute("memoryPoolType"));
                assertTrue("Didn't find a memoryPoolUsage attribute", monitor.hasAttribute("memoryPoolUsage"));
                if ("PS Old Gen".equals(monitor.getAsString("memoryPoolName"))) {
                    assertTrue("Didn't find a memoryPoolCollectionUsage attribute for 'PS Old Gen' pool",
                            monitor.hasAttribute("memoryPoolCollectionUsage"));
                }
            }
        }
        assertTrue("Didn't find a GarbageCollectorStats Monitor", garbageCollectorStats);
        assertTrue("Didn't find a ThreadStats Monitor", threadStats);
        assertTrue("Didn't find a MemoryStats Monitor", memoryStats);
        assertTrue("Didn't find a MemoryPoolStats Monitor", memoryPoolStats);
    }

    protected void tearDown() throws Exception {
        MonitoringEngine.getInstance().shutdown();
        processor = null;
        task = null;
        super.tearDown();
    }
}
