package com.orbitz.monitoring.test;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import junit.framework.Assert;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A mock implementation of MonitorProcessor that can be used to assert expected
 * monitor behavior.
 *
 *
 * @author Doug Barth
 */
public class MockMonitorProcessor implements MonitorProcessor {
    // ** PRIVATE DATA ********************************************************
    private String name;

    private boolean _startupCalled;
    private boolean _shutdownCalled;

    private Set _monitorCreatedObjects;
    private Set _monitorStartedObjects;
    private Set _processObjects;

    private int _processDelay = 0;
    private boolean _throwThrowableDuringProcessing;

    private String _stringConfig;
    private int _intConfig;
    private long _longConfig;
    private float _floatConfig;
    private double _doubleConfig;
    private char _charConfig;

    // ** CONSTRUCTORS ********************************************************
    public MockMonitorProcessor() {
        _startupCalled = false;
        _shutdownCalled = false;

        _throwThrowableDuringProcessing = false;

        _monitorCreatedObjects = new LinkedHashSet();
        _monitorStartedObjects = new LinkedHashSet();
        _processObjects = new LinkedHashSet();
    }

    public MockMonitorProcessor(String name) {
        this();
        this.name = name;
    }

    // ** PUBLIC METHODS ******************************************************
    public void startup() {
        _startupCalled = true;
    }

    public void shutdown() {
        _shutdownCalled = true;
    }

    public void monitorCreated(Monitor monitor) {
        throwThrowableIfConfigured();

        if (_processDelay > 0) {
            try {
                Thread.sleep(_processDelay);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        _monitorCreatedObjects.add(monitor);
    }

    public void assertExpectedMonitorCreatedObject(Monitor monitor) {
        Assert.assertTrue("For expected monitorCreated() object: " + monitor,
                          _monitorCreatedObjects.remove(monitor));
    }

    public Monitor[] extractMonitorCreatedObjects() {
        return extractMonitorsFrom(_monitorCreatedObjects);
    }

    public void monitorStarted(Monitor monitor) {
        _monitorStartedObjects.add(monitor);
    }

    public void assertExpectedMonitorStartedObject(Monitor monitor) {
        Assert.assertTrue("For expected monitorStarted() object: " + monitor,
                          _monitorStartedObjects.remove(monitor));
    }

    public Monitor[] extractMonitorStartedObjects() {
        return extractMonitorsFrom(_monitorStartedObjects);
    }

    public void process(Monitor monitor) {
        throwThrowableIfConfigured();

        if (_processDelay > 0) {
            try {
                Thread.sleep(_processDelay);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        _processObjects.add(monitor);
    }

    public void assertExpectedProcessObject(Monitor monitor) {
        Assert.assertTrue("For expected process() object: " + monitor,
                          _processObjects.remove(monitor));
    }

    public Monitor[] extractProcessObjects() {
        return extractMonitorsFrom(_processObjects);
    }

    public void assertStartupCalled() {
        Assert.assertTrue("For startup called", _startupCalled);
    }

    public void assertShutdownCalled() {
        Assert.assertTrue("For shutdown called", _shutdownCalled);
    }


    public void assertNoUnexpectedCalls() {
        Assert.assertEquals("For monitorCreated() calls",
                            new HashSet(), _monitorCreatedObjects);
        Assert.assertEquals("For process() calls",
                            new HashSet(), _processObjects);
    }

    public void clear() {
        _monitorCreatedObjects.clear();
        _processObjects.clear();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // ** PRIVATE DATA ********************************************************
    private Monitor[] extractMonitorsFrom(Set monitorSet) {
        int size = monitorSet.size();
        Monitor[] monitors = (Monitor[]) monitorSet.toArray(new Monitor[size]);

        monitorSet.clear();

        return monitors;
    }

    private void throwThrowableIfConfigured() {
        if (_throwThrowableDuringProcessing) {
            throw new Error("Processing throwable");
        }
    }

    // ** ACCESSORS ***********************************************************
    public int getProcessDelay() {
        return _processDelay;
    }

    public void setProcessDelay(int processDelay) {
        _processDelay = processDelay;
    }

    public boolean isThrowThrowableDuringProcessing() {
        return _throwThrowableDuringProcessing;
    }

    public void setThrowThrowableDuringProcessing(
            boolean throwThrowableDuringProcessing) {
        _throwThrowableDuringProcessing = throwThrowableDuringProcessing;
    }

    public String getStringConfig() {
        return _stringConfig;
    }

    public void setStringConfig(String stringConfig) {
        _stringConfig = stringConfig;
    }

    public int getIntConfig() {
        return _intConfig;
    }

    public void setIntConfig(int intConfig) {
        _intConfig = intConfig;
    }

    public long getLongConfig() {
        return _longConfig;
    }

    public void setLongConfig(long longConfig) {
        _longConfig = longConfig;
    }

    public float getFloatConfig() {
        return _floatConfig;
    }

    public void setFloatConfig(float floatConfig) {
        _floatConfig = floatConfig;
    }

    public double getDoubleConfig() {
        return _doubleConfig;
    }

    public void setDoubleConfig(double doubleConfig) {
        _doubleConfig = doubleConfig;
    }

    public char getCharConfig() {
        return _charConfig;
    }

    public void setCharConfig(char charConfig) {
        _charConfig = charConfig;
    }
}
