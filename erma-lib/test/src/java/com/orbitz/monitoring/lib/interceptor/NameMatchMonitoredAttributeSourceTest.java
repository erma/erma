package com.orbitz.monitoring.lib.interceptor;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Ray Krueger
 */
public class NameMatchMonitoredAttributeSourceTest extends TestCase {

    public void testMethodMap() throws Exception {

        Map methodMap = new HashMap();
        methodMap.put("testMethodOne", new MonitoredAttribute());
        methodMap.put("testMethodTwo", "MONITOR_NAME=TESTMANTEST, INCLUDE_RESULT, INCLUDE_ARGUMENTS");

        NameMatchMonitoredAttributeSource target = new NameMatchMonitoredAttributeSource();
        target.setNameMap(methodMap);

        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(ReflectionTarget.TEST_METHOD_ONE, ReflectionTarget.class);
        assertNotNull(monitoredAttribute);
        assertEquals("testMethodOne", monitoredAttribute.getMonitorName());

        monitoredAttribute = target.getMonitoredAttribute(ReflectionTarget.TEST_METHOD_TWO, ReflectionTarget.class);
        assertNotNull(monitoredAttribute);
        assertEquals("TESTMANTEST", monitoredAttribute.getMonitorName());
    }

    public void testProperties() throws Exception {
        Properties props = new Properties();
        props.put("testMethodOne", "MONITOR_NAME=SeeRayTest");
        props.put("testMethodTwo", "MONITOR_NAME=TestRayTest");

        NameMatchMonitoredAttributeSource target = new NameMatchMonitoredAttributeSource();
        target.setProperties(props);

        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(ReflectionTarget.TEST_METHOD_ONE, ReflectionTarget.class);
        assertNotNull(monitoredAttribute);
        assertEquals("SeeRayTest", monitoredAttribute.getMonitorName());

        monitoredAttribute = target.getMonitoredAttribute(ReflectionTarget.TEST_METHOD_TWO, ReflectionTarget.class);
        assertNotNull(monitoredAttribute);
        assertEquals("TestRayTest", monitoredAttribute.getMonitorName());

        monitoredAttribute = target.getMonitoredAttribute(ReflectionTarget.TEST_METHOD_THREE, ReflectionTarget.class);
        assertNull(monitoredAttribute);
    }

}
