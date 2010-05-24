package com.orbitz.monitoring.lib.interceptor.annotation;

import com.orbitz.monitoring.api.annotation.Monitored;
import com.orbitz.monitoring.lib.interceptor.MonitoredAttribute;
import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * DOCUMENT ME!
 *
 * @author Ray Krueger
 */
public class AnnotationMonitoredAttributeSourceTest extends TestCase {

    public void testMethodOne() throws Exception {
        AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(getMethod("methodOne"), AnnotatedTarget.class);

        assertEquals("methodOne", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());

    }

    public void testMethodTwo() throws Exception {
        AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(getMethod("methodTwo"), AnnotatedTarget.class);

        assertEquals("I_HAVE_A_NAME", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());

    }

    public void testMethodThree() throws Exception {
        AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(getMethod("methodThree"), AnnotatedTarget.class);

        assertEquals("I_HAVE_ALL", monitoredAttribute.getMonitorName());
        assertTrue(monitoredAttribute.isIncludeArguments());
        assertTrue(monitoredAttribute.isIncludeResult());
    }

    public void testLevelAnnotation() throws Exception {
        AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(getMethod("levelAnnotation"), AnnotatedTarget.class);

        assertEquals("monitor_name", monitoredAttribute.getMonitorName());
        assertEquals("debug", monitoredAttribute.getLevelStr());
    }

    public void testSubclass() throws Exception {
        AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(getMethod("methodThree", AnnotatedTarget.class), AnnotatedTargetSubclass.class);

        assertEquals("I_HAVE_ALL", monitoredAttribute.getMonitorName());
        assertTrue(monitoredAttribute.isIncludeArguments());
        assertTrue(monitoredAttribute.isIncludeResult());
    }

    public void testInterface() throws Exception {
        AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(getMethod("imethod", AnnotatedInterface.class), AnnotatedTargetSubclass.class);

        assertEquals("imethod", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());
    }

    public void testInterfaceNotAnnotated() throws Exception {
        AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(getMethod("notAnnotated", AnnotatedInterface.class), AnnotatedTargetSubclass.class);

        assertEquals("notAnnotated", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());
    }

    public void testTypeLevelAnnotation() throws Exception {
        AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();

        MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(getMethod("methodOne", TypeLevelAnnotated.class), TypeLevelAnnotated.class);
        assertEquals("methodOne", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());

        monitoredAttribute = target.getMonitoredAttribute(getMethod("notAnnotated", TypeLevelAnnotated.class), TypeLevelAnnotated.class);
        assertEquals("notAnnotated", monitoredAttribute.getMonitorName());
        assertFalse(monitoredAttribute.isIncludeArguments());
        assertFalse(monitoredAttribute.isIncludeResult());
    }

    private Method getMethod(String name) {
        return getMethod(name, AnnotatedTarget.class);
    }

    private Method getMethod(String name, Class cls) {
        Method[] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) return method;
        }
        throw new IllegalArgumentException("WTF? " + name);
    }


    public static class AnnotatedTarget {

        @Monitored
        public void methodOne() {

        }

        @Monitored("I_HAVE_A_NAME")
        public void methodTwo() {

        }

        @Monitored(value = "I_HAVE_ALL", includeArguments = true, includeResult = true)
        public String methodThree(String test) {
            return test;
        }

        @Monitored(value = "monitor_name", levelStr = "debug")
        public void levelAnnotation() {

        }
    }

    public static class AnnotatedTargetSubclass extends AnnotatedTarget implements AnnotatedInterface {

        public void imethod() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Monitored
        public void notAnnotated() {
            throw new UnsupportedOperationException("Not implemented");
        }

        public String methodThree(String test) {
            return super.methodThree(test);
        }
    }

    public static interface AnnotatedInterface {
        @Monitored
        public void imethod();

        public void notAnnotated();
    }

    @Monitored
    public static class TypeLevelAnnotated extends AnnotatedTarget {
        public void notAnnotated(){}
    }

}
