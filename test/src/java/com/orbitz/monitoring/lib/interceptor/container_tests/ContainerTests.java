package com.orbitz.monitoring.lib.interceptor.container_tests;

import com.orbitz.monitoring.lib.interceptor.SimpleService;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * DOCUMENT ME!
 *
 * @author Ray Krueger
 */
public class ContainerTests extends TestCase {

    static {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }

    public void testBasicProxyTests() throws Exception {
        ApplicationContext context = createContext("BasicProxyTests.xml");
        SimpleService simpleService = (SimpleService) context.getBean("simpleService");
        assertTrue(Proxy.isProxyClass(simpleService.getClass()));
        String s = simpleService.sayHello("Ray");
    }

    public void testMethodNamePoxyTests() throws Exception {
        ApplicationContext context = createContext("MethodNameProxyTests.xml");
        SimpleService service = (SimpleService) context.getBean("simpleService");
        assertTrue(Proxy.isProxyClass(service.getClass()));
        String s = service.sayHello("Ray");
        s = service.sayGoodbye("Ray");
    }

    public void testAnnotationAutoProxy() throws Exception {
        ApplicationContext context = createContext("AnnotationAutoProxyTest.xml");
        SimpleService simpleService = (SimpleService) context.getBean("simpleService");
        assertTrue(Proxy.isProxyClass(simpleService.getClass()));
        String s = simpleService.sayHello("Ray");
    }

    public void testBeanNameAutoProxy() throws Exception {
        ApplicationContext context = createContext("BeanNameAutoProxyTest.xml");
        SimpleService simpleService = (SimpleService) context.getBean("simpleService");
        assertTrue(Proxy.isProxyClass(simpleService.getClass()));
        String s = simpleService.sayHello("Ray");
    }

    public void testAnnotationBasedProxyFactory() throws Exception {
        ApplicationContext context = createContext("AnnotationProxyFactoryTest.xml");
        SimpleService simpleService = (SimpleService) context.getBean("simpleService");
        assertTrue(Proxy.isProxyClass(simpleService.getClass()));
        String s = simpleService.sayHello("Ray");
    }

    private ApplicationContext createContext(String resourceName) throws URISyntaxException {
        URL resource = ContainerTests.class.getResource(resourceName);

        if (resource == null) {
            throw new NullPointerException("Could not find " + resourceName + ", make sure resources are being copied to the classpath");
        }

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(resource.toURI().toString());
        return context;
    }

}
