package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.MonitoringEngine;

/**
 * Monitor that represents a single value of interest.  Typical usage:
 * <p>
 * new ValueMonitor(name, value).fire();
 * </p>
 *
 * NOTE:  This monitor is tuned for usage in potentially high volume monitoring use cases, such
 * as monitoring a value inside a tight loop.  By default the monitor is optimized to skip these
 * steps of monitor construction:
 * <ol>
 * <li>inheritable attributes</li>
 * <li>the "monitorCreated" lifecycle step</li>
 * </ol>
 *
 * @author Mohammed Abdulghani
 */
public class ValueMonitor extends AbstractMonitor {

    /**
     * Creates a new value monitor using the provided name
     * and value.  Does NOT support inheritable attributes.
     *
     * @param name the name of the monitor
     * @param value the value
     */
    public ValueMonitor(String name, double value) {
        this(name, value, false);
    }

    /**
     * Creates a new value monitor using the provided name, value and monitoring level.
     * Does NOT support inheritable attributes.
     *
     * @param name the name of the monitor
     * @param value the value
     * @param monitoringLevel the monitoring level
     */
    public ValueMonitor(String name, double value, MonitoringLevel monitoringLevel) {
        this(name, value, false, monitoringLevel);
    }

    /**
     * Optionally allows for inheritable attributes.
     *
     * @param name the name of the monitor
     * @param value the value
     * @param includeInheritables set to true if this monitor should support inheritables
     */
    public ValueMonitor(String name, double value, boolean includeInheritables) {
        this(name, value, includeInheritables, MonitoringLevel.INFO);
    }

    /**
     * Optionally allows for inheritable attributes.
     *
     * @param name the name of the monitor
     * @param value the value
     * @param includeInheritables set to true if this monitor should support inheritables
     * @param monitoringLevel monitoring level
     */
    public ValueMonitor(String name, double value, boolean includeInheritables,
                        MonitoringLevel monitoringLevel) {
        // note we are calling this specific constructor on AbstractMonitor to avoid
        // the call to init() on AbstractMonitor
        super();

        this.monitoringLevel = monitoringLevel;

        // perform standard monitor initialization optionally ommitting inheritable
        // attributes - this is a performance optimization targeted towards a potentially
        // costly step in monitor construction (inheritable attributes)
        MonitoringEngine.getInstance().initMonitor(this, includeInheritables);

        // also intentionally skip the created lifecycle step as this is not a
        // meaningful event/unnecessary overhead for ValueMonitor
        //MonitoringEngine.getInstance().monitorCreated(this);

        set(Attribute.NAME, name).lock();
        set(Attribute.VALUE, value).lock();
    }

    /**
     * Fire this value monitor. Delegates to AbstractMonitor.process().
     */
    public void fire() {
        process();
	}
}
