package com.orbitz.monitoring.lib.decomposer;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * Returns the full qualiified class name for a given Class object
 *
 * @author Operations Architecture
 */
class ClassDecomposer extends AbstractAttributeDecomposerStep {
    Serializable createMutableContainer(Object o) {
        return (Serializable) ((Class)o).getName();
    }

    void decomposeInto(Object o, Serializable container,
                       IdentityHashMap alreadyDecomposed) {
        // No-op
    }
}
