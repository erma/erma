package com.orbitz.monitoring.lib.interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.reflect.Method;

/**
 * Provides support for method interception in Spring
 * @author Ray Krueger
 */
public class MonitoredAttributeSourceAdvisor extends StaticMethodMatcherPointcutAdvisor {
    private static final long serialVersionUID = 1L;
    
    private final TransactionMonitorInterceptor interceptor;
    private final MonitoredAttributeSource monitoredAttributeSource;
    
    /**
     * Creates a new monitor pointcut advisor that ignores {@link Object} methods
     * @param monitoredAttributeSource provides access to {@link MonitoredAttribute monitored
     *        attributes} for a {@link TransactionMonitorInterceptor}
     */
    public MonitoredAttributeSourceAdvisor(final MonitoredAttributeSource monitoredAttributeSource) {
        this(new TransactionMonitorInterceptor(monitoredAttributeSource));
    }
    
    /**
     * Creates a new monitor pointcut advisor that ignores {@link Object} methods<br />
     * TODO: Version 5: Remove this constructor or reduce its visibility to default
     * @deprecated Use
     *             {@link MonitoredAttributeSourceAdvisor#MonitoredAttributeSourceAdvisor(MonitoredAttributeSource)}
     *             instead
     * @param interceptor the interceptor to apply to {@link com.orbitz.monitoring.api.annotation.Monitored} annotated methods
     */
    @Deprecated
    public MonitoredAttributeSourceAdvisor(final TransactionMonitorInterceptor interceptor) {
        this.interceptor = interceptor;
        setAdvice(this.interceptor);
        this.monitoredAttributeSource = interceptor.getMonitoredAttributeSource();
        setClassFilter(new ClassFilter() {
            @SuppressWarnings({"rawtypes", "unchecked"})
            public boolean matches(final Class clazz) {
                return !clazz.isAssignableFrom(Object.class);
            }
        });
    }
    
    /**
     * Indicates whether the fully qualified class name will be prepended to monitor names
     * @return true if the class name will be prepended, false otherwise
     */
    public boolean isPrependClassName() {
        return this.interceptor.isPrependClassName();
    }
    
    /**
     * @see org.springframework.aop.MethodMatcher#matches(java.lang.reflect.Method, java.lang.Class)
     */
    public boolean matches(final Method method,
            @SuppressWarnings("rawtypes") final Class targetClassCandidate) {
        final Class<?> targetClass = (targetClassCandidate != null) ? targetClassCandidate : method
                .getDeclaringClass();
        final boolean match = getClassFilter().matches(targetClass)
                && monitoredAttributeSource.getMonitoredAttribute(method, targetClass) != null;
        return match;
    }
    
    /**
     * Sets a flag indicating whether the fully qualified class name will be prepended to monitor
     * names
     * @param prependClassName true if the class name will be prepended, false otherwise
     */
    public void setPrependClassName(final boolean prependClassName) {
        this.interceptor.setPrependClassName(prependClassName);
    }
}
