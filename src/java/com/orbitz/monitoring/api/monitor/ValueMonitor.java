package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.Attribute;

/**
 * Monitor that represents a single value of interest. 
 *
 * @author Mohammed Abdulghani
 */
public class ValueMonitor extends AbstractMonitor{

    /**
     * Creates a new value monitor using the provided name
     * and value.
     *
     * @param name the name of the monitor
     * @param value the value
     */
    public ValueMonitor(String name, double value) {
        this(name, value, MonitoringLevel.INFO);
    }

    /**
     * Creates a new value monitor using the provided name,
     * value and monitoring level.
     *
     * @param name the name of the monitor
     * @param value the value
     * @param monitoringLevel the monitoring level
     */
    public ValueMonitor(String name, double value, MonitoringLevel monitoringLevel) {
		super(name, monitoringLevel);
		set(Attribute.VALUE, value).lock();
	}

    /**
     * Fire this value monitor. Delegates to AbstractMonitor.process().
     */
    public void fire() {
        process();
	}
	
}
