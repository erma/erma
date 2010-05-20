package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * Given an instance of <code>Object[]</object>, this class transforms it to an
 * equivalent <code>Object[]</code> that contains <code>Serializable</code>
 * elements and is guaranteed to be available in all standard VMs.
 *
 * @author Doug Barth
 */
class ArrayDecomposer extends AbstractAttributeDecomposerStep {
    private AttributeDecomposer.Step _delegate;

    public ArrayDecomposer(AttributeDecomposer.Step delegate) {
        _delegate = delegate;
    }

    Serializable createMutableContainer(Object object) {
        Object[] array = (Object[]) object;
        return new Object[array.length];
    }

    void decomposeInto(Object o, Serializable container,
                       IdentityHashMap alreadyDecomposed) {
        Object[] array = (Object[]) o;
        Object[] returnArray = (Object[]) container;

        for (int i = 0; i < array.length; i++) {
            returnArray[i] = _delegate.decompose(array[i], alreadyDecomposed);
        }
    }
}
