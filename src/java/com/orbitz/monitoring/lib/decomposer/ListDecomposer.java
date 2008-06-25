package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Given an instance of <code>List</object>, this class transforms it to an
 * equivalent <code>List</code> implementation that is <code>Serializable</code>
 * and is guaranteed to be available in all standard VMs.
 *
 * @author Doug Barth
 */
class ListDecomposer extends AbstractAttributeDecomposerStep {
    private AttributeDecomposer.Step _delegate;

    public ListDecomposer(AttributeDecomposer.Step delegate) {
        _delegate = delegate;
    }

    Serializable createMutableContainer(Object o) {
        List list = (List) o;
        return new ArrayList(list.size());
    }

    void decomposeInto(
            Object object, Serializable container, IdentityHashMap alreadyDecomposed) {
        List list = (List) object;
        ArrayList returnList = (ArrayList) container;

        for (int i = 0; i < list.size(); i++) {
            Object o = _delegate.decompose(list.get(i), alreadyDecomposed);
            returnList.add(o);
        }
    }
}
