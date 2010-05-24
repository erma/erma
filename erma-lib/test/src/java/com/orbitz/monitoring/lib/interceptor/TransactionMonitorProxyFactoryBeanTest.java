package com.orbitz.monitoring.lib.interceptor;

import com.orbitz.monitoring.lib.interceptor.SimpleService;
import com.orbitz.monitoring.lib.interceptor.SimpleServiceImpl;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * DOCUMENT ME!
 *
 * @author Ray Krueger
 */
public class TransactionMonitorProxyFactoryBeanTest extends TestCase {

    public void testSimple() throws Exception {
        TransactionMonitorProxyFactoryBean factory = new TransactionMonitorProxyFactoryBean();
        factory.setTarget(new SimpleServiceImpl());
        factory.setProxyInterfaces(new Class[]{SimpleService.class});
        factory.afterPropertiesSet();
        Object o = factory.createMainInterceptor();
        assertNotNull(o);
    }

    public void testInterceptorProvided() throws Exception {
        TransactionMonitorProxyFactoryBean factory = new TransactionMonitorProxyFactoryBean(
                new TransactionMonitorInterceptor(new ExplodingMonitoredAttributeSource()));

        factory.setProxyInterfaces(new Class[]{SimpleService.class});
        factory.setTarget(new SimpleServiceImpl());
        factory.afterPropertiesSet();

        SimpleService service = (SimpleService) factory.getObject();
        try {
            service.sayHello("Ray");
            fail("MonitoredAttributeSource provided was not used");
        } catch (UnsupportedOperationException e) {
            //If this happened, then I know the MonitoredAttributeSource was called correctly
        }
    }

    public void testMonitoredAttributeSourceProvided() throws Exception {
        TransactionMonitorProxyFactoryBean factory = new TransactionMonitorProxyFactoryBean(
                new ExplodingMonitoredAttributeSource());

        factory.setProxyInterfaces(new Class[]{SimpleService.class});
        factory.setTarget(new SimpleServiceImpl());
        factory.afterPropertiesSet();

        SimpleService service = (SimpleService) factory.getObject();
        try {
            service.sayHello("Ray");
            fail("MonitoredAttributeSource provided was not used");
        } catch (UnsupportedOperationException e) {
            //If this happened, then I know the MonitoredAttributeSource was called correctly
        }
    }

    public void testProperties() throws Exception {

        Properties props = new Properties();
        props.setProperty("sayHello", "MONITOR_NAME=Poop");
        
        TransactionMonitorProxyFactoryBean factory = new TransactionMonitorProxyFactoryBean();
        factory.setTransactionMonitorAttributes(props);
        factory.setTarget(new SimpleServiceImpl());
        factory.afterPropertiesSet();
        factory.getObject();

    }

    public void testNotNull() throws Exception {
        try {
            new TransactionMonitorProxyFactoryBean((MonitoredAttributeSource) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            new TransactionMonitorProxyFactoryBean((TransactionMonitorInterceptor) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        TransactionMonitorProxyFactoryBean factory = new TransactionMonitorProxyFactoryBean();
        try {
            factory.setMonitoredAttributeSource(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            factory.setTransactionMonitorAttributes(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            factory.setTransactionMonitorInterceptor(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }

    }

    private static class ExplodingMonitoredAttributeSource implements MonitoredAttributeSource {
        public MonitoredAttribute getMonitoredAttribute(Method method, Class targetClass) {
                            throw new UnsupportedOperationException("Not implemented");
                        }
    }
}
