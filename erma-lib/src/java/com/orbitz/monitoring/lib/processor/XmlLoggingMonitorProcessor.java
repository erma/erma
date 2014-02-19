package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.lib.renderer.XmlMonitorRenderer;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A monitor processor that logs the monitors collected to a log4j logger in an
 * XML format.
 *
 * @author Doug Barth
 */
public class XmlLoggingMonitorProcessor extends MonitorProcessorAdapter {
  // ** STATIC/FINAL DATA ***************************************************
  protected Logger log = Logger.getLogger(XmlLoggingMonitorProcessor.class);

  private List allowedAttributes = new ArrayList();

  // ** PUBLIC METHODS ******************************************************
  public void process(Monitor monitor) {
    XmlMonitorRenderer renderer = null;

    renderer = new XmlMonitorRenderer(allowedAttributes);

    String logString = renderer.renderMonitor(monitor);
    log.info(logString);
  }

  public void setAllowedAttributes(List allowedAttributes) {
    this.allowedAttributes = allowedAttributes;
  }
}
