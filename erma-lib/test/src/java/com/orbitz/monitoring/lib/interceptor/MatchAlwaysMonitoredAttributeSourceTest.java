package com.orbitz.monitoring.lib.interceptor;

import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * @author Ray Krueger
 */
public class MatchAlwaysMonitoredAttributeSourceTest extends TestCase {

    private Method getInterceptedMethod() throws NoSuchMethodException {
        return this.getClass().getMethod("interceptedMethod", new Class[]{String.class, String.class});
    }

    public void testDefaults() throws Exception {
        MatchAlwaysMonitoredAttributeSource source = new MatchAlwaysMonitoredAttributeSource();
        MonitoredAttribute monitoredAttribute = prepareAttributeSource(source);
        assertFalse(monitoredAttribute.isIncludeResult());
        assertFalse(monitoredAttribute.isIncludeArguments());
    }

    public void testIncludeResults() throws Exception {
        MatchAlwaysMonitoredAttributeSource source = new MatchAlwaysMonitoredAttributeSource(true, false);
        MonitoredAttribute monitoredAttribute = prepareAttributeSource(source);
        assertTrue(monitoredAttribute.isIncludeResult());
        assertFalse(monitoredAttribute.isIncludeArguments());
    }

    public void testIncludeArguments() throws Exception {
        MatchAlwaysMonitoredAttributeSource source = new MatchAlwaysMonitoredAttributeSource(false, true);
        MonitoredAttribute monitoredAttribute = prepareAttributeSource(source);
        assertFalse(monitoredAttribute.isIncludeResult());
        assertTrue(monitoredAttribute.isIncludeArguments());
    }

    private MonitoredAttribute prepareAttributeSource(MatchAlwaysMonitoredAttributeSource source) throws NoSuchMethodException {
        MonitoredAttribute monitoredAttribute = source.getMonitoredAttribute(getInterceptedMethod(), this.getClass());
        assertEquals("interceptedMethod", monitoredAttribute.getMonitorName());
        return monitoredAttribute;
    }

    /**
     * reflected on by the tests, do not remove
     */
    public String interceptedMethod(String arg1, String arg2) {
        return arg1 + arg2;
    }

}
