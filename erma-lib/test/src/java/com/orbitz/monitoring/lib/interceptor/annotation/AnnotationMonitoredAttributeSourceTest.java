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
  
  private Method findMethod(final String name) {
    return findMethod(name, AnnotatedTarget.class);
  }
  
  private Method findMethod(final String name, final Class<?> cls) {
    for (Method method : cls.getMethods()) {
      if (method.getName().equals(name)) {
        return method;
      }
    }
    throw new IllegalArgumentException("Could not find method '" + name + "' in '"
        + cls.getCanonicalName() + "'");
  }
  
  /**
   * @see AnnotationMonitoredAttributeSource#getMonitoredAttribute(Method, Class)
   */
  public void testInterface() {
    final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
    final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
        findMethod("imethod", AnnotatedInterface.class), AnnotatedTargetSubclass.class);
    assertEquals("imethod", monitoredAttribute.getMonitorName());
    assertFalse(monitoredAttribute.isIncludeArguments());
    assertFalse(monitoredAttribute.isIncludeResult());
  }
  
  /**
   * @see AnnotationMonitoredAttributeSource#getMonitoredAttribute(Method, Class)
   */
  public void testInterfaceNotAnnotated() {
    final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
    final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
        findMethod("notAnnotated", AnnotatedInterface.class), AnnotatedTargetSubclass.class);
    
    assertEquals("notAnnotated", monitoredAttribute.getMonitorName());
    assertFalse(monitoredAttribute.isIncludeArguments());
    assertFalse(monitoredAttribute.isIncludeResult());
  }
  
  /**
   * @see AnnotationMonitoredAttributeSource#getMonitoredAttribute(Method, Class)
   */
  public void testLevelAnnotation() {
    final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
    final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
        findMethod("levelAnnotation"), AnnotatedTarget.class);
    assertEquals("monitor_name", monitoredAttribute.getMonitorName());
    assertEquals("debug", monitoredAttribute.getLevelStr());
  }
  
  /**
   * @see AnnotationMonitoredAttributeSource#getMonitoredAttribute(Method, Class)
   */
  public void testMethodOne() {
    final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
    final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
        findMethod("methodOne"), AnnotatedTarget.class);
    assertEquals("methodOne", monitoredAttribute.getMonitorName());
    assertFalse(monitoredAttribute.isIncludeArguments());
    assertFalse(monitoredAttribute.isIncludeResult());
    
  }
  
  /**
   * @see AnnotationMonitoredAttributeSource#getMonitoredAttribute(Method, Class)
   */
  public void testMethodThree() {
    final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
    final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
        findMethod("methodThree"), AnnotatedTarget.class);
    assertEquals("I_HAVE_ALL", monitoredAttribute.getMonitorName());
    assertTrue(monitoredAttribute.isIncludeArguments());
    assertTrue(monitoredAttribute.isIncludeResult());
  }
  
  /**
   * @see AnnotationMonitoredAttributeSource#getMonitoredAttribute(Method, Class)
   */
  public void testMethodTwo() {
    final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
    final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
        findMethod("methodTwo"), AnnotatedTarget.class);
    assertEquals("I_HAVE_A_NAME", monitoredAttribute.getMonitorName());
    assertFalse(monitoredAttribute.isIncludeArguments());
    assertFalse(monitoredAttribute.isIncludeResult());
  }
  
  /**
   * @see AnnotationMonitoredAttributeSource#getMonitoredAttribute(Method, Class)
   */
  public void testSubclass() {
    final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
    final MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
        findMethod("methodThree", AnnotatedTarget.class), AnnotatedTargetSubclass.class);
    assertEquals("I_HAVE_ALL", monitoredAttribute.getMonitorName());
    assertTrue(monitoredAttribute.isIncludeArguments());
    assertTrue(monitoredAttribute.isIncludeResult());
  }
  
  /**
   * @see AnnotationMonitoredAttributeSource#getMonitoredAttribute(Method, Class)
   */
  public void testTypeLevelAnnotation() {
    final AnnotationMonitoredAttributeSource target = new AnnotationMonitoredAttributeSource();
    MonitoredAttribute monitoredAttribute = target.getMonitoredAttribute(
        findMethod("methodOne", TypeLevelAnnotated.class), TypeLevelAnnotated.class);
    assertEquals("methodOne", monitoredAttribute.getMonitorName());
    assertFalse(monitoredAttribute.isIncludeArguments());
    assertFalse(monitoredAttribute.isIncludeResult());
    monitoredAttribute = target.getMonitoredAttribute(
        findMethod("notAnnotated", TypeLevelAnnotated.class), TypeLevelAnnotated.class);
    assertEquals("notAnnotated", monitoredAttribute.getMonitorName());
    assertFalse(monitoredAttribute.isIncludeArguments());
    assertFalse(monitoredAttribute.isIncludeResult());
  }
  
  private static interface AnnotatedInterface {
    @Monitored
    public void imethod();
    
    public void notAnnotated();
  }
  
  private static class AnnotatedTarget {
    @SuppressWarnings("unused")
    @Monitored(value = "monitor_name", levelStr = "debug")
    public void levelAnnotation() {
    }
    
    @SuppressWarnings("unused")
    @Monitored
    public void methodOne() {
    }
    
    @Monitored(value = "I_HAVE_ALL", includeArguments = true, includeResult = true)
    public String methodThree(final String test) {
      return test;
    }
    
    @SuppressWarnings("unused")
    @Monitored("I_HAVE_A_NAME")
    public void methodTwo() {
    }
  }
  
  private static class AnnotatedTargetSubclass extends AnnotatedTarget implements
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
  private static class TypeLevelAnnotated extends AnnotatedTarget {
    @SuppressWarnings("unused")
    public void notAnnotated() {
    }
  }
}
