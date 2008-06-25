package com.orbitz.monitoring.lib.decomposer;

import com.orbitz.monitoring.api.Decomposer;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;

import java.io.Serializable;
import java.util.IdentityHashMap;
/**
 * AttributeHolderDecomposer
 */
public class AttributeHolderDecomposer extends AbstractAttributeDecomposerStep {
    private AttributeDecomposer.Step _delegate;

    private Object updatedValue;

    public AttributeHolderDecomposer(AttributeDecomposer.Step delegate) {
        _delegate = delegate;
    }

    Serializable createMutableContainer(Object object) {
        AttributeHolder holder = (AttributeHolder) object;
        return new AttributeHolder(holder.getValue());
    }

    void decomposeInto(Object o, Serializable container, IdentityHashMap alreadyDecomposed) {
        AttributeHolder holder = (AttributeHolder) o;

        updatedValue = _delegate.decompose(holder.getValue(), alreadyDecomposed);
    }

    public Serializable decompose(final Object object, final IdentityHashMap alreadyDecomposed) {
            if (object == null) {
                return null;
            }

            if (alreadyDecomposed.containsKey(object)) {
                return (Serializable) alreadyDecomposed.get(object);
            } else {
                Serializable mutableContainer = createMutableContainer(object);
                alreadyDecomposed.put(object, mutableContainer);
                decomposeInto(object, mutableContainer, alreadyDecomposed);
                Serializable holder = new AttributeHolder(updatedValue);
                return holder;
            }
        }

}
