package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.EventMonitor;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * VMStatTimerTask uses jdk5 MXBeans access garbage collection and thread stats.  EventMonitors named
 * GarbageCollectorStats and ThreadStats are fired containing the stats as attributes.
 *
 * @since 3.5
 *
 * @author Matt O'Keefe
 */
public class VMStatTimerTask extends MonitorEmittingTimerTask {

    // 2 ^ 20 = 1024 * 1024 = 1048576
    private static final int BASE_2_EXP_MEGABYTE = 20; 

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
    public Collection<Monitor> emitMonitors() {
        Set<Monitor> monitors = new HashSet<Monitor>();
        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            String name = garbageCollectorMXBean.getName();
            long count = setGCAndGetDelta(name + ".count", garbageCollectorMXBean.getCollectionCount());
            long time = setGCAndGetDelta(name + ".time", garbageCollectorMXBean.getCollectionTime());
            monitors.add(fireJvmStat("GarbageCollector." + name, count, time));
        }

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        monitors.add(fireJvmStat("Thread", (long) threadBean.getThreadCount()));

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memoryMXBean.getHeapMemoryUsage();
        monitors.add(fireJvmStat("Memory.Heap.memoryUsage", toMegaBytes(usage.getUsed()), null, getUsedPercentage(usage)));
        
        usage = memoryMXBean.getNonHeapMemoryUsage();
        monitors.add(fireJvmStat("Memory.NonHeap.memoryUsage", toMegaBytes(usage.getUsed()), null, getUsedPercentage(usage)));
        monitors.add(fireJvmStat("Memory.objectPendingFinalization", (long) memoryMXBean.getObjectPendingFinalizationCount()));

        for(MemoryPoolMXBean memoryPoolMXBean: ManagementFactory.getMemoryPoolMXBeans()) {
            String type = (MemoryType.HEAP == memoryPoolMXBean.getType()) ? "Heap" : "NonHeap";
            String name = "Memory." + type + ".Pool." + memoryPoolMXBean.getName();
            usage = memoryPoolMXBean.getUsage();
            monitors.add(fireJvmStat(name + ".memoryUsage", toMegaBytes(usage.getUsed()), null, getUsedPercentage(usage)));

            usage = memoryPoolMXBean.getCollectionUsage();
            if (usage != null) {
                monitors.add(fireJvmStat(name + ".memoryCollectionUsage", toMegaBytes(usage.getUsed())));
            }
        }
        return monitors;
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
    private EventMonitor fireJvmStat(String type, Long count) {
        return fireJvmStat(type, count, null, null);
    }
    
    private EventMonitor fireJvmStat(String type, Long count, Long time) {
        return fireJvmStat(type, count, time, null);
    }

    private EventMonitor fireJvmStat(String type, Long count, Long time, Double percent) {
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
        return monitor;
    }
}
