package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * Returns the fully qualified class name for a given Class object
 * @author Operations Architecture
 */
class ClassDecomposer extends AbstractAttributeDecomposerStep {
  @Override
  Serializable createMutableContainer(final Object o) {
    return ((Class<?>)o).getName();
  }
  
  @Override
  void decomposeInto(final Object o, final Serializable container,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    // No-op
  }
}
