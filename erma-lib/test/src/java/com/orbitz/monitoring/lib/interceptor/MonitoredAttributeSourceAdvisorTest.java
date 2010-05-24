package com.orbitz.monitoring.lib.interceptor;

import junit.framework.TestCase;

/**
 * @author Ray Krueger
 */
public class MonitoredAttributeSourceAdvisorTest extends TestCase {

    public void test() throws Exception {

        MonitoredAttributeSourceAdvisor target = new MonitoredAttributeSourceAdvisor(new MatchAlwaysMonitoredAttributeSource());

        assertFalse(target.matches(Object.class.getMethod("toString", new Class[0]), Object.class));
        assertTrue(target.matches(ReflectionTarget.TEST_METHOD_ONE, null));
    }

}
