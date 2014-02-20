package com.orbitz.monitoring.lib.processor.statsd;

import static com.orbitz.monitoring.api.Attribute.FAILED;
import static com.orbitz.monitoring.api.Attribute.LATENCY;
import static com.orbitz.monitoring.api.Attribute.NAME;
import static com.orbitz.monitoring.api.Attribute.VALUE;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.lib.processor.MonitorProcessorAdapter;
import com.orbitz.statsd.StatsdClient;

/**
 * Sends ERMA monitor data to a statsd server
 * 
 * @author orawlings
 *
 */
public class StatsdMonitorProcessor extends MonitorProcessorAdapter {
  
  private StatsdClient _statsdClient;
  private String _name;
  
  public StatsdMonitorProcessor(StatsdClient statsdClient) {
    this._statsdClient = statsdClient;
  }

  public void process(Monitor monitor) {
    if (monitor == null) {
      return;
    }

    if (monitor.hasAttribute(LATENCY)) {
      _statsdClient.timing(monitor.getAsString(NAME), monitor.getAsInt(LATENCY));
    } else if (monitor.hasAttribute(VALUE)) {
      _statsdClient.gauge(monitor.getAsString(NAME), (int) monitor.getAsInt(VALUE));
    } else {
      _statsdClient.increment(monitor.getAsString(NAME));
    }
    
    if (monitor.getAsBoolean(FAILED, false)) {
      _statsdClient.increment(monitor.getAsString(NAME) + ".failed");
    }
  }

  public String getName() {
    return _name;
  }
  
  public void setName(String name) {
    this._name = name;
  }
  
}
