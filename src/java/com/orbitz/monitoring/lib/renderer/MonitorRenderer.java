package com.orbitz.monitoring.lib.renderer;

import com.orbitz.monitoring.api.Monitor;

/**
 * Renders a {@link Monitor} to a String representation.
 */
public interface MonitorRenderer {
    /**
     * Renders a Monitor in a particular rendering format.
     *
     * @param monitor monitor to render
     * @return a String representation of the monitor.
     */
    public String renderMonitor(Monitor monitor);

    /**
     * Renders a Monitor in a particular rendering format.
     *
     * @param monitor monitor to render
     * @param includeStackTraces true if every Throwable attribute value should render with a stack trace
     * @return a String representation of the monitor.
     */
    public String renderMonitor(Monitor monitor, boolean includeStackTraces);
}
