package com.orbitz.monitoring.lib.interceptor;

import static org.mockito.Mockito.*;

import junit.framework.TestCase;

/**
 * @see MonitoredAttributeSourceAdvisor
 * @author Ray Krueger
 * @author Connor Garvey
 */
public class MonitoredAttributeSourceAdvisorTest extends TestCase {
    
    /**
     * @see MonitoredAttributeSourceAdvisor#matches(java.lang.reflect.Method, Class)
     * @throws Exception in case of failure
     */
    public void testMatches() throws Exception {
        final MonitoredAttributeSourceAdvisor target = new MonitoredAttributeSourceAdvisor(
                new MatchAlwaysMonitoredAttributeSource());
        assertFalse(target.matches(Object.class.getMethod("toString", new Class[0]), Object.class));
        assertTrue(target.matches(ReflectionTarget.TEST_METHOD_ONE, null));
    }
    
    /**
     * @see MonitoredAttributeSourceAdvisor#MonitoredAttributeSourceAdvisor(MonitoredAttributeSource)
     */
    public void testMonitoredAttributeSourceAdvisorDoNotPrependClassName() {
        final MonitoredAttributeSourceAdvisor advisor = new MonitoredAttributeSourceAdvisor(
                mock(MonitoredAttributeSource.class));
        advisor.setPrependClassName(false);
        final TransactionMonitorInterceptor interceptor = (TransactionMonitorInterceptor)advisor
                .getAdvice();
        assertFalse(interceptor.isPrependClassName());
    }
    
    /**
     * @see MonitoredAttributeSourceAdvisor#MonitoredAttributeSourceAdvisor(MonitoredAttributeSource)
     */
    public void testMonitoredAttributeSourceAdvisorPrependClassName() {
        final MonitoredAttributeSourceAdvisor advisor = new MonitoredAttributeSourceAdvisor(
                mock(MonitoredAttributeSource.class));
        final TransactionMonitorInterceptor interceptor = (TransactionMonitorInterceptor)advisor
                .getAdvice();
        assertTrue(interceptor.isPrependClassName());
    }
    
}
