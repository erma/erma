package com.orbitz.monitoring.api.monitor;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * CompositeAttributeMap provides the additional functionality to have inheritable attributes
 */
public class CompositeAttributeMap extends AttributeMap {
  private static final long serialVersionUID = 1L;
  
  /**
   * Default constructor.
   */
  public CompositeAttributeMap() {
    super();
  }
  
  /**
   * Constructor.
   * @param attributes initial attributes
   */
  public CompositeAttributeMap(final AttributeMap attributes) {
    super(attributes.getAll());
  }
  
  /**
   * Given a map of key -> CompositeAttributeHolder, clone and add all entries to this attributes
   * map. Overriding b/c we need to put CAH in attributes.
   * @param attributeHolders map of key -> CompositeAttributeHolder
   */
  @Override
  public void setAllAttributeHolders(final Map<String, ?> attributeHolders) {
    if (attributeHolders == null) {
      return;
    }
    for (Entry<String, ?> entry : attributeHolders.entrySet()) {
      final String key = entry.getKey();
      final Object value = entry.getValue();
      if (CompositeAttributeHolder.class.isAssignableFrom(value.getClass())) {
        final CompositeAttributeHolder original = (CompositeAttributeHolder)value;
        final CompositeAttributeHolder copy = (CompositeAttributeHolder)original.clone();
        getAttributes().put(key, copy);
      } else if (AttributeHolder.class.isAssignableFrom(value.getClass())) {
        final AttributeHolder original = (AttributeHolder)value;
        final CompositeAttributeHolder copy = new CompositeAttributeHolder(original.getValue());
        if (original.isSerializable()) {
          copy.serializable();
        }
        if (original.isLocked()) {
          copy.lock();
        }
        getAttributes().put(key, copy);
      } else {
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
      final CompositeAttributeHolder attribute = entry.getValue();
      if (attribute.isInheritable()) {
        allInheritable.put(entry.getKey(), attribute.getValue());
      }
    }
    return allInheritable;
  }
  
  /**
   * Gets an immutable view of the attribute map with the inheritable attribute holders
   * @return map of inheritable attributes
   */
  public Map<String, CompositeAttributeHolder> getAllInheritableAttributeHolders() {
    return Maps.filterValues(findCompositeAttributes(), new Predicate<CompositeAttributeHolder>() {
      public boolean apply(final CompositeAttributeHolder holder) {
        return holder.isInheritable();
      }
    });
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
