package com.orbitz.monitoring.lib.processor;

import org.apache.log4j.Logger;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.lib.renderer.MonitorRenderer;
import com.orbitz.monitoring.lib.renderer.SimpleMonitorRenderer;

/**
 * This is an implementation of the {@link MonitorProcessor} interface that
 * logs Monitor callback events to Log4j.
 *
 * @author Doug Barth
 */

public class LoggingMonitorProcessor
implements MonitorProcessor {
    // ** PRIVATE DATA ********************************************************
    private Logger log;

    private boolean _logMonitorCreated = false;
    private boolean _logMonitorStarted = false;
    private boolean _logProcess = true;
    private boolean _includeStackTrace = false;

    private MonitorRenderer monitorRenderer;

    private String _name;

    public LoggingMonitorProcessor() {
        monitorRenderer = new SimpleMonitorRenderer();
    }

    public String getName() {
        return _name;
    }

    public boolean isIncludeStackTrace() {
        return _includeStackTrace;
    }

    // ** ACCESSORS ***********************************************************
    public MonitorRenderer getMonitorRenderer() {
        return monitorRenderer;
    }

    public boolean isLogMonitorCreated() {
        return _logMonitorCreated;
    }

    public boolean isLogMonitorStarted() {
        return _logMonitorStarted;
    }

    public boolean isLogProcess() {
        return _logProcess;
    }

    public void monitorCreated(Monitor monitor) {
        if (isLogMonitorCreated()) {
            log.info("monitorCreated: " + renderMonitor(monitor));
        }
    }

    public void monitorStarted(Monitor monitor) {
        if (isLogMonitorStarted()) {
            log.info("monitorStarted: " + renderMonitor(monitor));
        }
    }

    public void process(Monitor monitor) {
        if (isLogProcess()) {
            log.info("process: " + renderMonitor(monitor));
        }
    }

    public void setIncludeStackTrace(boolean includeStackTrace) {
        _includeStackTrace = includeStackTrace;
    }

    public void setLogMonitorCreated(boolean logMonitorCreated) {
        _logMonitorCreated = logMonitorCreated;
    }

    public void setLogMonitorStarted(boolean logMonitorStarted) {
        _logMonitorStarted = logMonitorStarted;
    }

    public void setLogProcess(boolean logProcess) {
        _logProcess = logProcess;
    }

    public void setName(final String name) {
        _name = name;
    }

    public void setMonitorRenderer(MonitorRenderer monitorRenderer) {
        this.monitorRenderer = monitorRenderer;
    }

    public void shutdown() {
        // No-op
    }

    // ** PUBLIC METHODS ******************************************************
    public void startup() {
        log = Logger.getLogger(getClass());
    }

    // ** PRIVATE METHODS *****************************************************
    private String renderMonitor(Monitor monitor) {
        return monitorRenderer.renderMonitor(monitor, _includeStackTrace);
    }
}
