package com.orbitz.monitoring.lib.interceptor;

import junit.framework.TestCase;

/**
 * @author Ray Krueger
 */
public class MonitoredAttributeEditorTest extends TestCase {

    public void test() throws Exception {
        MonitoredAttributeEditor editor = new MonitoredAttributeEditor();
        editor.setAsText("MONITOR_NAME=Test");
        MonitoredAttribute value = (MonitoredAttribute) editor.getValue();

        assertEquals(new MonitoredAttribute("Test"), value);
    }

    public void testIncludes() throws Exception {
        MonitoredAttributeEditor editor = new MonitoredAttributeEditor();
        editor.setAsText("MONITOR_NAME=Test, INCLUDE_RESULT, INCLUDE_ARGUMENTS");
        MonitoredAttribute value = (MonitoredAttribute) editor.getValue();

        assertEquals(new MonitoredAttribute("Test", true, true), value);
    }

    public void testIncludeResult() throws Exception {
        MonitoredAttributeEditor editor = new MonitoredAttributeEditor();
        editor.setAsText("INCLUDE_RESULT");
        MonitoredAttribute value = (MonitoredAttribute) editor.getValue();

        assertEquals(new MonitoredAttribute(null, true, false), value);
    }

    public void testIncludeArguments() throws Exception {
        MonitoredAttributeEditor editor = new MonitoredAttributeEditor();
        editor.setAsText("INCLUDE_ARGUMENTS");
        MonitoredAttribute value = (MonitoredAttribute) editor.getValue();

        assertEquals(new MonitoredAttribute(null, false, true), value);
    }

    public void testNull() throws Exception {
        MonitoredAttributeEditor editor = new MonitoredAttributeEditor();
        editor.setAsText("");
        assertNull(editor.getValue());

        editor.setAsText(null);
        assertNull(editor.getValue());
    }
}
