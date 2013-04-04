package com.orbitz.monitoring.lib.interceptor.annotation;

import com.orbitz.monitoring.api.annotation.Monitored;
import com.orbitz.monitoring.lib.interceptor.MonitoredAttribute;
import com.orbitz.monitoring.lib.interceptor.MonitoredAttributeSource;
import java.lang.reflect.Method;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * @author Ray Krueger
 */
public class AnnotationMonitoredAttributeSource implements MonitoredAttributeSource {
  private static final Logger log = Logger.getLogger(AnnotationMonitoredAttributeSource.class);
  
  public MonitoredAttribute getMonitoredAttribute(final Method method,
      @SuppressWarnings("rawtypes") final Class targetClass) {
    final Monitored annotation = getAnnotation(method, targetClass);
    if (annotation != null) {
      return new MonitoredAttribute(getMonitorName(method, annotation), getLevelStr(annotation),
          includeResult(annotation), includeArguments(annotation));
    }
    return null;
  }
  
  private String getMonitorName(final Method method, final Monitored annotation) {
    if (annotation == null || StringUtils.trimToNull(annotation.value()) == null) {
      return method.getName();
    }
    else {
      return annotation.value();
    }
  }
  
  private boolean includeResult(final Monitored annotation) {
    if (annotation != null) {
      return annotation.includeResult();
    }
    return false;
  }
  
  private boolean includeArguments(final Monitored annotation) {
    if (annotation != null) {
      return annotation.includeArguments();
    }
    return false;
  }
  
  private String getLevelStr(final Monitored annotation) {
    if (annotation != null) {
      return annotation.levelStr();
    }
    return "info";
  }
  
  /**
   * Try to get the annotation off of the method. If that fails, get the annotation of of the
   * declaring class. if it is specified on the targetClass itself, use that.
   * @param method a method of the class from which to get the {@link Monitored} annotation
   * @param targetClass if targetClass contains a method with the same signature as "method" and
   *        targetClass has a {@link Monitored} annotation, targetClass' annotation will override
   *        "method"'s annotation
   * @return Annotation found or null
   */
  protected Monitored getAnnotation(final Method method, final Class<?> targetClass) {
    Monitored annotation = AnnotationUtils.findAnnotation(method, Monitored.class);
    if (annotation == null) {
      
      if (method.getDeclaringClass().isAnnotationPresent(Monitored.class)) {
        annotation = method.getDeclaringClass().getAnnotation(Monitored.class);
      }
      
      try {
        if (!method.getName().equals("clone")) {
          
          final Method targetMethod = targetClass.getMethod(method.getName(),
              method.getParameterTypes());
          if (targetMethod.isAnnotationPresent(Monitored.class)) {
            annotation = targetMethod.getAnnotation(Monitored.class);
          }
        }
      }
      catch (final NoSuchMethodException e) {
        log.warn("This should not happen: cannot get declared method from target class", e);
      }
      
      if (targetClass.isAnnotationPresent(Monitored.class)) {
        annotation = targetClass.getAnnotation(Monitored.class);
      }
      
    }
    
    return annotation;
  }
}
