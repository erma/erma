package com.orbitz.monitoring.lib.renderer;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * XmlMonitorRenderer uses xstream to render a Monitor to XML format.
 *
 * @author Operations Architecture 
 */
public class XmlMonitorRenderer {

  private static final Logger logger = Logger.getLogger(XmlMonitorRenderer.class);

  private static final String INDENT_STRING = "  ";

  private List allowedAttributes;
  private int maxCharacters = -1;
  private boolean prettyPrint = false;

  /**
   * Constructor.
   *
   * @param allowedAttributes List of attributes to allow in rendered XML
   */
  public XmlMonitorRenderer(List allowedAttributes) {
    this.allowedAttributes = allowedAttributes;
  }

  public List getAllowedAttributes() {
    return allowedAttributes;
  }

  public static String getIndentString() {
    return INDENT_STRING;
  }

  public int getMaxCharacters() {
    return maxCharacters;
  }

  

  /**
   * Renders a Monitor with attributes filtered to specified set.
   *
   * @param monitor ERMA monitor
   * @return a String of XML
   */
  public String renderMonitor(Monitor monitor) {

    if (monitor == null) {
      return "<null/>";
    }

    HierarchicalStreamWriter xmlWriter;
    StringWriter stringWriter = new StringWriter();

    if (prettyPrint) {
      xmlWriter = new PrettyPrintWriter(stringWriter, INDENT_STRING);
    } else {
      xmlWriter = new CompactWriter(stringWriter);
    }

    writeMonitor(xmlWriter, monitor);

    if((maxCharacters != -1) && (stringWriter.getBuffer().length() > maxCharacters)) {
      // clear the buffer and reuse the streams
      StringBuffer sb = stringWriter.getBuffer();
      int length = sb.length();
      sb.delete(0, length);
      // required inorder to avoid the state maintained by the previous rendering
      xmlWriter = new PrettyPrintWriter(stringWriter, INDENT_STRING);
      writeMonitorTerse(xmlWriter, monitor, length);
    }

    return stringWriter.toString();
  }

  public void setMaxCharacters(int maxCharacters) {
    this.maxCharacters = maxCharacters;
  }

  public boolean isPrettyPrint() {
    return prettyPrint;
  }

  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  protected void writeMonitorTerse(HierarchicalStreamWriter writer, Monitor monitor, int length) {
    String monitorClassName = monitor.getClass().getName();
    int classIdx = monitorClassName.lastIndexOf('.');
    if (classIdx >= 0) {
      monitorClassName = monitorClassName.substring(classIdx + 1);
    }

    String message = "Error rendering event pattern: length is " + length + ", max length is " 
        +  maxCharacters;

    writer.startNode(monitorClassName);
    appendMonitorDataAsAttributes(writer, monitor);
    writer.setValue("\n" + INDENT_STRING + message + "\n");
    writer.endNode();

    logger.warn(message);
  }

  // private methods ********************************************************

  protected void writeMonitor(HierarchicalStreamWriter writer, Monitor monitor) {
    String monitorClassName = monitor.getClass().getName();
    int classIdx = monitorClassName.lastIndexOf('.');
    if (classIdx >= 0) {
      monitorClassName = monitorClassName.substring(classIdx + 1);
    }

    writer.startNode(monitorClassName);
    appendMonitorDataAsAttributes(writer, monitor);

    // recursively add all child monitors to dom tree
    if (CompositeMonitor.class.isAssignableFrom(monitor.getClass())) {
      CompositeMonitor cm = (CompositeMonitor) monitor;
      Collection childMonitors = cm.getChildMonitors();
      Iterator childIterator = childMonitors.iterator();
      while (childIterator.hasNext()) {
        Monitor childMonitor = (Monitor) childIterator.next();
        writeMonitor(writer, childMonitor);
      }
    }

    writer.endNode();
  }

  protected void appendMonitorDataAsAttributes(HierarchicalStreamWriter writer, Monitor monitor) {
    Iterator it = allowedAttributes.iterator();
    while (it.hasNext()) {
      String attributeName = (String) it.next();
      if (monitor.hasAttribute(attributeName)) {
        String attributeValue = monitor.getAsString(attributeName);
        if (attributeValue == null) {
          writer.addAttribute(attributeName, "null");
        } else {
          if (attributeValue.indexOf("LazyDynaBean") > -1) {
            DynaBean dynaBean = (DynaBean) monitor.get(attributeName);
            Object clazz = dynaBean.get("class");
            if (clazz != null) {
              attributeValue = clazz.toString();
            }
          }
          int idx = attributeValue.indexOf("\n");
          if(idx > -1) {
            attributeValue = attributeValue.substring(0, idx);
          }
          writer.addAttribute(attributeName, attributeValue);
        }
      }
    }
  }
}
