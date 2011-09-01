package com.orbitz.monitoring.lib.decomposer;

import com.orbitz.monitoring.api.Decomposer;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;
import com.orbitz.monitoring.lib.decomposer.AttributeDecomposer.Step;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class that takes any Object and turns it into a Serializable implementation that can be a
 * drop-in replacement for the original Object.
 * 
 * @author Doug Barth
 */
public class AttributeDecomposer implements Decomposer {
  public Serializable decompose(final Object object) {
    return new BaseAttributeDecomposer().decompose(object,
        new IdentityHashMap<Object, Serializable>());
  }
  
  interface Step {
    Serializable decompose(Object object, IdentityHashMap<Object, Serializable> alreadyDecomposed);
  }
}

class BaseAttributeDecomposer implements AttributeDecomposer.Step {
  private Map<Class<?>, Step> _classToDecomposer;
  
  public BaseAttributeDecomposer() {
    _classToDecomposer = new HashMap<Class<?>, Step>();
    final IdentityDecomposer identityDecomposer = new IdentityDecomposer();
    _classToDecomposer.put(Character.class, identityDecomposer);
    _classToDecomposer.put(String.class, identityDecomposer);
    _classToDecomposer.put(Byte.class, identityDecomposer);
    _classToDecomposer.put(Short.class, identityDecomposer);
    _classToDecomposer.put(Integer.class, identityDecomposer);
    _classToDecomposer.put(Long.class, identityDecomposer);
    _classToDecomposer.put(Float.class, identityDecomposer);
    _classToDecomposer.put(Double.class, identityDecomposer);
    _classToDecomposer.put(Boolean.class, identityDecomposer);
    _classToDecomposer.put(Date.class, identityDecomposer);
    
    _classToDecomposer.put(StringBuffer.class, new ToStringDecomposer());
    _classToDecomposer.put(Class.class, new ClassDecomposer());
    
    _classToDecomposer.put(Map.class, new MapDecomposer(this));
    _classToDecomposer.put(List.class, new ListDecomposer(this));
    _classToDecomposer.put(Set.class, new SetDecomposer(this));
    _classToDecomposer.put(CompositeAttributeHolder.class, new AttributeHolderDecomposer(this));
    _classToDecomposer.put(AttributeHolder.class, new AttributeHolderDecomposer(this));
    _classToDecomposer.put(Object[].class, new ArrayDecomposer(this));
    _classToDecomposer.put(Object.class, new ReflectiveDecomposer(this));
  }
  
  public Map<Class<?>, Step> getClassDecomposerMap() {
    return new HashMap<Class<?>, Step>(_classToDecomposer);
  }
  
  public Serializable decompose(final Object object,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    if (object == null) {
      return null;
    }
    
    Class<?> klass = object.getClass();
    AttributeDecomposer.Step decomposer = null;
    CLASS_LOOP: while (klass != null) {
      decomposer = _classToDecomposer.get(klass);
      
      if (decomposer != null) {
        break;
      }
      
      if (Object[].class.isAssignableFrom(klass)) {
        decomposer = _classToDecomposer.get(Object[].class);
        if (decomposer != null) {
          break;
        }
      }
      
      final Class<?>[] interfaces = klass.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        final Class<?> anInterface = interfaces[i];
        decomposer = _classToDecomposer.get(anInterface);
        
        if (decomposer != null) {
          break CLASS_LOOP;
        }
      }
      klass = klass.getSuperclass();
    }
    if (decomposer == null) {
      // Not using Preconditions for verification so that this message is only generated when needed
      throw new NullPointerException("Could not find an AttributeDecomposer.Step for "
          + klass.getCanonicalName());
    }
    return decomposer.decompose(object, alreadyDecomposed);
  }
}
