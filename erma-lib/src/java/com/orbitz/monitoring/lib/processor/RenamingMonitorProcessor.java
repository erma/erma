package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.monitor.AbstractCompositeMonitor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This {@link MonitorProcessor} will create a renamed monitor before passing it 
 * along to be handled by delegate {@link MonitorProcessor}. Renamed {@link Monitor}s 
 * can be prefixed by a dot (.) separated {@link String}-ification of various 
 * attributes of the {@link Monitor}.
 * <p/>For instance, this class can be configured to prefix each {@link Monitor} 
 * name with the value of its hostname and threadId attributes:
 * <pre>
 * {@code 
 * String[] attributes = new String[] { "hostname", "threadId" };
 * RenamingMonitorProcessor processor = new RenamingMonitorProcessor(delegate, 
 *     (List<String>) Arrays.asList(attributes));
 * }
 * </pre>
 * <p/>Thus, given a {@link Monitor} with a hostname attribute of 
 * <code>orawlings_example_com</code>, a threadId attribute of <code>29f7f9</code>, 
 * and a name of <code>some.work</code>, the delegate {@link MonitorProcessor} 
 * would handle a {@link Monitor} with name attribute equal to 
 * <code>orawlings_example_com.29f7f9.some.work</code>.
 * 
 * @author Ori Rawlings
 * 
 */
public class RenamingMonitorProcessor extends MonitorProcessorAdapter {

  private MonitorProcessor _delegate;
  private List<String> _attributesToPrefix;

  public RenamingMonitorProcessor(MonitorProcessor delegate, List<String> attributesToPrefix) {
    this._delegate = delegate;
    this._attributesToPrefix = attributesToPrefix;
  }

  @Override
  public void monitorCreated(Monitor monitor) {
    _delegate.monitorCreated(getRenamedMonitor(monitor));
  }

  @Override
  public void monitorStarted(Monitor monitor) {
    _delegate.monitorStarted(getRenamedMonitor(monitor));
  }

  @Override
  public void process(Monitor monitor) {
    _delegate.process(getRenamedMonitor(monitor));
  }

  private String getMonitorNamePrefix(Monitor monitor) {
    StringBuffer prefixBuffer = new StringBuffer();
    for (String attribute : _attributesToPrefix) {
      prefixBuffer.append(getAttributeAsStringWithDefault(monitor, attribute, "")).append('.');
    }
    return prefixBuffer.toString();
  }

  private String getAttributeAsStringWithDefault(Monitor monitor, 
      String attributeKey, String defaultValue) {
    if (!monitor.hasAttribute(attributeKey)) {
      return defaultValue;
    } else {
      return monitor.getAsString(attributeKey);
    }
  }

  private Monitor getRenamedMonitor(Monitor monitor) {
    String newName = getMonitorNamePrefix(monitor) + 
        getAttributeAsStringWithDefault(monitor, Attribute.NAME, "");
    Monitor renamedMonitor;
    if (CompositeMonitor.class.isAssignableFrom(monitor.getClass())) {
      renamedMonitor = new NonLifecycleMonitor(newName, monitor.getAll(), 
          ((CompositeMonitor) monitor).getChildMonitors());
    } else {
      renamedMonitor = new NonLifecycleMonitor(newName, monitor.getAll());
    }

    return renamedMonitor;
  }

  /**
   * This is monitor implementation does not interact with the {@link MonitoringEngine}
   * for basic lifecycle progression.
   * 
   */
  private static class NonLifecycleMonitor extends AbstractCompositeMonitor {
    
    public NonLifecycleMonitor(String name, Map<String, Object> inheritedAttributes, 
        Collection<Monitor> childMonitors) {
      super(name, inheritedAttributes);
      if (childMonitors != null) {
        for (Iterator<Monitor> iterator = childMonitors.iterator(); iterator.hasNext();) {
          Monitor child = (Monitor) iterator.next();
          addChildMonitor(child);
        }
      }
    }

    public NonLifecycleMonitor(String name, Map<String, Object> inheritedAttributes) {
      this(name, inheritedAttributes, null);
    }
    
    @Override
    protected void init(String name, Map<String, Object> inheritedAttributes) {
      set(Attribute.NAME, name).lock();
      setInheritedAttributes(inheritedAttributes);
    }

    @Override
    protected void process() {
      // no-op
    }
  }

}
