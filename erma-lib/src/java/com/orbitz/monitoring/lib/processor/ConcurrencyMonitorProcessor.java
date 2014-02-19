package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

import com.google.common.collect.Maps;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link com.orbitz.monitoring.api.MonitorProcessor} that maintains counts of concurrently
 * executing transactions
 * @author Matt O'Keefe
 */

public class ConcurrencyMonitorProcessor extends MonitorProcessorAdapter {
  private static final Logger log = Logger.getLogger(ConcurrencyMonitorProcessor.class);
  private static final Map<String, Integer> map = new HashMap<String, Integer>();
  
  private boolean isEnabled = true;
  
  @Override
  public void monitorStarted(final Monitor monitor) {
    if (isEnabled && TransactionMonitor.class.isAssignableFrom(monitor.getClass())) {
      String name = monitor.getAsString(Attribute.NAME);
      synchronized (map) {
        Integer cnt = map.get(name);
        if (cnt == null) {
          map.put(name, Integer.valueOf(1));
        } else {
          int count = cnt.intValue();
          map.put(name, Integer.valueOf(count + 1));
        }
      }
    }
  }
  
  @Override
  public void process(final Monitor monitor) {
    if (isEnabled && TransactionMonitor.class.isAssignableFrom(monitor.getClass())) {
      String name = monitor.getAsString(Attribute.NAME);
      synchronized (map) {
        Integer cnt = map.get(name);
        if (cnt == null) {
          log.warn("No count available for Monitor named " + name);
        } else {
          int count = cnt.intValue();
          map.put(name, Integer.valueOf(count - 1));
          monitor.set("concurrencyCount", count);
          
          if (count == 1) {
            map.remove(name);
          }
        }
      }
    }
  }
  
  /**
   * Gets the map of counts, in which keys are attribute names and values are counts
   * @return the new map
   */
  public Map<String, Integer> getAll() {
    synchronized (map) {
      return Maps.newHashMap(map);
    }
  }
  
  public boolean isEnabled() {
    return isEnabled;
  }
  
  public void setEnabled(final boolean enabled) {
    isEnabled = enabled;
  }
  
  public static void clear() {
    synchronized (map) {
      map.clear();
    }
  }
}
