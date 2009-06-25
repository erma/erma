package com.orbitz.monitoring.api.monitor;

import junit.framework.TestCase;

public class ValueMonitorTest extends TestCase {

    public void testThatAttributesAreSet() {
        final ValueMonitor m = new ValueMonitor("baseName.valueName", 1.0);
        assertEquals("baseName.valueName", m.get("name"));
        assertEquals(1.0, m.get("value"));
    }

}
