package com.orbitz.monitoring.lib.interceptor;

import java.lang.reflect.Method;

/**
 * {@link MonitoredAttributeSource} that always returns a {@link MonitoredAttribute}
 * using the method name as the monitor name.
 *
 * @author Ray Krueger
 */
public class MatchAlwaysMonitoredAttributeSource implements MonitoredAttributeSource {

    private final boolean includeResult;
    private final boolean includeArguments;

    /**
     * Never sets the result or arguments on the monitor.
     */
    public MatchAlwaysMonitoredAttributeSource() {
        this(false, false);
    }

    /**
     * Enable result and arguments on the monitor for all methods intercepted.
     *
     * @param includeResult
     * @param includeArguments
     */
    public MatchAlwaysMonitoredAttributeSource(boolean includeResult, boolean includeArguments) {
        this.includeResult = includeResult;
        this.includeArguments = includeArguments;
    }

    public MonitoredAttribute getMonitoredAttribute(Method method, Class targetClass) {
        return new MonitoredAttribute(method.getName(), includeResult, includeArguments);
    }
}
