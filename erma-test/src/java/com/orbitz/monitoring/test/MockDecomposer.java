package com.orbitz.monitoring.test;

import com.orbitz.monitoring.api.Decomposer;
import com.orbitz.monitoring.api.monitor.AttributeHolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A mock implementation of the Decomposer interface that can be used for
 * testing.
 *
 *
 * @author Doug Barth
 */
public class MockDecomposer implements Decomposer {
    private List _decomposedObjects = new ArrayList();

    public Serializable decompose(Object object) {

        _decomposedObjects.add(((AttributeHolder)object).getValue());
        return (Serializable)object;
    }

    public List getDecomposedObjects() {
        return _decomposedObjects;
    }
}
