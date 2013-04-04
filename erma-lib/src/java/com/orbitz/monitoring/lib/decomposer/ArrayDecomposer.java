package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * Converts {@link Object} arrays into {@link Serializable} arrays by reflecting on items in the
 * input array and making best guesses about how to serialize them
 * @author Doug Barth
 */
class ArrayDecomposer extends AbstractAttributeDecomposerStep {
  private final AttributeDecomposer.Step _delegate;
  
  public ArrayDecomposer(final AttributeDecomposer.Step delegate) {
    _delegate = delegate;
  }
  
  @Override
  Serializable createMutableContainer(final Object object) {
    Object[] array = (Object[])object;
    return new Object[array.length];
  }
  
  @Override
  void decomposeInto(final Object o, final Serializable container,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    Object[] array = (Object[])o;
    Object[] returnArray = (Object[])container;
    
    for (int i = 0; i < array.length; i++) {
      returnArray[i] = _delegate.decompose(array[i], alreadyDecomposed);
    }
  }
}
