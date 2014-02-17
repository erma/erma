package com.orbitz.monitoring.api;

import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;

import java.util.Collection;
import java.util.Map;

/**
 * A monitor that holds related submonitors. This can be useful for gathering multiple monitors and
 * correlating them with some parent monitor.
 * <p>
 * 
 * Children of CompositeMonitors will implicitly inherit their parent's inheritable attributes.
 * CompositeMonitors can designate attributes as inheritable by using the
 * <code>setInheritable</code> methods. Attributes are <strong>not</strong> inheritable when using
 * the <code>set</code> methods.
 * 
 * @author Doug Barth
 */
public interface CompositeMonitor extends Monitor {
  /**
   * Adds a child monitor to this composite monitor. The interface makes no prescription as to
   * whether the children are ordered.
   * 
   * @param monitor the Monitor to be added as a child of this Monitor
   */
  public void addChildMonitor(Monitor monitor);
  
  /**
   * Gets the child monitors of this monitors.
   * 
   * @return a collection of child monitors that were captured. This returns an empty collection if
   *         none were set.
   */
  public Collection<Monitor> getChildMonitors();
  
  /**
   * Adds an inheritable Object attribute to this monitor.
   * 
   * @param key the name of this attribute
   * @param value the Object value of this attribute
   * @return a CompositeAttributeHolder containing the given value and its metadata
   */
  public CompositeAttributeHolder setInheritable(String key, Object value);
  
  /**
   * Adds an inheritable byte attribute to this monitor.
   * 
   * @param key the name of this attribute
   * @param value the byte value of this attribute
   * @return a CompositeAttributeHolder containing the given value and its metadata
   */
  public CompositeAttributeHolder setInheritable(String key, byte value);
  
  /**
   * Adds an inheritable int attribute to this monitor.
   * 
   * @param key the name of this attribute
   * @param value the int value of this attribute
   * @return a CompositeAttributeHolder containing the given value and its metadata
   */
  public CompositeAttributeHolder setInheritable(String key, int value);
  
  /**
   * Adds an inheritable long attribute to this monitor.
   * 
   * @param key the name of this attribute
   * @param value the long value of this attribute
   * @return a CompositeAttributeHolder containing the given value and its metadata
   */
  public CompositeAttributeHolder setInheritable(String key, long value);
  
  /**
   * Adds an inheritable float attribute to this monitor.
   * 
   * @param key the name of this attribute
   * @param value the float value of this attribute
   * @return a CompositeAttributeHolder containing the given value and its metadata
   */
  public CompositeAttributeHolder setInheritable(String key, float value);
  
  /**
   * Adds an inheritable double attribute to this monitor.
   * 
   * @param key the name of this attribute
   * @param value the double value of this attribute
   * @return a CompositeAttributeHolder containing the given value and its metadata
   */
  public CompositeAttributeHolder setInheritable(String key, double value);
  
  /**
   * Adds an inheritable char attribute to this monitor.
   * 
   * @param key the name of this attribute
   * @param value the char value of this attribute
   * @return a CompositeAttributeHolder containing the given value and its metadata
   */
  public CompositeAttributeHolder setInheritable(String key, char value);
  
  /**
   * Adds an inheritable boolean attribute to this monitor.
   * 
   * @param key the name of this attribute
   * @param value the boolean value of this attribute
   * @return a CompositeAttributeHolder containing the given value and its metadata
   */
  public CompositeAttributeHolder setInheritable(String key, boolean value);
  
  /**
   * Gets those attributes that can be inherited by the child monitors. These attributes will be
   * added to the child monitor when it is created.
   * 
   * @return a reference to the map of the inheritable attributes that should be added to the child
   *         monitors
   */
  public Map<String, Object> getInheritableAttributes();
  
  /**
   * Gets all {@link com.orbitz.monitoring.api.monitor.AttributeHolder attribute holders} for which
   * {@link CompositeAttributeHolder#isInheritable()} is true
   * @return the inheritable holders
   */
  public Map<String, CompositeAttributeHolder> getInheritableAttributeHolders();
}
