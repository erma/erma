package com.orbitz.monitoring.api.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

/**
 * CompositeAttributeMap provides the additional functionality to have inheritable attributes
 */
public class CompositeAttributeMap extends AttributeMap {
  private static final Logger logger = Logger.getLogger(CompositeAttributeMap.class);
  
  /**
   * Hard version used to support evolutionary compatibility during serialization between client and
   * server. Basically, an older instance of this class can be compatible with a newer one, if the
   * new additions are optional / ancillary to core functionality / backward compatible.
   * 
   * NOTE: Changing this value requires coordination with other teams. !! TREAD LIGHTLY !!
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * Default constructor.
   */
  public CompositeAttributeMap() {
    super();
  }
  
  /**
   * Constructor.
   * 
   * @param attributes initial attributes
   */
  public CompositeAttributeMap(final AttributeMap attributes) {
    super(attributes.getAll());
  }
  
  /**
   * Given a map of key -> CompositeAttributeHolder, clone and add all entries to this attributes
   * map. Overriding b/c we need to put CAH in attributes.
   * 
   * @param attributeHolders map of key -> CompositeAttributeHolder
   */
  @Override
  public void setAllAttributeHolders(final Map attributeHolders) {
    if (attributeHolders == null) {
      return;
    }
    
    for (final Iterator i = attributeHolders.entrySet().iterator(); i.hasNext();) {
      final Map.Entry entry = (Map.Entry)i.next();
      final String key = (String)entry.getKey();
      final Object value = entry.getValue();
      
      if (CompositeAttributeHolder.class.isAssignableFrom(value.getClass())) {
        final CompositeAttributeHolder original = (CompositeAttributeHolder)value;
        final CompositeAttributeHolder copy = (CompositeAttributeHolder)original.clone();
        getAttributes().put(key, copy);
      }
      else if (AttributeHolder.class.isAssignableFrom(value.getClass())) {
        final AttributeHolder original = (AttributeHolder)value;
        final CompositeAttributeHolder copy = new CompositeAttributeHolder(original.getValue());
        if (original.isSerializable()) {
          copy.serializable();
        }
        if (original.isLocked()) {
          copy.lock();
        }
        getAttributes().put(key, copy);
      }
      else {
        set(key, value);
      }
    }
  }
  
  /**
   * Generate a new map of key -> value containing all inheritable attributes
   * @return map of inheritable attributes
   */
  public Map<String, Object> getAllInheritable() {
    final Map<String, Object> allInheritable = new HashMap<String, Object>();
    for (final Entry<String, CompositeAttributeHolder> entry : findCompositeAttributes().entrySet()) {
      final String key = entry.getKey();
      final CompositeAttributeHolder attributeHolder = entry.getValue();
      
      if (attributeHolder.isInheritable()) {
        final Object value = attributeHolder.getValue();
        allInheritable.put(key, value);
      }
    }
    return allInheritable;
  }
  
  /**
   * Generate a new map of key -> CompsositeAttributeHolder containing all inheritable attributes
   * @return map of inheritable attributes
   */
  public Map<String, CompositeAttributeHolder> getAllInheritableAttributeHolders() {
    final Map<String, CompositeAttributeHolder> allInheritable = new HashMap<String, CompositeAttributeHolder>();
    for (final Entry<String, CompositeAttributeHolder> entry : findCompositeAttributes().entrySet()) {
      final String key = entry.getKey();
      final CompositeAttributeHolder attributeHolder = entry.getValue();
      
      if (attributeHolder.isInheritable()) {
        allInheritable.put(key, attributeHolder);
      }
    }
    return allInheritable;
  }
  
  @Override
  protected CompositeAttributeHolder createHolderForValue(final AttributeHolder old,
      final Object value) {
    final CompositeAttributeHolder attributeHolder = new CompositeAttributeHolder(value);
    if (old.isSerializable()) {
      attributeHolder.serializable();
    }
    if (CompositeAttributeHolder.class.isAssignableFrom(old.getClass())) {
      if (((CompositeAttributeHolder)old).isInheritable()) {
        attributeHolder.setInheritable(true);
      }
    }
    return attributeHolder;
  }
  
  @Override
  protected CompositeAttributeHolder createHolderForValue(final Object value) {
    return new CompositeAttributeHolder(value);
  }
}
