package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Given an instance of <code>Map</object>, this class transforms it to an
 * equivalent <code>Map</code> implementation that is <code>Serializable</code>
 * and is guaranteed to be available in all standard VMs.
 *
 * @author Doug Barth
 */
class MapDecomposer extends AbstractAttributeDecomposerStep {
    private AttributeDecomposer.Step _delegate;

    public MapDecomposer(AttributeDecomposer.Step delegate) {
        _delegate = delegate;
    }

    Serializable createMutableContainer(Object object) {
        Map map = (Map) object;
        return new HashMap(map.size());
    }

    void decomposeInto(Object o, Serializable container, IdentityHashMap alreadyDecomposed) {
        Map map = (Map) o;
        Map returnMap = (Map) container;

        for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            Object key = _delegate.decompose(entry.getKey(), alreadyDecomposed);
            Object value = _delegate.decompose(entry.getValue(),
                                               alreadyDecomposed);

            returnMap.put(key, value);
        }
    }
}
