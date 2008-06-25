package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * Given a <code>Serializable</code> object, this class calls toString()
 * and returns it.
 *
 * @author Doug Barth
 */
class ToStringDecomposer extends AbstractAttributeDecomposerStep {
    Serializable createMutableContainer(Object o) {
        return (Serializable) o.toString();
    }

    void decomposeInto(Object o, Serializable container,
                       IdentityHashMap alreadyDecomposed) {
        // No-op
    }
}
