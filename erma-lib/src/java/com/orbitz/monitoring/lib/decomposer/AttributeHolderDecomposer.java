package com.orbitz.monitoring.lib.decomposer;

import com.orbitz.monitoring.api.monitor.AttributeHolder;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * Converts an {@link AttributeHolder} into something serializable, also decomposing the
 * {@link AttributeHolder#getValue() value} of the holder.
 */
public class AttributeHolderDecomposer extends AbstractAttributeDecomposerStep {
  private final AttributeDecomposer.Step _delegate;
  
  private Object updatedValue;
  
  /**
   * Creates an attribute holder decomposer that uses the specified delegate to decompose the
   * holder's value
   * @param delegate the delegate
   */
  public AttributeHolderDecomposer(final AttributeDecomposer.Step delegate) {
    _delegate = delegate;
  }
  
  @Override
  Serializable createMutableContainer(final Object object) {
    AttributeHolder holder = (AttributeHolder)object;
    return new AttributeHolder(holder.getValue());
  }
  
  @Override
  void decomposeInto(final Object o, final Serializable container,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    AttributeHolder holder = (AttributeHolder)o;
    
    updatedValue = _delegate.decompose(holder.getValue(), alreadyDecomposed);
  }
  
  @Override
  public Serializable decompose(final Object object,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    if (object == null) {
      return null;
    }
    
    if (alreadyDecomposed.containsKey(object)) {
      return alreadyDecomposed.get(object);
    } else {
      Serializable mutableContainer = createMutableContainer(object);
      alreadyDecomposed.put(object, mutableContainer);
      decomposeInto(object, mutableContainer, alreadyDecomposed);
      Serializable holder = new AttributeHolder(updatedValue);
      return holder;
    }
  }
  
}
