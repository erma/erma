package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * Given a <code>Serializable</code> object, this class calls toString() and returns it.
 * @author Doug Barth
 */
class ToStringDecomposer extends AbstractAttributeDecomposerStep {
  @Override
  Serializable createMutableContainer(final Object o) {
    return o.toString();
  }
  
  @Override
  void decomposeInto(final Object o, final Serializable container,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    // No-op
  }
}
