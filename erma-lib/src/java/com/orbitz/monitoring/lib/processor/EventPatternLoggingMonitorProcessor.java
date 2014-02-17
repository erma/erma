package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.lib.renderer.EventPatternMonitorRenderer;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A monitor processor that logs the monitors collected to a log4j logger in an
 * event pattern format.  This format displays the parent-child relationships in
 * a recursive manner.  The pattern of events includes a subset of the Monitor
 * attributes including vmid, name and failed.
 *
 * @author Matt O'Keefe
 */
public class EventPatternLoggingMonitorProcessor extends MonitorProcessorAdapter {

    private EventPatternMonitorRenderer renderer;

    private static final List DEFAULT_ATTRIBUTES = new ArrayList();
    
    static {
        DEFAULT_ATTRIBUTES.add("vmid");
        DEFAULT_ATTRIBUTES.add("name");
        DEFAULT_ATTRIBUTES.add("failureThrowable");
    }

    public EventPatternLoggingMonitorProcessor() {
        renderer = new EventPatternMonitorRenderer(DEFAULT_ATTRIBUTES);
    }

    // ** STATIC/FINAL DATA ***************************************************
    private static final Logger log = Logger.getLogger(
            EventPatternLoggingMonitorProcessor.class);

    // ** PUBLIC METHODS ******************************************************
    public void process(Monitor monitor) {
        String logString = renderMonitor(monitor);
        log.info(logString);
    }

    protected String renderMonitor(Monitor monitor) {
        return renderer.renderMonitor(monitor);
    }

    public void setAllowedAttributes(final List allowedAttributes) {
        renderer.setAllowedAttributes(allowedAttributes);
    }

    public Set getMonitorsToSkip() {
        return renderer.getMonitorsToSkip();
    }

    public void setMonitorsToSkip(Set monitorsToSkip) {
        renderer.setMonitorsToSkip(monitorsToSkip);
    }
}
