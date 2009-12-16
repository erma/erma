package com.orbitz.monitoring.api.engine;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.InheritableStrategy;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 *
 */
public class MapBasedInheritableStrategy implements InheritableStrategy {

    private static final Logger log = Logger.getLogger(MapBasedInheritableStrategy.class);

    private final ConcurrentMap threadBasedMap = new ConcurrentHashMap();

    public int clearCurrentThread() {
        Map map = (Map) threadBasedMap.remove(Thread.currentThread());
        return (map == null) ? 0 : map.size();
    }

    public void compositeMonitorCompleted(CompositeMonitor monitor) {
        if(threadBasedMap.containsKey(Thread.currentThread())) {
            Map map = (Map) threadBasedMap.get(Thread.currentThread());
            if(map != null && map.containsKey(monitor)) {
                map.remove(monitor);
            }
        }
    }

    public void compositeMonitorStarted(CompositeMonitor monitor) {
        if(!threadBasedMap.containsKey(Thread.currentThread())) {
            threadBasedMap.put(Thread.currentThread(), new HashMap());
        }
    }

    public CompositeMonitor getCompositeMonitorNamed(String name) throws IllegalArgumentException {
        throw new UnsupportedOperationException("MapBasedInheritableStrategy.getCompositeMonitorNamed()");
    }

    public Map getInheritableAttributes() {
        Map inheritableAttributes = new HashMap();
        Map map = (Map) threadBasedMap.get(Thread.currentThread());
        if(map != null) {
            for(Iterator it = map.values().iterator(); it.hasNext(); ) {
                inheritableAttributes.putAll((Map) it.next());
            }
        }
        return inheritableAttributes;
    }

    public void processMonitorForCompositeMonitor(Monitor monitor) {
        // no-op
    }

    public void setInheritable(CompositeMonitor monitor, String key, AttributeHolder original) {
        Map inheritableAttributes = getInheritableAttributes();
        if(!inheritableAttributes.containsKey(key) && original != null) {
            Map map = (Map) threadBasedMap.get(Thread.currentThread());
            if(!map.containsKey(monitor)) {
                map.put(monitor, new HashMap());
            }
            AttributeHolder copy = new AttributeHolder(original.getValue());
            if(original.isSerializable()) {
                copy.serializable();
            }
            Map monitorMap = (Map) map.get(monitor);
            monitorMap.put(key, copy);
        } else {
            if(log.isDebugEnabled()) {
                AttributeHolder holder = (AttributeHolder) inheritableAttributes.get(key);
                log.debug("Attempted to re-add " + key + " with new value [" +
                        original.getValue() + "] to inheritableMap; old value is [" +
                        holder.getValue() + "]");
            }
        }
    }

    public void shutdown() {
        threadBasedMap.clear();
    }

    public void startup() {
        threadBasedMap.clear();
    }
}
