package com.orbitz.monitoring.lib.factory;

import junit.framework.TestCase;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.test.MockMonitorProcessor;

import java.util.List;

/**
 * Unit tests for {@link ProcessGroup}.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */

public class ProcessGroupTest extends TestCase {
    // ** PRIVATE DATA ********************************************************
    private ProcessGroup _pGroup;

    // ** TEST SUITE METHODS **************************************************
    protected void setUp()
            throws Exception {
        super.setUp();

        _pGroup = new ProcessGroup(new MockMonitorProcessor());
    }

    protected void tearDown() {
        String foo = MonitoringEngine.getInstance().getOverrideProcessorLevelsListing();
    }

    // ** TEST METHODS ********************************************************
    public void testDeactivate() {
        _pGroup.setActive(false);
        assertEquals("Deactivated group should return 0 processors", 0,
                _pGroup.getProcessorsFor(new EventMonitor("foo")).size());
    }

    public void testMonitoringLevelNoLevelForProcessor() {
        ProcessGroup processGroup = new ProcessGroup(new MockMonitorProcessor());
        assertEquals("Default process group level should be INFO", MonitoringLevel.INFO.toString(),
                processGroup.getMonitoringLevel());

        EventMonitor event = new EventMonitor("baz", MonitoringLevel.DEBUG);

        List processors = processGroup.getProcessorsFor(event);
        assertEquals("No processor should process this monitor b/c its level is DEBUG", 0, processors.size());

        EventMonitor event2 = new EventMonitor("baz", MonitoringLevel.INFO);

        processors = processGroup.getProcessorsFor(event2);

        assertEquals("Processor should process this monitor b/c its level is INFO", 1, processors.size());

        EventMonitor event3 = new EventMonitor("baz", MonitoringLevel.ESSENTIAL);

        processors = processGroup.getProcessorsFor(event3);
        assertEquals("Processor should process this monitor b/c its level is ESSENTIAL", 1, processors.size());
    }

    public void testMonitoringLevelForProcessor() {
        MonitorProcessor mp = new MockMonitorProcessor("mpA");
        ProcessGroup processGroup = new ProcessGroup(mp);

        MonitoringEngine.getInstance().addProcessorLevel("mpA",MonitoringLevel.DEBUG);

        EventMonitor event = new EventMonitor("baz", MonitoringLevel.DEBUG);

        List processors = processGroup.getProcessorsFor(event);
        assertEquals("Processor should process this monitor b/c its level is DEBUG", 1, processors.size());

        MonitoringEngine.getInstance().addProcessorLevel("mpA",MonitoringLevel.ESSENTIAL);

        processors = processGroup.getProcessorsFor(event);
        assertEquals("No processor should process this monitor b/c its level is DEBUG", 0, processors.size());
    }

    public void testMonitoringLevelForProcessGroup() {
        ProcessGroup processGroup = new ProcessGroup(new MockMonitorProcessor());

        EventMonitor event = new EventMonitor("baz");

        List processors = processGroup.getProcessorsFor(event);
        assertEquals("ProcessorGroup should process INFO monitors by default", 1, processors.size());

        event = new EventMonitor("baz", MonitoringLevel.DEBUG);

        processors = processGroup.getProcessorsFor(event);
        assertEquals("ProcessorGroup should not process DEBUG monitors by default", 0, processors.size());

        processGroup.updateMonitoringLevel(MonitoringLevel.DEBUG.toString());

        processors = processGroup.getProcessorsFor(event);
        assertEquals("ProcessorGroup should apply this monitor b/c its level is DEBUG", 1, processors.size());

        event = new EventMonitor("baz", MonitoringLevel.ESSENTIAL);

        processors = processGroup.getProcessorsFor(event);
        assertEquals("ProcessorGroup should apply this monitor b/c its level is DEBUG", 1, processors.size());
    }

    public void testMonitoringLevelForProcessorAndProcessGroup() {
        MonitorProcessor mp = new MockMonitorProcessor("mpA");
        ProcessGroup processGroup = new ProcessGroup(mp);

        EventMonitor event = new EventMonitor("baz", MonitoringLevel.DEBUG);
        processGroup.updateMonitoringLevel(MonitoringLevel.DEBUG.toString());

        MonitoringEngine.getInstance().addProcessorLevel("mpA",MonitoringLevel.INFO);

        List processors = processGroup.getProcessorsFor(event);
        assertEquals("No processor should process this monitor b/c its level is DEBUG", 0, processors.size());

        event = new EventMonitor("baz", MonitoringLevel.DEBUG);
        processGroup.updateMonitoringLevel(MonitoringLevel.INFO.toString());

        MonitoringEngine.getInstance().addProcessorLevel("mpA",MonitoringLevel.DEBUG);

        processors = processGroup.getProcessorsFor(event);
        assertEquals("Processor should process this monitor b/c its level is DEBUG", 1, processors.size());
    }
    
    public void testNameMatching() {
        // Null name expression
        List processors = _pGroup.getProcessorsFor(new EventMonitor("foo"));
        assertEquals("Processor should appy to monitor", 1, processors.size());
        processors = _pGroup.getProcessorsFor(new EventMonitor("bar"));
        assertEquals("Processor should appy to monitor", 1, processors.size());

        _pGroup.setExpression("m.get('name').matches('.*')");
        processors = _pGroup.getProcessorsFor(new EventMonitor("foo"));
        assertEquals("Processor should appy to monitor", 1, processors.size());

        processors = _pGroup.getProcessorsFor(new EventMonitor("bar"));
        assertEquals("Processor should appy to monitor", 1, processors.size());

        _pGroup.setExpression("m.get('name').matches('foo')");

        processors = _pGroup.getProcessorsFor(new EventMonitor("foo"));
        assertEquals("Processor should appy to monitor", 1, processors.size());

        processors = _pGroup.getProcessorsFor(new EventMonitor("bar"));
        assertEquals("Processor should appy to monitor", 0, processors.size());
    }

    public void testUserDataMatching() {
        EventMonitor noUserData = new EventMonitor("noUserData");

        EventMonitor barUserData = new EventMonitor("barUserData");
        barUserData.set("foo", "bar");
        barUserData.set("bar", "baz");

        EventMonitor bazUserData = new EventMonitor("bazUserData");
        bazUserData.set("foo", "baz");

        // Null user data expression

        List processors = _pGroup.getProcessorsFor(noUserData);
        assertEquals("Processor should appy to monitor", 1, processors.size());

        processors = _pGroup.getProcessorsFor(barUserData);
        assertEquals("Processor should appy to monitor", 1, processors.size());

        processors = _pGroup.getProcessorsFor(bazUserData);
        assertEquals("Processor should appy to monitor", 1, processors.size());

        _pGroup.setExpression("m.get('foo').matches('.*')");
        processors = _pGroup.getProcessorsFor(noUserData);
        assertEquals("Processor should appy to monitor", 0, processors.size());

        processors = _pGroup.getProcessorsFor(barUserData);
        assertEquals("Processor should appy to monitor", 1, processors.size());

        processors = _pGroup.getProcessorsFor(bazUserData);
        assertEquals("Processor should appy to monitor", 1, processors.size());

        _pGroup.setExpression("m.get('foo').matches('bar')");
        processors = _pGroup.getProcessorsFor(noUserData);
        assertEquals("Processor should appy to monitor", 0, processors.size());

        processors = _pGroup.getProcessorsFor(barUserData);
        assertEquals("Processor should appy to monitor", 1, processors.size());

        processors = _pGroup.getProcessorsFor(bazUserData);
        assertEquals("Processor should appy to monitor", 0, processors.size());

        _pGroup.setExpression("m.get('bar').matches('bar')");
        processors = _pGroup.getProcessorsFor(noUserData);
        assertEquals("Processor should appy to monitor", 0, processors.size());

        processors = _pGroup.getProcessorsFor(barUserData);
        assertEquals("Processor should appy to monitor", 0, processors.size());

        processors = _pGroup.getProcessorsFor(bazUserData);
        assertEquals("Processor should appy to monitor", 0, processors.size());

        _pGroup.setExpression("m.get('bar').matches('baz')");
        processors = _pGroup.getProcessorsFor(noUserData);
        assertEquals("Processor should appy to monitor", 0, processors.size());

        processors = _pGroup.getProcessorsFor(barUserData);
        assertEquals("Processor should appy to monitor", 1, processors.size());

        processors = _pGroup.getProcessorsFor(bazUserData);
        assertEquals("Processor should appy to monitor", 0, processors.size());
    }

    public void testNonsenseMatching() {
        _pGroup.setExpression("m.bar");

        List processors = _pGroup.getProcessorsFor(new EventMonitor("test"));
        assertEquals("Processor should appy to monitor", 0, processors.size());

        _pGroup.setExpression("m.name");

        processors = _pGroup.getProcessorsFor(new EventMonitor("test"));
        assertEquals("Processor should appy to monitor", 0, processors.size());
    }
}
