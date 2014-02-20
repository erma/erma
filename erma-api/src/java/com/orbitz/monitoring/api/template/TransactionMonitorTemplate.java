package com.orbitz.monitoring.api.template;

import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * Encapsulates the logic involed in properly handling a TransactionMonitor.
 * It also avoids breaking that logic by eliminating the cancer pattern of the logic itself.
 * <p/>
 * A new {@link TransactionMonitor} will be built using the given Class and Monitor name, handed to 
 * the callback for use.
 *
 * @author Ray Krueger
 */
public class TransactionMonitorTemplate {

  public static final TransactionMonitorTemplate INSTANCE = new TransactionMonitorTemplate();

  /**
   * Builds a {@link TransactionMonitor} with just a monitor name and exeuctes the given callback 
   * within the monitored try/catch/finally block.
   *
   * @param monitorName MonitorName used to construct a {@link TransactionMonitor}
   * @param callback {@link TransactionMonitorCallback} to execute
   * @return anything the callback returns
   */
  public Object doInMonitor(String monitorName, TransactionMonitorCallback callback) {
    return doInMonitor(null, monitorName, callback);
  }

  /**
   * Builds a {@link TransactionMonitor} with a class name and monitor name; and exeuctes the given 
   * callback within the monitored try/catch/finally block.
   *
   * @param cls class to use when constructing the {@link TransactionMonitor}
   * @param monitorName MonitorName used to construct a {@link TransactionMonitor}
   * @param callback {@link TransactionMonitorCallback} to execute
   * @return anything the callback returns
   */
  public Object doInMonitor(Class cls, String monitorName, TransactionMonitorCallback callback) {
    TransactionMonitor monitor = createMonitor(cls, monitorName);
    return executeCallback(callback, monitor);
  }

  /**
   * Hook method for overriding how the template creates {@link TransactionMonitor}s
   *
   * @param cls class to use (may be null)
   * @param monitorName should not be null
   * @return {@link TransactionMonitor} to use
   */
  protected TransactionMonitor createMonitor(Class cls, String monitorName) {
    TransactionMonitor monitor;
    if (cls == null) {
      monitor = new TransactionMonitor(monitorName);
    } else {
      monitor = new TransactionMonitor(cls, monitorName);
    }
    return monitor;
  }

  /**
   * Override this method to override how the Template executes the callback.
   * You probably shouldn't need to override this; but who am I to judge?
   *
   * @param callback {@link TransactionMonitorCallback} to execute
   * @param monitor {@link TransactionMonitor} to use
   * @return anything the callback returns
   */
  protected Object executeCallback(TransactionMonitorCallback callback, 
      TransactionMonitor monitor) {
    try {
      // This looks wrong but it's intentional. We want to allow the callback to
      // mark the monitor as failed in its code, even if it didn't throw an exception.
      // Therefore, we're turning the fail-by-default TransactionMonitor into
      // a succeeded-by-default object.
      monitor.succeeded();
      return callback.doInMonitor(monitor);
    } catch (RuntimeException e) {
      monitor.failedDueTo(e);
      throw e;
    } catch (Error e) {
      monitor.failedDueTo(e);
      throw e;
    } finally {
      monitor.done();
    }
  }

}
