package com.orbitz.monitoring.lib.processor.statsd;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.statsd.StatsdClient;

/**
 * Sends ERMA monitor data to a statsd server
 * 
 * @author orawlings
 *
 */
public class StatsdMonitorProcessor implements MonitorProcessor {
    
    private StatsdClient _statsdClient;
    private String _name;
    
    public StatsdMonitorProcessor(StatsdClient statsdClient) {
        this._statsdClient = statsdClient;
    }

    public void startup() {
        // no-op
    }

    public void shutdown() {
        // no-op
    }

    public void monitorCreated(Monitor monitor) {
        // no-op
    }

    public void monitorStarted(Monitor monitor) {
        // no-op
    }

    public void process(Monitor monitor) {
        if (monitor == null) {
            return;
        }

        if (monitor.hasAttribute(Attribute.LATENCY)) {
            _statsdClient.timing(monitor.getAsString(Attribute.NAME), monitor.getAsInt(Attribute.LATENCY));
        } else if (monitor.hasAttribute(Attribute.VALUE)) {
            _statsdClient.gauge(monitor.getAsString(Attribute.NAME), (int) monitor.getAsInt(Attribute.VALUE));
        } else {
            _statsdClient.increment(monitor.getAsString(Attribute.NAME));
        }
        
        if (monitor.getAsBoolean(Attribute.FAILED, false)) {
            _statsdClient.increment(monitor.getAsString(Attribute.NAME) + ".failed");
        }
    }

    public String getName() {
        return _name;
    }
    
    public void setName(String name) {
        this._name = name;
    }
    
}
