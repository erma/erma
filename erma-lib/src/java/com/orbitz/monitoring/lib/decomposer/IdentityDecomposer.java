package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * Given a <code>Serializable</code> object, this class cast the object to
 * <code>Serializable</code> and returns it.
 *
 * @author Doug Barth
 */
class IdentityDecomposer extends AbstractAttributeDecomposerStep {
    Serializable createMutableContainer(Object o) {
        return (Serializable) o;
    }

    void decomposeInto(Object o, Serializable container,
                       IdentityHashMap alreadyDecomposed) {
        // No-op
    }
}
