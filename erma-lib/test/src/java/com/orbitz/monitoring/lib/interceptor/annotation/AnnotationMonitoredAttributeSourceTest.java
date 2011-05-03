package com.orbitz.monitoring.lib.interceptor.annotation;

import com.orbitz.monitoring.api.annotation.Monitored;
import com.orbitz.monitoring.lib.interceptor.MonitoredAttribute;
import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 * @see AnnotationMonitoredAttributeSource
 * @author Ray Krueger
 */
public class AnnotationMonitoredAttributeSourceTest extends TestCase {
    
    private Method getMethod(final String name) {
        return getMethod(name, AnnotatedTarget.class);
    }
    
    private Method getMethod(final String name, final Class cls) {
        final Method[] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            if (method.getName().equals(name)) {
                return method;
            }
        }
        throw new IllegalArgumentException("WTF? " + name);
    }
    
    public void testInterface() throws Exception {
        final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
                getMethod("imethod", AnnotatedInterface.class), AnnotatedTargetSubclass.class);
        
        assertEquals("imethod", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());
    }
    
    public void testInterfaceNotAnnotated() throws Exception {
        final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
                getMethod("notAnnotated", AnnotatedInterface.class), AnnotatedTargetSubclass.class);
        
        assertEquals("notAnnotated", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());
    }
    
    public void testLevelAnnotation() throws Exception {
        final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
                getMethod("levelAnnotation"), AnnotatedTarget.class);
        
        assertEquals("monitor_name", monitoredAttribute.getMonitorName());
        assertEquals("debug", monitoredAttribute.getLevelStr());
    }
    
    public void testMethodOne() throws Exception {
        final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
                getMethod("methodOne"), AnnotatedTarget.class);
        
        assertEquals("methodOne", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());
        
    }
    
    public void testMethodThree() throws Exception {
        final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
                getMethod("methodThree"), AnnotatedTarget.class);
        
        assertEquals("I_HAVE_ALL", monitoredAttribute.getMonitorName());
        assertTrue(monitoredAttribute.isIncludeArguments());
        assertTrue(monitoredAttribute.isIncludeResult());
    }
    
    public void testMethodTwo() throws Exception {
        final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
                getMethod("methodTwo"), AnnotatedTarget.class);
        
        assertEquals("I_HAVE_A_NAME", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());
        
    }
    
    public void testSubclass() throws Exception {
        final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
                getMethod("methodThree", AnnotatedTarget.class), AnnotatedTargetSubclass.class);
        
        assertEquals("I_HAVE_ALL", monitoredAttribute.getMonitorName());
        assertTrue(monitoredAttribute.isIncludeArguments());
        assertTrue(monitoredAttribute.isIncludeResult());
    }
    
    public void testTypeLevelAnnotation() throws Exception {
        final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        
        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
                getMethod("methodOne", TypeLevelAnnotated.class), TypeLevelAnnotated.class);
        assertEquals("methodOne", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());
        
        monitoredAttribute = target.getMonitoredAttribute(
                getMethod("notAnnotated", TypeLevelAnnotated.class), TypeLevelAnnotated.class);
        assertEquals("notAnnotated", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());
    }
    
    public static interface AnnotatedInterface {
        @Monitored
        public void imethod();
        
        public void notAnnotated();
    }
    
    public static class AnnotatedTarget {
        
        @Monitored(value = "monitor_name", levelStr = "debug")
        public void levelAnnotation() {
            
        }
        
        @Monitored
        public void methodOne() {
            
        }
        
        @Monitored(value = "I_HAVE_ALL", includeArguments = true, includeResult = true)
        public String methodThree(final String test) {
            return test;
        }
        
        @Monitored("I_HAVE_A_NAME")
        public void methodTwo() {
            
        }
    }
    
    public static class AnnotatedTargetSubclass extends AnnotatedTarget implements
            AnnotatedInterface {
        
        public void imethod() {
            throw new UnsupportedOperationException("Not implemented");
        }
        
        @Override
        public String methodThree(final String test) {
            return super.methodThree(test);
        }
        
        @Monitored
        public void notAnnotated() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
    
    @Monitored
    public static class TypeLevelAnnotated extends AnnotatedTarget {
        public void notAnnotated() {
        }
    }
    
}
