package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.CompositeMonitor;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.serializable.SerializableCompositeMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An abstract base class suitable for extending to obtain common behavior of
 * CompositeMonitors.
 *
 * @author Doug Barth
 */
public abstract class AbstractCompositeMonitor extends AbstractMonitor
        implements CompositeMonitor {
    private List _childMonitors = new LinkedList();

    public AbstractCompositeMonitor(String name) {
        super(name);

        MonitoringEngine.getInstance().compositeMonitorStarted(this);
    }

    public AbstractCompositeMonitor(String name, Map inheritedAttributes) {
        super(name, inheritedAttributes);

        MonitoringEngine.getInstance().compositeMonitorStarted(this);
    }

    public AbstractCompositeMonitor(String name, MonitoringLevel monitoringLevel) {
        super(name, monitoringLevel);

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
        return ((CompositeAttributeHolder)_attributes.set(key, value)).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, int value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value)).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, long value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value)).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, float value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value)).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, double value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value)).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, char value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value)).setInheritable(true);
    }

    public CompositeAttributeHolder setInheritable(String key, boolean value) {
        return ((CompositeAttributeHolder)_attributes.set(key, value)).setInheritable(true);
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
        List childMomentos = new LinkedList();
        for (int i = 0; i < _childMonitors.size(); i++) {
            Monitor monitor = (Monitor) _childMonitors.get(i);
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
