package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

public class MockDecomposerStep implements AttributeDecomposer.Step {
  public List<Object> _decomposedObjects = new ArrayList<Object>();
  
  public Serializable decompose(final Object object,
      final IdentityHashMap<Object, Serializable> alreadyDecomposed) {
    _decomposedObjects.add(object);
    return object == null ? null : object.toString();
  }
  
  public List<Object> getDecomposedObjects() {
    return _decomposedObjects;
  }
}
