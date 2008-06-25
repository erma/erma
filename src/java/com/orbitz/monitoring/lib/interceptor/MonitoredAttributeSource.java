package com.orbitz.monitoring.lib.interceptor;

import java.lang.reflect.Method;

/**
 * @author Ray Krueger
 */
public interface MonitoredAttributeSource {
    MonitoredAttribute getMonitoredAttribute(Method method, Class targetClass);
}
