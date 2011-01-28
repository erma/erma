package com.orbitz.monitoring.api.engine;

import java.util.Collections;
import java.util.Map;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AttributeHolder;

public class NullInheritableStrategy implements InheritableStrategy {

    public int clearCurrentThread() {
        return 0;
    }

    public void compositeMonitorCompleted(CompositeMonitor monitor) {
        // NOOP
    }

    public void compositeMonitorStarted(CompositeMonitor compositeMonitor) {
        // NOOP
    }

    public CompositeMonitor getCompositeMonitorNamed(String name) {
        return null;
    }

    public Map<?, ?> getInheritableAttributes() {
        return Collections.emptyMap();
    }

    public void processMonitorForCompositeMonitor(Monitor monitor) {
        // NOOP
    }

    public void setInheritable(CompositeMonitor monitor, String key, AttributeHolder original) {
        // NOOP
    }

    public void shutdown() {
        // NOOP
    }

    public void startup() {
        // NOOP
    }
    
}
