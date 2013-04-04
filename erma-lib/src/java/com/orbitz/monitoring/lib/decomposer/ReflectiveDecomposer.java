package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.beanutils.WrapDynaClass;

/**
 * Given an object, this class reflects over its JavaBean attributes and creates a Serializable
 * representation containing serializable versions of the JavaBean attributes.
 * 
 * @author Doug Barth
 */
class ReflectiveDecomposer extends AbstractAttributeDecomposerStep {
  private final AttributeDecomposer.Step _delegate;
  
  public ReflectiveDecomposer(final AttributeDecomposer.Step delegate) {
    _delegate = delegate;
  }
  
  @Override
  Serializable createMutableContainer(final Object o) {
    return new LazyDynaBean();
  }
  
  /**
   * Will decompose only if the Object is Serializable
   * @param o object to decompose
   * @param container Serializable container
   * @param alreadyDecomposed hashmap of decomposed objects
   */
  @Override
  void decomposeInto(final Object o, final Serializable container,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    LazyDynaBean decomposed = (LazyDynaBean)container;
    WrapDynaBean bean = new WrapDynaBean(o);
    WrapDynaClass dynaClass = (WrapDynaClass)bean.getDynaClass();
    for (DynaProperty property : dynaClass.getDynaProperties()) {
      String name = property.getName();
      Method readMethod = dynaClass.getPropertyDescriptor(name).getReadMethod();
      if (MethodUtils.getAccessibleMethod(readMethod) != null) {
        Object beanProperty;
        try {
          beanProperty = bean.get(name);
        }
        catch (RuntimeException e) {
          throw new RuntimeException("Unable to decompose " + o.getClass().getName() + "." + name,
              e);
        }
        Serializable decomposedProperty = _delegate.decompose(beanProperty, alreadyDecomposed);
        decomposed.set(name, decomposedProperty);
      }
    }
  }
}
