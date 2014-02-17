package com.orbitz.monitoring.test;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitorProcessorFactory;

import junit.framework.Assert;

import java.util.Set;

/**
 * A mock implementation of {@link MonitorProcessorFactory}.
 *
 * @author Doug Barth
 */
public class MockMonitorProcessorFactory
    implements MonitorProcessorFactory {
    // ** PRIVATE DATA ********************************************************
    private boolean _startupCalled;
    private boolean _throwExceptionOnStartup;

    private boolean _shutdownCalled;
    private boolean _throwExceptionOnShutdown;

    private MonitorProcessor[] _defaultProcessors;
    private boolean _getProcessorsForMonitorCalled;
    private boolean _throwThrowableOnGetProcessors;

    // ** CONSTRUCTORS ********************************************************
    public MockMonitorProcessorFactory(MonitorProcessor[] defaultProcessors) {
        _startupCalled = false;
        _throwExceptionOnStartup = false;

        _shutdownCalled = false;
        _throwExceptionOnShutdown = false;

        _defaultProcessors = defaultProcessors;
        _getProcessorsForMonitorCalled = false;
        _throwThrowableOnGetProcessors = false;
    }
    
    public MockMonitorProcessorFactory(MonitorProcessor processor) {
        this(new MonitorProcessor[] {processor});
    }

    // ** PUBLIC METHODS ******************************************************
    public void startup() {
        _startupCalled = true;
        if (_throwExceptionOnStartup) {
            throw new RuntimeException("Startup exception");
        }
    }

    public void shutdown() {
        _shutdownCalled = true;
        if (_throwExceptionOnShutdown) {
            throw new RuntimeException("Shutdown exception");
        }
    }

    public MonitorProcessor[] getProcessorsForMonitor(Monitor monitor) {
        _getProcessorsForMonitorCalled = true;
        if (_throwThrowableOnGetProcessors) {
            throw new Error("getProcessorsForMonitor() throwable");
        }
        return _defaultProcessors;
    }

    public MonitorProcessor[] getAllProcessors() {
        return _defaultProcessors;
    }

    public void assertStartupCalled() {
        Assert.assertTrue("For startup() called", _startupCalled);
    }

    public void assertShutdownCalled() {
        Assert.assertTrue("For shutdown() called", _shutdownCalled);
    }

    public void assertGetProcessorsForMonitorCalled() {
        Assert.assertTrue("For getProcessorsForMonitor() called",
                          _getProcessorsForMonitorCalled);
    }

    // ** ACCESSORS ***********************************************************
    public void setThrowExceptionOnStartup(boolean throwExceptionOnStartup) {
        _throwExceptionOnStartup = throwExceptionOnStartup;
    }

    public void setThrowExceptionOnShutdown(boolean throwExceptionOnShutdown) {
        _throwExceptionOnShutdown = throwExceptionOnShutdown;
    }

    public void setThrowThrowableOnGetProcessors(
            boolean throwThrowableOnGetProcessors) {
        _throwThrowableOnGetProcessors = throwThrowableOnGetProcessors;
    }

    @Override
    public Set<MonitorProcessor> getProcessorsByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }
}
