package com.orbitz.monitoring.api.monitor.serializable;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.monitor.AttributeHolder;
import com.orbitz.monitoring.api.monitor.AttributeMap;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A monitor implementation that is specifically created for serializing to
 * other systems. Using a special implementation will allow developers to
 * create their own Monitor implementations without needing to ensure that their
 * implementation class is available on a remote JVM.
 *
 * @author Doug Barth
 */
public class SerializableMonitor implements Monitor, Serializable {
    // ** STATIC/FINAL DATA ***************************************************
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
    private AttributeMap _attributes;
    private MonitoringLevel _monitoringLevel;

    // ** CONSTRUCTORS ********************************************************
    public SerializableMonitor(Map attributes) {
        _attributes = new AttributeMap(attributes);
        _monitoringLevel = MonitoringLevel.INFO;
    }

    public SerializableMonitor(Map attributes, MonitoringLevel monitoringLevel) {
        _attributes = new AttributeMap(attributes);
        _monitoringLevel = monitoringLevel;
    }

    // ** PUBLIC METHODS ******************************************************
    public AttributeHolder set(String key, short value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, int value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, long value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, float value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, double value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, char value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, byte value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, boolean value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, String value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, Date value) {
        return _attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, Object value) {
        return _attributes.set(key, value);
    }

    public void unset(String key) {
        _attributes.unset(key);
    }

    public Map getAllSerializable() {
        return _attributes.getAllSerializable();
    }

    public void setAllAttributeHolders(final Map attributeHolders) {
        _attributes.setAllAttributeHolders(attributeHolders);
    }

    public void setAll(Map attributes) {
        _attributes.setAll(attributes);
    }

    public Object get(String key) {
        return _attributes.get(key);
    }

    public String getAsString(String key) {
        return _attributes.getAsString(key);
    }

    public Map getAsMap(String key) {
        return _attributes.getAsMap(key);
    }

    public List getAsList(String key) {
        return _attributes.getAsList(key);
    }

    public Set getAsSet(String key) {
        return _attributes.getAsSet(key);
    }

    public short getAsShort(String key) {
        return _attributes.getAsShort(key);
    }

    public int getAsInt(String key) {
        return _attributes.getAsInt(key);
    }

    public long getAsLong(String key) {
        return _attributes.getAsLong(key);
    }

    public float getAsFloat(String key) {
        return _attributes.getAsFloat(key);
    }

    public double getAsDouble(String key) {
        return _attributes.getAsDouble(key);
    }

    public char getAsChar(String key) {
        return _attributes.getAsChar(key);
    }

    public byte getAsByte(String key) {
        return _attributes.getAsByte(key);
    }

    public boolean getAsBoolean(String key) {
        return _attributes.getAsBoolean(key);
    }

    public Map getAll() {
        return _attributes.getAll();
    }

    public boolean getAsBoolean(String key, boolean defaultValue) {
        return _attributes.getAsBoolean(key, defaultValue);
    }

    public short getAsShort(String key, short defaultValue) {
        return _attributes.getAsShort(key, defaultValue);
    }

    public byte getAsByte(String key, byte defaultValue) {
        return _attributes.getAsByte(key, defaultValue);
    }

    public int getAsInt(String key, int defaultValue) {
        return _attributes.getAsInt(key, defaultValue);
    }

    public long getAsLong(String key, long defaultValue) {
        return _attributes.getAsLong(key, defaultValue);
    }

    public float getAsFloat(String key, float defaultValue) {
        return _attributes.getAsFloat(key, defaultValue);
    }

    public double getAsDouble(String key, double defaultValue) {
        return _attributes.getAsDouble(key, defaultValue);
    }

    public char getAsChar(String key, char defaultValue) {
        return _attributes.getAsChar(key, defaultValue);
    }

    public boolean hasAttribute(String key) {
        return _attributes.hasAttribute(key);
    }

    public SerializableMonitor getSerializableMomento() {
        return this;
    }

    public MonitoringLevel getLevel() {
        MonitoringLevel overrideLevel = MonitoringEngine.getInstance().getOverrideLevelForMonitor(this);
        return (overrideLevel != null ? overrideLevel : _monitoringLevel);
    }
}
