package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;

/**
 * ResultCodeAnnotatingMonitorProcessor will set the resultCode attribute with the failureThrowable
 * class's name.
 * <br>
 * This is only done if a failureThrowable attribute is present but not a resultCode attribute.  We 
 * avoid overwriting custom resultCode attributes in the case where they've been set elsewhere.  The
 * purpose of this processor is to provide aggregation and alarming on "business failures" vs. 
 * "system failures".
 * <br>
 * For example:
 *
 * <pre>
 * <code>
 * public void search() {
 *   TransactionMonitor monitor = new TransactionMonitor(getClass(), "search");
 *   try {
 *     // some code
 *     monitor.succeeded();
 *   } catch (NoResultsFoundException e) {
 *     monitor.failedDueTo(e);
 *     throw e;
 *   } catch (Throwable t) {
 *     monitor.failedDueTo(t);
 *     throw new RuntimeException(t);
 *   } finally {
 *     monitor.done();
 *   }
 * }
 * </code>
 * </pre>
 *
 * Will result in com.orbitz.foo.Searcher.search
 *
 * This provides one metric for each possible outcome in addition to "count", "countFailed", and 
 * "pctFailed" aggregate exception metrics.
 *
 * @author Matt O'Keefe
 */
public class ResultCodeAnnotatingMonitorProcessor extends MonitorProcessorAdapter {

  public void process(Monitor monitor) {

    if (CompositeMonitor.class.isAssignableFrom(monitor.getClass()) 
        && !monitor.hasAttribute("resultCode")) {
      if (monitor.hasAttribute("failureThrowable")) {
        Throwable t = (Throwable) monitor.get("failureThrowable");
        while (t.getCause() != null) {
          t = t.getCause();
        }
        monitor.set("resultCode", t.getClass().getName());
      } else {
        monitor.set("resultCode", "success");
      }
    }
  }
}
