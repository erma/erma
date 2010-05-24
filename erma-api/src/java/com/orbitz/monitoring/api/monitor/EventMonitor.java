package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.MonitoringLevel;

import java.util.Map;

/**
 * A singular event that does not have a duration.
 *
 * @author Doug Barth
 */
public class EventMonitor extends AbstractMonitor {

    /**
     * Create a new event monitor with the provided
     * name.
     * 
     * @param name the name of the monitor
     */
    public EventMonitor(String name) {
        super(name);
    }

    /**
     * Create a new event monitor with the provided
     * name and monitoring level.
     *
     * @param name the name of the monitor
     * @param monitoringLevel the monitoring level
     */
    public EventMonitor(String name, MonitoringLevel monitoringLevel) {
        super(name, monitoringLevel);
    }

    /**
     * Create a new event monitor with the provided
     * name and inherited attributes.
     *
     * @param name the name of the monitor
     * @param inheritedAttributes the collection of inherited attributes
     */
    public EventMonitor(String name, Map inheritedAttributes) {
        super(name, inheritedAttributes);
    }

    /**
     * Create a new event monitor with the provided
     * name, monitoring level and inherited attributes.
     *
     * @param name the name of the monitor
     * @param monitoringLevel the monitoring level
     * @param inheritedAttributes the collection of inherited attributes
     */
    public EventMonitor(String name, MonitoringLevel monitoringLevel, Map inheritedAttributes) {
        super(name, monitoringLevel, inheritedAttributes);
    }

    /**
     * Fire this event monitor. Delegates to AbstractMonitor.process().
     */
    public void fire() {
        process();
    }
}
