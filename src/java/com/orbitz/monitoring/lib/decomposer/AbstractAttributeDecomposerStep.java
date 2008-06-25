package com.orbitz.monitoring.lib.decomposer;

import com.orbitz.monitoring.api.monitor.AttributeHolder;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * The base implementation for all AttributeDecomposer.Step implementations
 *
 *
 * @author Doug Barth
 */
abstract class AbstractAttributeDecomposerStep
        implements AttributeDecomposer.Step {
    public Serializable decompose(Object object, IdentityHashMap alreadyDecomposed) {
        if (object == null) {
            return null;
        }

        if (alreadyDecomposed.containsKey(object)) {
            return (Serializable) alreadyDecomposed.get(object);
        } else {
            Serializable mutableContainer = createMutableContainer(object);
            alreadyDecomposed.put(object, mutableContainer);
            decomposeInto(object, mutableContainer, alreadyDecomposed);
            return mutableContainer;
        }
    }

    abstract Serializable createMutableContainer(Object o);
    abstract void decomposeInto(Object o, Serializable container,
                                IdentityHashMap alreadyDecomposed);
}
