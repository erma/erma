package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.test.MonitorTestBase;

import java.util.Map;

/**
 * Unit tests for {@link EventMonitor}.
 *
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 *
 * @author Doug Barth
 */
public class EventMonitorTest extends MonitorTestBase {
    // ** TEST METHODS ********************************************************
    public void testEvent()
            throws Exception {
        EventMonitor event = new EventMonitor("fakeEvent");

        event.fire();

        getMockProcessor(event).assertExpectedProcessObject(event);
    }

    public void testBadNameEvent() {
        EventMonitor event = new EventMonitor("fake|Event,[]()$,*^@|~?&<> ");

        assertEquals("invalid chars should be removed from name", "fakeEvent", event.get(Monitor.NAME));

        event.fire();

        getMockProcessor(event).assertExpectedProcessObject(event);
    }

    // ** PROTECTED METHODS ***************************************************
    protected Monitor createMonitor(String name, Map inheritedAttributes) {
        return new EventMonitor(name, inheritedAttributes);
    }

    protected Monitor[] useMonitors() {
        EventMonitor event = new EventMonitor("test1");
        event.fire();

        return new Monitor[]{event};
    }

    protected void completeMonitorUse(Monitor monitor) {
        ((EventMonitor) monitor).fire();
    }
}
