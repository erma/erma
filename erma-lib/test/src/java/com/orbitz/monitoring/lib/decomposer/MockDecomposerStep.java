package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.IdentityHashMap;

/**
 * Description of class goes here.<p>
 * <p/>
 * <p>(c) 2000-04 Orbitz, LLC. All Rights Reserved.
 */

public class MockDecomposerStep implements AttributeDecomposer.Step {
    public List _decomposedObjects = new ArrayList();

    public Serializable decompose(Object object, IdentityHashMap alreadyDecomposed) {
        _decomposedObjects.add(object);
        return object == null ? null : object.toString();
    }

    public List getDecomposedObjects() {
        return _decomposedObjects;
    }
}
