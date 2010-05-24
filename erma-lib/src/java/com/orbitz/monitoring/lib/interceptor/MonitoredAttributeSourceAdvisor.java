package com.orbitz.monitoring.lib.interceptor;

import org.apache.log4j.Logger;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.reflect.Method;

/**
 * @author Ray Krueger
 */
public class MonitoredAttributeSourceAdvisor extends StaticMethodMatcherPointcutAdvisor {

    private static final Logger log = Logger.getLogger(MonitoredAttributeSourceAdvisor.class);
    private final MonitoredAttributeSource monitoredAttributeSource;

    public MonitoredAttributeSourceAdvisor(MonitoredAttributeSource monitoredAttributeSource) {
        this(new TransactionMonitorInterceptor(monitoredAttributeSource));
    }

    public MonitoredAttributeSourceAdvisor(TransactionMonitorInterceptor interceptor) {
        setAdvice(interceptor);
        this.monitoredAttributeSource = interceptor.getMonitoredAttributeSource();

        //Exclude java.lang.Object methods by default
        setClassFilter(new ClassFilter() {
            public boolean matches(Class clazz) {
                return !clazz.isAssignableFrom(Object.class);
            }
        });
    }

    public boolean matches(Method method, Class targetClassCandidate) {

        Class targetClass = (targetClassCandidate != null) ? targetClassCandidate : method.getDeclaringClass();

        boolean match = getClassFilter().matches(targetClass)
                && monitoredAttributeSource.getMonitoredAttribute(method, targetClass) != null;

        if (log.isDebugEnabled() && match) {
            log.debug("Pointcut match: [" + method + "]");
        }

        return match;

    }


}
