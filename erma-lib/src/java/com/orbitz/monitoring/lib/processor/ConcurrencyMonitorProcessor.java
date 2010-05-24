package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This is an implementation of the {@link com.orbitz.monitoring.api.MonitorProcessor} interface that
 * maintains counts of concurrently executing transactions.
 *
 * @author Matt O'Keefe
 */

public class ConcurrencyMonitorProcessor
        extends MonitorProcessorAdapter {

    private static final Logger log = Logger.getLogger(ConcurrencyMonitorProcessor.class);

    private static final Map map = new HashMap();

    private boolean isEnabled = true;

    // ** PUBLIC METHODS ******************************************************
    public void monitorStarted(Monitor monitor) {
        if (isEnabled && TransactionMonitor.class.isAssignableFrom(monitor.getClass())) {
            String name = monitor.getAsString(Attribute.NAME);
            synchronized(map) {
                Integer cnt = (Integer) map.get(name);
                if (cnt == null) {
                    map.put(name, new Integer(1));
                } else {
                    int count = cnt.intValue();
                    map.put(name, new Integer(count+1));
                }
            }
        }
    }

    public void process(Monitor monitor) {
        if (isEnabled && TransactionMonitor.class.isAssignableFrom(monitor.getClass())) {
            String name = monitor.getAsString(Attribute.NAME);
            synchronized(map) {
                Integer cnt = (Integer) map.get(name);
                if (cnt == null) {
                    log.warn("No count available for Monitor named "+name);
                } else {
                    int count = cnt.intValue();
                    map.put(name, new Integer(count-1));
                    monitor.set("concurrencyCount", count);
                }
            }
        }
    }

    public Map getAll() {
        Map all = new HashMap();
        synchronized (map) {
            all.putAll(map);
        }
        return all;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
