package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.EventMonitor;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

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

    private final Map<String,AtomicLong> gc;

    public VMStatTimerTask() {
        super();
        gc = new HashMap<String,AtomicLong>(8);
        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            final String name = garbageCollectorMXBean.getName();
            gc.put(name + ".count", new AtomicLong());
            gc.put(name + ".time", new AtomicLong());
        }
    }

    /**
     * The action to be performed by this timer task.
     */
    public void run() {
        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            final String name = garbageCollectorMXBean.getName();
            final long count = setGCAndGetDelta(name + ".count", garbageCollectorMXBean.getCollectionCount());
            final long time = setGCAndGetDelta(name + ".time", garbageCollectorMXBean.getCollectionTime());
            fireJvmStat("GarbageCollector." + name, count, time);
        }

        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        fireJvmStat("Thread", (long) threadBean.getThreadCount());

        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memoryMXBean.getHeapMemoryUsage();
        fireJvmStat("Memory.Heap.memoryUsage", toMegaBytes(usage.getUsed()), null, getUsedPercentage(usage));
        usage = memoryMXBean.getNonHeapMemoryUsage();
        fireJvmStat("Memory.NonHeap.memoryUsage", toMegaBytes(usage.getUsed()), null, getUsedPercentage(usage));
        fireJvmStat("Memory.objectPendingFinalization", (long) memoryMXBean.getObjectPendingFinalizationCount());

        for(MemoryPoolMXBean memoryPoolMXBean: ManagementFactory.getMemoryPoolMXBeans()) {
            final String type = (MemoryType.HEAP == memoryPoolMXBean.getType()) ? "Heap" : "NonHeap";
            final String name = "Memory." + type + ".Pool." + memoryPoolMXBean.getName();
            usage = memoryPoolMXBean.getUsage();
            fireJvmStat(name + ".memoryUsage", toMegaBytes(usage.getUsed()), null, getUsedPercentage(usage));

            usage = memoryPoolMXBean.getCollectionUsage();
            if (usage != null) {
                fireJvmStat(name + ".memoryCollectionUsage", toMegaBytes(usage.getUsed()));
            }
        }
    }

    // calculates a gc delta value
    private long setGCAndGetDelta(final String key, final long newValue) {
        final long oldValue = gc.get(key).getAndSet(newValue);
        return newValue - oldValue;
    }

    // convert a number from bytes to megabytes
    private long toMegaBytes(final long number) {
        return number >> BASE_2_EXP_MEGABYTE;
    }

    // get the percentage used
    private double getUsedPercentage(MemoryUsage usage) {
        return (100.0 * usage.getUsed()) / usage.getMax();
    }

    // fire JvmStats monitor; sets the name, type, count, time and percent
    private void fireJvmStat(final String type, final Long count) {
        fireJvmStat(type, count, null, null);
    }
    
    private void fireJvmStat(final String type, final Long count, final Long time) {
        fireJvmStat(type, count, time, null);
    }

    private void fireJvmStat(final String type, final Long count, final Long time, final Double percent) {
        final EventMonitor monitor = new EventMonitor("JvmStats", MonitoringLevel.ESSENTIAL);
        monitor.set("type", type);
        if(count != null) {
            monitor.set("count", count);
        }
        if(time != null) {
            monitor.set("time", time);
        }
        if(percent != null) {
            monitor.set("percent", percent);
        }
        monitor.fire();
    }
}
