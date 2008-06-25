package com.orbitz.monitoring.api.monitor.serializable;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.monitor.CompositeAttributeHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A CompositeMonitor implementation that is specifically created for
 * serialization.
 *
 * @author Doug Barth
 * @see SerializableMonitor
 */
public class SerializableCompositeMonitor extends SerializableMonitor
        implements CompositeMonitor {
    
    /**
     * Hard version used to support evolutionary compatibility during
     * serialization between client and server.  Basically, an older instance
     * of this class can be compatible with a newer one, if the new additions
     * are optional / ancillary to core functionality / backward compatible.
     *
     * NOTE: Changing this value requires coordination with other teams.
     *       !! TREAD LIGHTLY !!
     */
    private static final long serialVersionUID = 3L;

    // ** PRIVATE DATA ********************************************************
    private List _childMomentos;

    // ** CONSTRUCTORS ********************************************************
    public SerializableCompositeMonitor(Map attributes, List childMomentos) {
        super(attributes);
        _childMomentos = childMomentos;
    }

    public void addChildMonitor(Monitor monitor) {
        throw new UnsupportedOperationException(
                "Adding child monitors not allowed");
    }

    public Collection getChildMonitors() {
        return _childMomentos;
    }

    public CompositeAttributeHolder setInheritable(String key, Object value) {
        return (CompositeAttributeHolder)set(key, value);
    }

    public CompositeAttributeHolder setInheritable(String key, byte value) {
        return (CompositeAttributeHolder)set(key, value);
    }

    public CompositeAttributeHolder setInheritable(String key, int value) {
        return (CompositeAttributeHolder)set(key, value);
    }

    public CompositeAttributeHolder setInheritable(String key, long value) {
        return (CompositeAttributeHolder)set(key, value);
    }

    public CompositeAttributeHolder setInheritable(String key, float value) {
        return (CompositeAttributeHolder)set(key, value);
    }

    public CompositeAttributeHolder setInheritable(String key, double value) {
        return (CompositeAttributeHolder)set(key, value);
    }

    public CompositeAttributeHolder setInheritable(String key, char value) {
        return (CompositeAttributeHolder)set(key, value);
    }

    public CompositeAttributeHolder setInheritable(String key, boolean value) {
        return (CompositeAttributeHolder)set(key, value);
    }

    public Map getInheritableAttributes() {
        return new HashMap(0);
    }

    public Map getInheritableAttributeHolders() {
        return new HashMap(0);
    }
}
