package com.orbitz.monitoring.api;

import java.util.List;

/**
 * An interface that objects can implement if they support attaching
 * {@link MonitorProcessor} instances to themselves.
 *
 * @author Doug Barth
 */
public interface MonitorProcessorAttachable {
    /**
     * Adds the supplied MonitorProcessor instance to this object.
     *
     * @param processor the MonitorProcessor to be attached
     */
    public void addMonitorProcessor(MonitorProcessor processor);

    /**
     * Get the MonitorProcessors attached to this object.
     *
     * @return the processors attached to this object
     */
    public List getMonitorProcessors();
}
