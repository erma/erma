package com.orbitz.monitoring.api.template;

import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * The {@link com.orbitz.monitoring.api.template.TransactionMonitorTemplate} will build a transaction monitor and pass
 * it to the {@link #doInMonitor(com.orbitz.monitoring.api.monitor.TransactionMonitor)} callback method.
 * <p/>
 * Generally, TransactionMonitorCallback implementors do not even need to use the monitor passed to them.
 * <p/>
 * Callback implementors <b>do not</b> need to make calls to
 * {@link com.orbitz.monitoring.api.monitor.TransactionMonitor#succeeded()} or
 * {@link com.orbitz.monitoring.api.monitor.TransactionMonitor#done()}
 * <p/>
 * The TransactionMonitorCallback does not attempt to handle checked exceptions.
 * Any {@link RuntimeException} thrown will be handled with a call to {@link TransactionMonitor#failedDueTo(Throwable)}
 *
 * @author Ray Krueger
 */
public interface TransactionMonitorCallback {

  /**
   * Execute logic within a try/catch/finally block using the given {@link TransactionMonitor}.
   * <b>Note</b> that the monitor given has already been declared as {@link TransactionMonitor#succeeded()}.
   * If you wish to declare the monitor a failure either call {@link TransactionMonitor#failed()},
   * {@link TransactionMonitor#failedDueTo(Throwable)}, or throw a <code>RuntimeException</code>
   *
   * @param monitor {@link TransactionMonitor} to use
   * @return anything you wish to be returned from the template
   */
  Object doInMonitor(TransactionMonitor monitor);

}
