package com.orbitz.monitoring.lib.interceptor;

import org.apache.commons.lang.Validate;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;

import java.util.Properties;

/**
 * Produces proxies for the given Target
 *
 * @author Ray Krueger
 */
public class TransactionMonitorProxyFactoryBean extends AbstractSingletonProxyFactoryBean {

  private TransactionMonitorInterceptor transactionMonitorInterceptor;

  public TransactionMonitorProxyFactoryBean() {
    this.transactionMonitorInterceptor = new TransactionMonitorInterceptor(
        new MatchAlwaysMonitoredAttributeSource());
  }

  public TransactionMonitorProxyFactoryBean(MonitoredAttributeSource monitoredAttributeSource) {
    setMonitoredAttributeSource(monitoredAttributeSource);
  }

  public TransactionMonitorProxyFactoryBean(TransactionMonitorInterceptor interceptor) {
    setTransactionMonitorInterceptor(interceptor);
  }

  public void setTransactionMonitorInterceptor(TransactionMonitorInterceptor interceptor) {
    Validate.notNull(interceptor, "transactionMonitorInterceptor is required");
    this.transactionMonitorInterceptor = interceptor;
  }

  public void setMonitoredAttributeSource(MonitoredAttributeSource source) {
    Validate.notNull(source, "monitoredAttributeSource is required");
    this.transactionMonitorInterceptor = new TransactionMonitorInterceptor(source);
  }

  public void setTransactionMonitorAttributes(Properties props) {
    Validate.notNull(props, "transactionMonitorAttributes must not be null");
    NameMatchMonitoredAttributeSource attributeSource = new NameMatchMonitoredAttributeSource();
    attributeSource.setProperties(props);
    this.transactionMonitorInterceptor = new TransactionMonitorInterceptor(attributeSource);
  }

  protected Object createMainInterceptor() {
    return new MonitoredAttributeSourceAdvisor(transactionMonitorInterceptor);
  }
}
