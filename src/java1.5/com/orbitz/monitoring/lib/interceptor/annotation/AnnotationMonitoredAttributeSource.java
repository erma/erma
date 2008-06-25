package com.orbitz.monitoring.lib.interceptor.annotation;

import com.orbitz.monitoring.api.annotation.Monitored;
import com.orbitz.monitoring.lib.interceptor.MonitoredAttribute;
import com.orbitz.monitoring.lib.interceptor.MonitoredAttributeSource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

/**
 * @author Ray Krueger
 */
public class AnnotationMonitoredAttributeSource implements MonitoredAttributeSource {

    private static final Logger log = Logger.getLogger(AnnotationMonitoredAttributeSource.class);

    public MonitoredAttribute getMonitoredAttribute(Method method, Class targetClass) {
        Monitored annotation = getAnnotation(method, targetClass);

        if (annotation != null) {
        return new MonitoredAttribute(
                getMonitorName(method, annotation),
                getLevelStr(annotation),
                includeResult(annotation),
                includeArguments(annotation)
        );
        }

        return null;

    }

    private String getMonitorName(Method method, Monitored annotation) {
        if (annotation == null || StringUtils.trimToNull(annotation.value()) == null) {
            return method.getName();
        } else {
            return annotation.value();
        }
    }

    private boolean includeResult(Monitored annotation) {
        if (annotation != null) {
            return annotation.includeResult();
        }
        return false;
    }

    private boolean includeArguments(Monitored annotation) {
        if (annotation != null) {
            return annotation.includeArguments();
        }
        return false;
    }

    private String getLevelStr(Monitored annotation) {
        if (annotation != null) {
            return annotation.levelStr();
        }
        return "info";
    }

    /**
     * Try to get the annotation off of the method.
     * If that fails, get the annotation of of the declaring class.
     * if it is specified on the targetClass itself, use that.
     *
     * @param method
     * @param targetClass
     * @return Annotation found or null
     */
    @SuppressWarnings("unchecked")
    protected Monitored getAnnotation(Method method, Class targetClass) {
        Monitored annotation = AnnotationUtils.findAnnotation(method, Monitored.class);
        if (annotation == null) {

            if (method.getDeclaringClass().isAnnotationPresent(Monitored.class)) {
                annotation = method.getDeclaringClass().getAnnotation(Monitored.class);
            }

            try {
                if (!method.getName().equals("clone")) {

                    Method targetMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
                    if (targetMethod.isAnnotationPresent(Monitored.class)) {
                        annotation = (Monitored) targetMethod.getAnnotation(Monitored.class);
                    }
                }
            } catch (NoSuchMethodException e) {
                log.warn("This should not happen: cannot get declared method from target class", e);
            }

            if (targetClass.isAnnotationPresent(Monitored.class)) {
                annotation = (Monitored) targetClass.getAnnotation(Monitored.class);
            }

        }

        return annotation;
    }
}
