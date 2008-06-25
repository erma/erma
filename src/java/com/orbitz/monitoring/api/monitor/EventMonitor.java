package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.MonitoringLevel;

import java.util.Map;

/**
 * A singular event that does not have a duration.
 *
 * @author Doug Barth
 */
public class EventMonitor extends AbstractMonitor {
    // ** PRIVATE DATA ********************************************************

    // ** CONSTRUCTORS ********************************************************
    public EventMonitor(String name) {
        super(name);
    }

    public EventMonitor(String name, Map inheritedAttributes) {
        super(name, inheritedAttributes);
    }

    public EventMonitor(String name, MonitoringLevel monitoringLevel) {
        super(name, monitoringLevel);
    }

    public EventMonitor(String name, MonitoringLevel monitoringLevel, Map inheritedAttributes) {
        super(name, monitoringLevel, inheritedAttributes);
    }

    // ** PUBLIC METHODS ******************************************************
    public void fire() {
        process();
    }
}
