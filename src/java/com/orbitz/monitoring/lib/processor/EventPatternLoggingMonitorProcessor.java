package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.lib.renderer.EventPatternMonitorRenderer;
import org.apache.log4j.Logger;

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

    private boolean includeLatency = false;

    private boolean includeCpuTime = false;

    private EventPatternMonitorRenderer renderer;

    // ** STATIC/FINAL DATA ***************************************************
    private static final Logger log = Logger.getLogger(
            EventPatternLoggingMonitorProcessor.class);

    public EventPatternLoggingMonitorProcessor() {
        renderer = new EventPatternMonitorRenderer();
    }

    // ** PUBLIC METHODS ******************************************************
    public void process(Monitor monitor) {
        String logString = renderMonitor(monitor);
        log.info(logString);
    }

    protected String renderMonitor(Monitor monitor) {
        return renderer.renderMonitor(monitor, includeLatency, includeCpuTime);
    }


    public boolean isIncludeLatency() {
        return includeLatency;
    }

    public void setIncludeLatency(boolean includeLatency) {
        this.includeLatency = includeLatency;
    }


    public boolean isIncludeCpuTime() {
        return includeCpuTime;
    }

    public void setIncludeCpuTime(boolean includeCpuTime) {
        this.includeCpuTime = includeCpuTime;
    }

    public Set getMonitorsToSkip() {
        return renderer.getMonitorsToSkip();
    }

    public void setMonitorsToSkip(Set monitorsToSkip) {
        renderer.setMonitorsToSkip(monitorsToSkip);
    }

}
