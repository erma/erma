package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.serializable.SerializableCompositeMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * An abstract base class suitable for extending to obtain common behavior of CompositeMonitors.
 * @author Doug Barth
 */
public abstract class AbstractCompositeMonitor extends AbstractMonitor implements CompositeMonitor {
  
  private static final Set<Class<? extends Serializable>> SERIALIZABLE;
  static {
    final Set<Class<? extends Serializable>> set = new HashSet<Class<? extends Serializable>>();
    set.add(Boolean.class);
    set.add(Byte.class);
    set.add(Character.class);
    set.add(Date.class);
    set.add(Double.class);
    set.add(Float.class);
    set.add(Integer.class);
    set.add(Long.class);
    set.add(Short.class);
    set.add(String.class);
    SERIALIZABLE = Collections.unmodifiableSet(set);
  }
  
  private List<Monitor> _childMonitors = new LinkedList<Monitor>();
  
  /**
   * Create a new composite monitor with the provided name.
   * 
   * @param name the name of the monitor
   */
  public AbstractCompositeMonitor(final String name) {
    this(name, MonitoringLevel.INFO, null);
  }
  
  /**
   * Create a new composite monitor with the provided name and monitoring level.
   * 
   * @param name the name of the monitor
   * @param monitoringLevel the monitoring level
   */
  public AbstractCompositeMonitor(final String name, final MonitoringLevel monitoringLevel) {
    this(name, monitoringLevel, null);
  }
  
  /**
   * Create a new composite monitor with the provided name and inherited attributes.
   * 
   * @param name the name of the monitor
   * @param inheritedAttributes the collection of inherited attributes
   */
  public AbstractCompositeMonitor(final String name, final Map<String, Object> inheritedAttributes) {
    this(name, MonitoringLevel.INFO, inheritedAttributes);
  }
  
  /**
   * Create a new composite monitor with the provided name, monitoring level and inherited
   * attributes.
   * 
   * @param name the name of the monitor
   * @param monitoringLevel the monitoring level
   * @param inheritedAttributes the collection of inherited attributes
   */
  public AbstractCompositeMonitor(final String name, final MonitoringLevel monitoringLevel,
      final Map<String, Object> inheritedAttributes) {
    super(name, monitoringLevel, inheritedAttributes);
    
    MonitoringEngine.getInstance().compositeMonitorStarted(this);
  }
  
  /**
   * Add a monitor as a child.
   * 
   * @param monitor the child monitor
   */
  public void addChildMonitor(final Monitor monitor) {
    _childMonitors.add(monitor);
  }
  
  /**
   * Get the child monitors.
   * @return the child monitors
   */
  public Collection<Monitor> getChildMonitors() {
    return _childMonitors;
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final Object value) {
    final CompositeAttributeHolder holder = ((CompositeAttributeHolder)attributes.set(key, value))
        .setInheritable(true);
    MonitoringEngine.getInstance().setInheritable(this, key, holder);
    return holder;
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final byte value) {
    final CompositeAttributeHolder holder = ((CompositeAttributeHolder)attributes.set(key, value))
        .setInheritable(true);
    MonitoringEngine.getInstance().setInheritable(this, key, holder);
    return holder;
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final int value) {
    final CompositeAttributeHolder holder = ((CompositeAttributeHolder)attributes.set(key, value))
        .setInheritable(true);
    MonitoringEngine.getInstance().setInheritable(this, key, holder);
    return holder;
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final long value) {
    final CompositeAttributeHolder holder = ((CompositeAttributeHolder)attributes.set(key, value))
        .setInheritable(true);
    MonitoringEngine.getInstance().setInheritable(this, key, holder);
    return holder;
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final float value) {
    final CompositeAttributeHolder holder = ((CompositeAttributeHolder)attributes.set(key, value))
        .setInheritable(true);
    MonitoringEngine.getInstance().setInheritable(this, key, holder);
    return holder;
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final double value) {
    final CompositeAttributeHolder holder = ((CompositeAttributeHolder)attributes.set(key, value))
        .setInheritable(true);
    MonitoringEngine.getInstance().setInheritable(this, key, holder);
    return holder;
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final char value) {
    final CompositeAttributeHolder holder = ((CompositeAttributeHolder)attributes.set(key, value))
        .setInheritable(true);
    MonitoringEngine.getInstance().setInheritable(this, key, holder);
    return holder;
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final boolean value) {
    final CompositeAttributeHolder holder = ((CompositeAttributeHolder)attributes.set(key, value))
        .setInheritable(true);
    MonitoringEngine.getInstance().setInheritable(this, key, holder);
    return holder;
  }
  
  public Map getInheritableAttributes() {
    final CompositeAttributeMap compositeMap = (CompositeAttributeMap)attributes;
    
    return compositeMap.getAllInheritable();
  }
  
  public Map getInheritableAttributeHolders() {
    final CompositeAttributeMap compositeMap = (CompositeAttributeMap)attributes;
    
    return compositeMap.getAllInheritableAttributeHolders();
  }
  
  /**
   * Get a serializable version of this monitor. Also creates serialized versions of any child
   * monitors.
   * @return the serializable monitor
   */
  @Override
  public SerializableMonitor getSerializableMomento() {
    final List<SerializableMonitor> childMomentos = new ArrayList<SerializableMonitor>(
        _childMonitors.size());
    for (final Monitor monitor : _childMonitors) {
      childMomentos.add(monitor.getSerializableMomento());
    }
    final MonitoringEngine engine = MonitoringEngine.getInstance();
    final Map<String, Serializable> serializableAttributes = engine
        .makeAttributeHoldersSerializable(attributes.getAllAttributeHolders());
    
    final SerializableCompositeMonitor monitor = new SerializableCompositeMonitor(null,
        childMomentos);
    monitor.setAllAttributeHolders(serializableAttributes);
    
    return monitor;
  }
  
  /**
   * Used to set the inherited attributes on this monitor.
   * 
   * @param inheritedAttributes the collection of inherited attributes
   */
  @Override
  protected void setInheritedAttributes(final Map<String, Object> inheritedAttributes) {
    if (inheritedAttributes != null) {
      for (final Entry<String, Object> entry : inheritedAttributes.entrySet()) {
        final String key = entry.getKey();
        Object value = entry.getValue();
        if (value != null && AttributeHolder.class.isAssignableFrom(value.getClass())) {
          value = ((AttributeHolder)value).getValue();
        }
        final AttributeHolder holder = setInheritable(key, value).lock();
        if (value != null && SERIALIZABLE.contains(value.getClass())) {
          holder.serializable();
        }
      }
    }
  }
  
  /**
   * Process this composite monitor. Delegates to AbstractMonitor.process().
   */
  @Override
  protected void process() {
    MonitoringEngine.getInstance().compositeMonitorCompleted(this);
    super.process();
  }
  
  @Override
  protected AttributeMap createAttributeMap() {
    return new CompositeAttributeMap();
  }
}
