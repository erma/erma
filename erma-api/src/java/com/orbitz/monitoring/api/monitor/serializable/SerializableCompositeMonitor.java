package com.orbitz.monitoring.api.monitor.serializable;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A CompositeMonitor implementation that is specifically created for serialization.
 * 
 * @author Doug Barth
 * @see SerializableMonitor
 */
public class SerializableCompositeMonitor extends SerializableMonitor implements CompositeMonitor {
  
  /**
   * Hard version used to support evolutionary compatibility during serialization between client and
   * server. Basically, an older instance of this class can be compatible with a newer one, if the
   * new additions are optional / ancillary to core functionality / backward compatible.
   * 
   * NOTE: Changing this value requires coordination with other teams. !! TREAD LIGHTLY !!
   */
  private static final long serialVersionUID = 3L;
  
  private final List<SerializableMonitor> _childMomentos;
  
  public SerializableCompositeMonitor(final Map attributes, final List childMomentos) {
    super(attributes);
    _childMomentos = childMomentos;
  }
  
  public void addChildMonitor(final Monitor monitor) {
    throw new UnsupportedOperationException("Adding child monitors not allowed");
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Collection<Monitor> getChildMonitors() {
    return (List)_childMomentos;
  }
  
  /**
   * Gets child monitors as {@link SerializableMonitor serializable monitors}
   * @return the child monitors
   */
  public Collection<SerializableMonitor> getSerializableChildMonitors() {
    return _childMomentos;
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final Object value) {
    return (CompositeAttributeHolder)set(key, value);
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final byte value) {
    return (CompositeAttributeHolder)set(key, value);
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final int value) {
    return (CompositeAttributeHolder)set(key, value);
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final long value) {
    return (CompositeAttributeHolder)set(key, value);
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final float value) {
    return (CompositeAttributeHolder)set(key, value);
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final double value) {
    return (CompositeAttributeHolder)set(key, value);
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final char value) {
    return (CompositeAttributeHolder)set(key, value);
  }
  
  public CompositeAttributeHolder setInheritable(final String key, final boolean value) {
    return (CompositeAttributeHolder)set(key, value);
  }
  
  public Map getInheritableAttributes() {
    return new HashMap(0);
  }
  
  public Map getInheritableAttributeHolders() {
    return new HashMap(0);
  }
}
