package com.orbitz.monitoring.lib.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * @author Ray Krueger
 */
public class TransactionMonitorInterceptor implements MethodInterceptor {

    public final String MONITOR_RESULT_NAME = "result";
    public final String MONITOR_ARGUMENTS_NAME = "arguments";

    private static final Logger log = Logger.getLogger(TransactionMonitorInterceptor.class);

    private final MonitoredAttributeSource monitoredAttributeSource;

    public TransactionMonitorInterceptor(MonitoredAttributeSource monitoredAttributeSource) {
        Validate.notNull(monitoredAttributeSource, "monitredAttributeSource is a required constructor argument");
        this.monitoredAttributeSource = monitoredAttributeSource;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {

        TransactionMonitor monitor = createTransactionMonitor(invocation);

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
        } catch (Throwable t) {
            log.debug("Monitor failed due to [" + t + "]");
            monitor.failedDueTo(t);
            throw t;
        } finally {
            log.debug("Monitor done");
            monitor.done();
        }

    }

    protected TransactionMonitor createTransactionMonitor(MethodInvocation invocation) {
        String monitorName = getMonitorName(invocation);
        MonitoringLevel level = getMonitoringLevel(invocation);

        log.debug("Creating monitor for [" + monitorName + "]");
        return new TransactionMonitor(invocation.getMethod().getDeclaringClass(), monitorName, level);
    }

    protected MonitoringLevel getMonitoringLevel(MethodInvocation invocation) {
        MonitoredAttribute att = getAttribute(invocation);

        if (att == null) {
            return MonitoringLevel.INFO;
        } else {
            return MonitoringLevel.toLevel(att.getLevelStr());
        }
    }

    protected String getMonitorName(MethodInvocation invocation) {

        MonitoredAttribute att = getAttribute(invocation);

        if (att == null || StringUtils.trimToNull(att.getMonitorName()) == null) {
            return invocation.getMethod().getName();
        } else {
            return att.getMonitorName();
        }
    }

    protected MonitoredAttribute getAttribute(MethodInvocation invocation) {
        Class targetClass = (invocation.getThis() != null) ? invocation.getThis().getClass() : invocation.getMethod()
                .getDeclaringClass();
        return monitoredAttributeSource.getMonitoredAttribute(invocation.getMethod(), targetClass);
    }

    protected boolean includeResult(MethodInvocation invocation) {

        MonitoredAttribute attribute = getAttribute(invocation);

        if (attribute != null) {
            return attribute.isIncludeResult();
        }
        return false;
    }

    protected boolean includeArguments(MethodInvocation invocation) {

        MonitoredAttribute attribute = getAttribute(invocation);

        if (attribute != null) {
            return attribute.isIncludeArguments();
        }
        return false;
    }

    public MonitoredAttributeSource getMonitoredAttributeSource() {
        return monitoredAttributeSource;
    }
}
