package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Transforms {@link Set sets} to an equivalent set implementation that is {@link Serializable} and
 * is guaranteed to be available in all standard VMs.
 * @author Doug Barth
 */
class SetDecomposer extends AbstractAttributeDecomposerStep {
  private final AttributeDecomposer.Step delegate;
  
  /**
   * Creates a new set composer that uses the specified delegate to decompose the elements of the
   * set
   * @param delegate the delegate that will decompose set elements
   */
  public SetDecomposer(final AttributeDecomposer.Step delegate) {
    this.delegate = delegate;
  }
  
  /**
   * @see AbstractAttributeDecomposerStep#createMutableContainer(java.lang.Object)
   */
  @Override
  Serializable createMutableContainer(final Object object) {
    Set<?> set = (Set<?>)object;
    return new HashSet<Object>(set.size());
  }
  
  /**
   * @see AbstractAttributeDecomposerStep#decomposeInto(Object, Serializable, IdentityHashMap)
   */
  @Override
  void decomposeInto(final Object object, final Serializable container,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    Set<?> set = (Set<?>)object;
    @SuppressWarnings("unchecked")
    HashSet<Serializable> returnSet = (HashSet<Serializable>)container;
    for (Object item : set) {
      returnSet.add(this.delegate.decompose(item, alreadyDecomposed));
    }
  }
}
