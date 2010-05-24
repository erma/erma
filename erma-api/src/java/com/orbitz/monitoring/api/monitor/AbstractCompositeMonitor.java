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

    // a set of classes that are known to be serializable
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

    /**
     * Create a new composite monitor with the provided
     * name.
     *
     * @param name the name of the monitor
     */
    public AbstractCompositeMonitor(String name) {
        this(name, MonitoringLevel.INFO, null);
    }

    /**
     * Create a new composite monitor with the provided
     * name and monitoring level.
     *
     * @param name the name of the monitor
     * @param monitoringLevel the monitoring level
     */
    public AbstractCompositeMonitor(String name, MonitoringLevel monitoringLevel) {
        this(name, monitoringLevel, null);
    }

    /**
     * Create a new composite monitor with the provided
     * name and inherited attributes.
     *
     * @param name the name of the monitor
     * @param inheritedAttributes the collection of inherited attributes
     */
    public AbstractCompositeMonitor(String name, Map inheritedAttributes) {
        this(name, MonitoringLevel.INFO, inheritedAttributes);
    }

    /**
     * Create a new composite monitor with the provided
     * name, monitoring level and inherited attributes.
     *
     * @param name the name of the monitor
     * @param monitoringLevel the monitoring level
     * @param inheritedAttributes the collection of inherited attributes
     */
    public AbstractCompositeMonitor(String name, MonitoringLevel monitoringLevel, Map inheritedAttributes) {
        super(name, monitoringLevel, inheritedAttributes);

        MonitoringEngine.getInstance().compositeMonitorStarted(this);
    }

    /**
     * Add a monitor as a child.
     *
     * @param monitor the child monitor
     */
    public void addChildMonitor(Monitor monitor) {
        _childMonitors.add(monitor);
    }

    /**
     * Get the collection of child monitors.
     *
     * @return the collection of child monitors
     */
    public Collection getChildMonitors() {
        return _childMonitors;
    }

    public CompositeAttributeHolder setInheritable(String key, Object value) {
        CompositeAttributeHolder holder = ((CompositeAttributeHolder) attributes.set(key, value)).setInheritable(true);
        MonitoringEngine.getInstance().setInheritable(this, key, holder);
        return holder;
    }

    public CompositeAttributeHolder setInheritable(String key, byte value) {
        CompositeAttributeHolder holder = ((CompositeAttributeHolder) attributes.set(key, value)).setInheritable(true);
        MonitoringEngine.getInstance().setInheritable(this, key, holder);
        return holder;
    }

    public CompositeAttributeHolder setInheritable(String key, int value) {
        CompositeAttributeHolder holder = ((CompositeAttributeHolder) attributes.set(key, value)).setInheritable(true);
        MonitoringEngine.getInstance().setInheritable(this, key, holder);
        return holder;
    }

    public CompositeAttributeHolder setInheritable(String key, long value) {
        CompositeAttributeHolder holder = ((CompositeAttributeHolder) attributes.set(key, value)).setInheritable(true);
        MonitoringEngine.getInstance().setInheritable(this, key, holder);
        return holder;
    }

    public CompositeAttributeHolder setInheritable(String key, float value) {
        CompositeAttributeHolder holder = ((CompositeAttributeHolder) attributes.set(key, value)).setInheritable(true);
        MonitoringEngine.getInstance().setInheritable(this, key, holder);
        return holder;
    }

    public CompositeAttributeHolder setInheritable(String key, double value) {
        CompositeAttributeHolder holder = ((CompositeAttributeHolder) attributes.set(key, value)).setInheritable(true);
        MonitoringEngine.getInstance().setInheritable(this, key, holder);
        return holder;
    }

    public CompositeAttributeHolder setInheritable(String key, char value) {
        CompositeAttributeHolder holder = ((CompositeAttributeHolder) attributes.set(key, value)).setInheritable(true);
        MonitoringEngine.getInstance().setInheritable(this, key, holder);
        return holder;
    }

    public CompositeAttributeHolder setInheritable(String key, boolean value) {
        CompositeAttributeHolder holder = ((CompositeAttributeHolder) attributes.set(key, value)).setInheritable(true);
        MonitoringEngine.getInstance().setInheritable(this, key, holder);
        return holder;
    }

    public Map getInheritableAttributes() {
        CompositeAttributeMap compositeMap = (CompositeAttributeMap) attributes;

        return compositeMap.getAllInheritable();
    }

    public Map getInheritableAttributeHolders() {
        CompositeAttributeMap compositeMap = (CompositeAttributeMap) attributes;

        return compositeMap.getAllInheritableAttributeHolders();
    }

    /**
     * Get a serializable version of this monitor. Also creates
     * serialized versions of any child monitors.
     *
     * @return the serializable monitor
     */
    public SerializableMonitor getSerializableMomento() {
        List childMomentos = new ArrayList(_childMonitors.size());
        Iterator it = _childMonitors.iterator();
        while (it.hasNext()) {
            Monitor monitor = (Monitor) it.next();
            childMomentos.add(monitor.getSerializableMomento());
        }

        MonitoringEngine engine = MonitoringEngine.getInstance();
        Map serializableAttributes = engine.makeAttributeHoldersSerializable(attributes.getAllAttributeHolders());

        SerializableCompositeMonitor monitor = new SerializableCompositeMonitor(null,childMomentos);
        monitor.setAllAttributeHolders(serializableAttributes);

        return monitor;
    }

    /**
     * Used to set the inherited attributes on this
     * monitor.
     *
     * @param inheritedAttributes the collection of inherited attributes
     */
    protected void setInheritedAttributes(Map inheritedAttributes) {
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
    }

    /**
     * Process this composite monitor. Delegates to AbstractMonitor.process().
     */
    protected void process() {
        MonitoringEngine.getInstance().compositeMonitorCompleted(this);
        super.process();
    }

    protected AttributeMap createAttributeMap() {
        return new CompositeAttributeMap();
    }
}
