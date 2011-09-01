package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * Default AttributeDecomposer.Step implementations
 * @author Doug Barth
 */
abstract class AbstractAttributeDecomposerStep implements AttributeDecomposer.Step {
  public Serializable decompose(final Object object,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    if (object == null) {
      return null;
    }
    
    if (alreadyDecomposed.containsKey(object)) {
      return alreadyDecomposed.get(object);
    }
    else {
      final Serializable mutableContainer = createMutableContainer(object);
      alreadyDecomposed.put(object, mutableContainer);
      decomposeInto(object, mutableContainer, alreadyDecomposed);
      return mutableContainer;
    }
  }
  
  abstract Serializable createMutableContainer(Object o);
  
  abstract void decomposeInto(Object o, Serializable container,
      IdentityHashMap<Object, Serializable> alreadyDecomposed);
}
