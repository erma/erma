package com.orbitz.monitoring.lib.interceptor;

import com.google.common.annotations.VisibleForTesting;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Designed to intercept methods annotated with {@link com.orbitz.monitoring.api.annotation.Monitored}, applying
 * {@link TransactionMonitor transaction monitors} to them
 * @author Ray Krueger
 */
public class TransactionMonitorInterceptor implements MethodInterceptor {
    private static final Logger log = Logger.getLogger(TransactionMonitorInterceptor.class);
    /**
     * If {@link com.orbitz.monitoring.api.annotation.Monitored#includeArguments()} is true, the name of the monitor property that will
     * contain the arguments of the intercepted method
     */
    public static final String MONITOR_ARGUMENTS_NAME = "arguments";
    /**
     * If {@link com.orbitz.monitoring.api.annotation.Monitored#includeResult()} is true, the name of the monitor property that will
     * contain the return value of the intercepted method
     */
    public static final String MONITOR_RESULT_NAME = "result";
    
    private final MonitoredAttributeSource monitoredAttributeSource;
    /**
     * TODO: Version 5: Change the default to false so that annotation monitoring works the same way
     * as regular monitoring.
     */
    private boolean prependClassName = true;
    
    /**
     * Creates a new transaction monitor interceptor
     * @param monitoredAttributeSource provides {@link MonitoredAttribute MonitoredAttributes}
     */
    public TransactionMonitorInterceptor(final MonitoredAttributeSource monitoredAttributeSource) {
        Validate.notNull(monitoredAttributeSource,
                "monitredAttributeSource is a required constructor argument");
        this.monitoredAttributeSource = monitoredAttributeSource;
    }
    
    @VisibleForTesting
    TransactionMonitor createTransactionMonitor(final MethodInvocation invocation) {
        final MonitoringLevel level = getMonitoringLevel(invocation);
        final String nameFromAnnotation = getNameFromAnnotation(invocation);
        if (this.prependClassName || (nameFromAnnotation == null)) {
            final String name = nameFromAnnotation == null ? invocation.getMethod().getName()
                    : nameFromAnnotation;
            return new TransactionMonitor(invocation.getMethod().getDeclaringClass(), name, level);
        }
        else {
            return new TransactionMonitor(nameFromAnnotation, level);
        }
    }
    
    private MonitoredAttribute getAttribute(final MethodInvocation invocation) {
        final Object invocationThis = invocation.getThis();
        final Method method = invocation.getMethod();
        final Class<?> targetClass = (invocationThis != null) ? invocationThis.getClass() : method
                .getDeclaringClass();
        return monitoredAttributeSource.getMonitoredAttribute(method, targetClass);
    }
    
    /**
     * Gets the attribute source, responsible for creating {@link MonitoredAttribute monitored
     * attributes}
     * @return the attribute source
     */
    public MonitoredAttributeSource getMonitoredAttributeSource() {
        return monitoredAttributeSource;
    }
    
    private MonitoringLevel getMonitoringLevel(final MethodInvocation invocation) {
        final MonitoredAttribute att = getAttribute(invocation);
        
        if (att == null) {
            return MonitoringLevel.INFO;
        }
        else {
            return MonitoringLevel.toLevel(att.getLevelStr());
        }
    }
    
    @VisibleForTesting
    String getMonitorName(final MethodInvocation invocation) {
        final MonitoredAttribute attribute = getAttribute(invocation);
        if (attribute == null || StringUtils.trimToNull(attribute.getMonitorName()) == null) {
            return invocation.getMethod().getName();
        }
        else {
            return attribute.getMonitorName();
        }
    }
    
    private String getNameFromAnnotation(final MethodInvocation invocation) {
        final MonitoredAttribute attribute = getAttribute(invocation);
        final String monitorName = attribute.getMonitorName();
        if (attribute == null || StringUtils.trimToNull(monitorName) == null) {
            return null;
        }
        return monitorName;
    }
    
    private boolean includeArguments(final MethodInvocation invocation) {
        final MonitoredAttribute attribute = getAttribute(invocation);
        
        if (attribute != null) {
            return attribute.isIncludeArguments();
        }
        return false;
    }
    
    private boolean includeResult(final MethodInvocation invocation) {
        final MonitoredAttribute attribute = getAttribute(invocation);
        
        if (attribute != null) {
            return attribute.isIncludeResult();
        }
        return false;
    }
    
    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
     */
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        
        final TransactionMonitor monitor = createTransactionMonitor(invocation);
        
        try {
            if (includeArguments(invocation)) {
                monitor.set(MONITOR_ARGUMENTS_NAME, invocation.getArguments());
            }
            Object result;
            result = invocation.proceed();
            log.debug("Monitor success");
            
            if (includeResult(invocation)) {
                monitor.set(MONITOR_RESULT_NAME, result);
            }
            
            monitor.succeeded();
            
            return result;
        }
        catch (final Throwable t) {
            log.debug("Monitor failed due to [" + t + "]");
            monitor.failedDueTo(t);
            throw t;
        }
        finally {
            log.debug("Monitor done");
            monitor.done();
        }
        
    }
    
    /**
     * Indicates whether the fully qualified class name will be prepended to monitor names
     * @return true if the class name will be prepended, false otherwise
     */
    public boolean isPrependClassName() {
        return prependClassName;
    }
    
    /**
     * Sets a flag indicating whether the fully qualified class name will be prepended to monitor
     * names
     * @param prependClassName true if the class name will be prepended, false otherwise
     */
    public void setPrependClassName(final boolean prependClassName) {
        this.prependClassName = prependClassName;
    }
}
