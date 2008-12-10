package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.serializable.SerializableCompositeMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstract base class suitable for extending to obtain common behavior of
 * CompositeMonitors.
 *
 * @author Doug Barth
 */
public abstract class AbstractCompositeMonitor extends AbstractMonitor
        implements CompositeMonitor {

    private static final Set SERIALIZABLE;
    static {
        final Set set = new HashSet();
        set.add(Boolean.class);
        set.add(Byte.class);
        set.add(Character.class);
        set.add(Date.class);
        set.add(Double.class);
        set.add(Float.class);
        set.add(Integer.class);
        set.add(Long.class);
        set.add(Short.class);
        set.add(String.class);
        SERIALIZABLE = Collections.unmodifiableSet(set);
    }

    private List _childMonitors = new LinkedList();

    public AbstractCompositeMonitor(String name) {
        super(name);

        MonitoringEngine.getInstance().compositeMonitorStarted(this);
    }

    public AbstractCompositeMonitor(String name, MonitoringLevel monitoringLevel) {
        super(name, monitoringLevel);

        MonitoringEngine.getInstance().compositeMonitorStarted(this);
    }

    public AbstractCompositeMonitor(String name, Map inheritedAttributes) {
        this(name, MonitoringLevel.INFO, inheritedAttributes);
    }

    public AbstractCompositeMonitor(String name, MonitoringLevel monitoringLevel, Map inheritedAttributes) {
        super();

        this._monitoringLevel = monitoringLevel;

        if(inheritedAttributes != null) {
            for (Iterator i = inheritedAttributes.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                if(value != null && AttributeHolder.class.isAssignableFrom(value.getClass())) {
                    value = ((AttributeHolder) value).getValue();
                }
                AttributeHolder holder = setInheritable(key, value).lock();
                if(value != null && SERIALIZABLE.contains(value.getClass())) {
                    holder.serializable();
                }
            }
        }

        init(name);

        MonitoringEngine.getInstance().compositeMonitorStarted(this);
    }

    public void addChildMonitor(Monitor monitor) {
        _childMonitors.add(monitor);
    }

    public Collection getChildMonitors() {
        return _childMonitors;
    }

    public CompositeAttributeHolder setInheritable(String key, Object value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value)).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, byte value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value).serializable()).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, int value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value).serializable()).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, long value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value).serializable()).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, float value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value).serializable()).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, double value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value).serializable()).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, char value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value).serializable()).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, boolean value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value).serializable()).setInheritable(true);
    }

    public Map getInheritableAttributes() {
        CompositeAttributeMap compositeMap = (CompositeAttributeMap)_attributes;

        return compositeMap.getAllInheritable();
    }

    public Map getInheritableAttributeHolders() {
        CompositeAttributeMap compositeMap = (CompositeAttributeMap)_attributes;

        return compositeMap.getAllInheritableAttributeHolders();
    }

    public SerializableMonitor getSerializableMomento() {
        List childMomentos = new ArrayList(_childMonitors.size());
        Iterator it = _childMonitors.iterator();
        while (it.hasNext()) {
            Monitor monitor = (Monitor) it.next();
            childMomentos.add(monitor.getSerializableMomento());
        }

        MonitoringEngine engine = MonitoringEngine.getInstance();
        Map serializableAttributes = engine.makeAttributeHoldersSerializable(_attributes.getAllAttributeHolders());

        SerializableCompositeMonitor monitor = new SerializableCompositeMonitor(null,childMomentos);
        monitor.setAllAttributeHolders(serializableAttributes);

        return monitor;
    }

    protected void process() {
        MonitoringEngine.getInstance().compositeMonitorCompleted(this);
        super.process();
    }

    protected AttributeMap createAttributeMap() {
        return new CompositeAttributeMap();
    }
}
