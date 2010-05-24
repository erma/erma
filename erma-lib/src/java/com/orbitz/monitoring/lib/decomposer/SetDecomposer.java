package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Given an instance of <code>Set</object>, this class transforms it to an
 * equivalent <code>Set</code> implementation that is <code>Serializable</code>
 * and is guaranteed to be available in all standard VMs.
 *
 * @author Doug Barth
 */
class SetDecomposer extends AbstractAttributeDecomposerStep {
    private AttributeDecomposer.Step _delegate;

    public SetDecomposer(AttributeDecomposer.Step delegate) {
        _delegate = delegate;
    }

    Serializable createMutableContainer(Object object) {
        Set set = (Set) object;
        return new HashSet(set.size());
    }

    void decomposeInto(
            Object object, Serializable container, IdentityHashMap alreadyDecomposed) {
        Set set = (Set) object;
        HashSet returnSet = (HashSet) container;

        for (Iterator i = set.iterator(); i.hasNext();) {
            Object o = _delegate.decompose(i.next(), alreadyDecomposed);
            returnSet.add(o);
        }
    }
}
