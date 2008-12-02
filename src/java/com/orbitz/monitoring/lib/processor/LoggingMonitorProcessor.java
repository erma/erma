package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.lib.renderer.SimpleMonitorRenderer;
import org.apache.log4j.Logger;

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

    private String _name;

    // ** PUBLIC METHODS ******************************************************
    public void startup() {
        log = Logger.getLogger(getClass());
    }

    public void shutdown() {
        // No-op
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

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    // ** PRIVATE METHODS *****************************************************
    private String renderMonitor(Monitor monitor) {
        return new SimpleMonitorRenderer().renderMonitor(monitor, _includeStackTrace);
    }

    // ** ACCESSORS ***********************************************************
    public boolean isLogMonitorCreated() {
        return _logMonitorCreated;
    }

    public void setLogMonitorCreated(boolean logMonitorCreated) {
        _logMonitorCreated = logMonitorCreated;
    }

    public boolean isLogMonitorStarted() {
        return _logMonitorStarted;
    }

    public void setLogMonitorStarted(boolean logMonitorStarted) {
        _logMonitorStarted = logMonitorStarted;
    }

    public boolean isLogProcess() {
        return _logProcess;
    }

    public void setLogProcess(boolean logProcess) {
        _logProcess = logProcess;
    }

    public boolean isIncludeStackTrace() {
        return _includeStackTrace;
    }

    public void setIncludeStackTrace(boolean includeStackTrace) {
        _includeStackTrace = includeStackTrace;
    }
}
