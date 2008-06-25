package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.MonitoringLevel;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.MemoryPoolMXBean;
import java.util.TimerTask;

/**
 * VMStatTimerTask uses jdk5 MXBeans access garbage collection and thread stats.  EventMonitors named
 * GarbageCollectorStats and ThreadStats are fired containing the stats as attributes.
 *
 * @since 3.5
 *
 * @author Matt O'Keefe
 */
public class VMStatTimerTask extends TimerTask {

    private static final int BASE_2_EXP_MEGABYTE = 20; // 2 ^ 20 = 1024 * 1024 = 1048576

    /**
     * The action to be performed by this timer task.
     */
    public void run() {
        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            EventMonitor gcMonitor = new EventMonitor("GarbageCollectorStats", MonitoringLevel.ESSENTIAL);
            gcMonitor.set("collectorName", garbageCollectorMXBean.getName());
            gcMonitor.set("collectionCount", garbageCollectorMXBean.getCollectionCount());
            gcMonitor.set("collectionTime", garbageCollectorMXBean.getCollectionTime());
            gcMonitor.fire();
        }

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int count = threadBean.getThreadCount();
        EventMonitor threadCountMonitor = new EventMonitor("ThreadStats", MonitoringLevel.ESSENTIAL);
        threadCountMonitor.set("threadCount", count);
        threadCountMonitor.fire();

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memoryMXBean.getHeapMemoryUsage();
        long used = usage.getUsed() >> BASE_2_EXP_MEGABYTE; // MB used
        EventMonitor eventMonitor = new EventMonitor("MemoryStats", MonitoringLevel.ESSENTIAL);
        eventMonitor.set("heapMemoryUsage", used);
        usage = memoryMXBean.getNonHeapMemoryUsage();
        used = usage.getUsed() >> BASE_2_EXP_MEGABYTE; // MB used
        eventMonitor.set("nonHeapMemoryUsage", used);
        int objectPendingFinalizationCount = memoryMXBean.getObjectPendingFinalizationCount();
        eventMonitor.set("objectPendingFinalizationCount", objectPendingFinalizationCount);
        eventMonitor.fire();

        for(MemoryPoolMXBean memoryPoolMXBean: ManagementFactory.getMemoryPoolMXBeans()) {
            EventMonitor mpMonitor = new EventMonitor("MemoryPoolStats", MonitoringLevel.ESSENTIAL);
            mpMonitor.set("memoryPoolName", memoryPoolMXBean.getName());
            mpMonitor.set("memoryPoolType", memoryPoolMXBean.getType().toString());
            usage = memoryPoolMXBean.getUsage();
            used = usage.getUsed() >> BASE_2_EXP_MEGABYTE; // MB used
            mpMonitor.set("memoryPoolUsage", used);
            usage = memoryPoolMXBean.getCollectionUsage();
            if (usage != null) {
                used = usage.getUsed() >> BASE_2_EXP_MEGABYTE; // MB used
                mpMonitor.set("memoryPoolCollectionUsage", used);
            }
            mpMonitor.fire();
        }
    }
}
