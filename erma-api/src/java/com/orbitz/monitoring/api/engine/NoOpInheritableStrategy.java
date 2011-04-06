package com.orbitz.monitoring.api.engine;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import java.util.Collections;
import java.util.Map;

/**
 * A strategy that doesn't provide any inheritance. This should be used to improve running time and
 * reduce memory usage when inheritance isn't needed.<br />
 * Created Apr 6, 2011
 * @author John VanDerpol
 */
public class NoOpInheritableStrategy implements InheritableStrategy {
    
    public int clearCurrentThread() {
        return 0;
    }
    
    public void compositeMonitorCompleted(final CompositeMonitor monitor) {
        // NOOP
    }
    
    public void compositeMonitorStarted(final CompositeMonitor compositeMonitor) {
        // NOOP
    }
    
    public CompositeMonitor getCompositeMonitorNamed(final String name) {
        return null;
    }
    
    public Map<?, ?> getInheritableAttributes() {
        return Collections.emptyMap();
    }
    
    public void processMonitorForCompositeMonitor(final Monitor monitor) {
        // NOOP
    }
    
    public void setInheritable(final CompositeMonitor monitor, final String key,
            final AttributeHolder original) {
        // NOOP
    }
    
    public void shutdown() {
        // NOOP
    }
    
    public void startup() {
        // NOOP
    }
}
