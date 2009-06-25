package com.orbitz.monitoring.api;

import com.orbitz.monitoring.api.monitor.AttributeHolder;

import java.util.Map;

/**
 * Represents how the MonitoringEngine handles inheritable
 * attributes. Not all methods may be supported by all
 * inheritable strategies.
 */
public interface InheritableStrategy {

    int clearCurrentThread();

    /**
     * This method should be called by all CompositeMonitor implementations
     * before they call process().
     *
     * @param monitor the monitor that is completed
     */
    void compositeMonitorCompleted(CompositeMonitor monitor);

    /**
     * This method should be called by all CompositeMonitor implementations
     * before they call monitorStarted().
     *
     * @param compositeMonitor the composite monitor
     */
    void compositeMonitorStarted(CompositeMonitor compositeMonitor);

    /**
     * Obtains the first CompositeMonitor found on the per thread stack that has
     * its name attribute equal to the supplied name. This method should be used
     * in situations where stateless code is unable to hold a reference to
     * the CompositeMonitor that was originally created. Supplying the name
     * value is needed to ensure that instrumentation errors in code called by
     * users of this method does not interfere with the ability to correctly
     * obtain the original CompositeMonitor.
     *
     * @param name the value of name that our Monitor was created with.
     * @return the first CompositeMonitor with the supplied name, or null if not
     *         found
     * @throws IllegalArgumentException if name is null
     */
    CompositeMonitor getCompositeMonitorNamed(String name) throws IllegalArgumentException;

    /**
     * Returns the current inheritable attributes for this thread.
     *
     * @return the inheritable attributes that would be applied to a monitor
     *         if it were made right now, or an empty Map if there are none
     */
    Map getInheritableAttributes();

    void processMonitorForCompositeMonitor(Monitor monitor);

    void setInheritable(CompositeMonitor monitor, String key, AttributeHolder original);

    void shutdown();

    void startup();

}
