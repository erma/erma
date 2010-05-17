package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import org.apache.commons.lang.CharSetUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A base implementation of {@link Monitor} that contains all common aspects of
 * monitors.
 *
 * @author Doug Barth
 */
public abstract class AbstractMonitor implements Monitor {

    private static final Logger log = Logger.getLogger(AbstractMonitor.class);

    protected AttributeMap attributes;
    private boolean processed;
    protected MonitoringLevel monitoringLevel = MonitoringLevel.INFO;

    private static final String invalidCharacters = " \\[\\]*,|()$@|~?&<>\\^";
    private static final Set invalidCharSet =  buildInvalidCharSet();

    // ** CONSTRUCTORS ********************************************************

    /**
     * Initializes the attribute map and global attributes. Subclasses
     * will need to call init(String) themselves.
     */
    protected AbstractMonitor() {
        attributes = createAttributeMap();
        processed = false;
    }

    /**
     * Initializes the attribute map, global attributes and calls
     * init(String).
     * 
     * @param name the name of the monitor
     */
    public AbstractMonitor(String name) {
        this(name, MonitoringLevel.INFO, null);
    }

    /**
     * Initializes the attribute map, global attributes, monitoring
     * level and calls init(String).
     *
     * @param name the name of the monitor
     * @param monitoringLevel the monitoring level
     */
    public AbstractMonitor(String name, MonitoringLevel monitoringLevel) {
        this(name, monitoringLevel, null);
    }

    /**
     * Initializes the attribute map, global attributes, sets the
     * provided inherited attributes and calls init(String).
     *
     * @param name the name of the monitor
     * @param inheritedAttributes the collection of inherited attributes
     */
    public AbstractMonitor(String name, Map inheritedAttributes) {
        this(name, MonitoringLevel.INFO, inheritedAttributes);
    }

    /**
     * Initializes the attribute map, global attributes, monitoring
     * level, sets the provided inherited attributes and calls
     * init(String).
     *
     * @param name the name of the monitor
     * @param monitoringLevel the monitoring level
     * @param inheritedAttributes the collection of inherited attributes
     */
    public AbstractMonitor(String name, MonitoringLevel monitoringLevel, Map inheritedAttributes) {
        this();
        this.monitoringLevel = monitoringLevel;
        init(name, inheritedAttributes);
    }

    // ** PUBLIC METHODS ******************************************************
    public AttributeHolder set(String key, short value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, int value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, long value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, float value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, double value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, char value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, byte value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, boolean value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, Date value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, String value) {
        return attributes.set(key, value).serializable();
    }

    public AttributeHolder set(String key, Object value) {
        return attributes.set(key, value);
    }

    public void setAll(Map attributes) {
        this.attributes.setAll(attributes);
    }

    public void setAllAttributeHolders(final Map attributeHolders) {
        attributes.setAllAttributeHolders(attributeHolders);
    }

    public void unset(String key) {
        attributes.unset(key);
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public Map getAsMap(String key) {
        return attributes.getAsMap(key);
    }

    public List getAsList(String key) {
        return attributes.getAsList(key);
    }

    public Set getAsSet(String key) {
        return attributes.getAsSet(key);
    }

    public String getAsString(String key) {
        return attributes.getAsString(key);
    }

    public short getAsShort(String key) {
        return attributes.getAsShort(key);
    }

    public int getAsInt(String key) {
        return attributes.getAsInt(key);
    }

    public long getAsLong(String key) {
        return attributes.getAsLong(key);
    }

    public float getAsFloat(String key) {
        return attributes.getAsFloat(key);
    }

    public double getAsDouble(String key) {
        return attributes.getAsDouble(key);
    }

    public char getAsChar(String key) {
        return attributes.getAsChar(key);
    }

    public byte getAsByte(String key) {
        return attributes.getAsByte(key);
    }

    public boolean getAsBoolean(String key) {
        return attributes.getAsBoolean(key);
    }

    public Map getAll() {
        return attributes.getAll();
    }

    public Map getAllSerializable() {
        return attributes.getAllSerializable();
    }

    public boolean getAsBoolean(String key, boolean defaultValue) {
        return attributes.getAsBoolean(key, defaultValue);
    }

    public short getAsShort(String key, short defaultValue) {
        return attributes.getAsShort(key, defaultValue);
    }

    public byte getAsByte(String key, byte defaultValue) {
        return attributes.getAsByte(key, defaultValue);
    }

    public int getAsInt(String key, int defaultValue) {
        return attributes.getAsInt(key, defaultValue);
    }

    public long getAsLong(String key, long defaultValue) {
        return attributes.getAsLong(key, defaultValue);
    }

    public float getAsFloat(String key, float defaultValue) {
        return attributes.getAsFloat(key, defaultValue);
    }

    public double getAsDouble(String key, double defaultValue) {
        return attributes.getAsDouble(key, defaultValue);
    }

    public char getAsChar(String key, char defaultValue) {
        return attributes.getAsChar(key, defaultValue);
    }

    public final MonitoringLevel getLevel() {
        MonitoringLevel overrideLevel = MonitoringEngine.getInstance().getOverrideLevelForMonitor(this);
        return (overrideLevel != null ? overrideLevel : monitoringLevel);
    }

    public boolean hasAttribute(String key) {
        return attributes.hasAttribute(key);
    }

    /**
     * Get a serializable version of this monitor.
     *
     * @return the serializable monitor
     */
    public SerializableMonitor getSerializableMomento() {
        MonitoringEngine engine = MonitoringEngine.getInstance();
        Map serializableAttributes = engine.makeAttributeHoldersSerializable(attributes.getAllAttributeHolders());

        SerializableMonitor monitor = new SerializableMonitor(null);
        monitor.setAllAttributeHolders(serializableAttributes);

        return monitor;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("[").append(getClass()).append(" attributes=");
        buf.append(attributes);
        buf.append(" level=").append(monitoringLevel).append("]");

        return buf.toString();
    }

    // ** PROTECTED METHODS ***************************************************
    /**
     * Used to invoke the monitor lifecycle methods
     *   MonitoringEngine.initMonitor and
     *   MonitoringEngine.monitorCreated
     * on this monitor.
     *
     * @param name the name of the monitor
     * @param inheritedAttributes the collection of inherited attributes
     */
    protected void init(String name, Map inheritedAttributes) {
        MonitoringEngine.getInstance().initMonitor(this);
        if(name != null){
            for (int i = 0; i < name.length(); i++) {
                if(invalidCharSet.contains(new Character(name.charAt(i)))){
                    name = CharSetUtils.delete(name,invalidCharacters);
                    break;
                }
            }
        }
        set(Attribute.NAME, name);

        setInheritedAttributes(inheritedAttributes);

        MonitoringEngine.getInstance().monitorCreated(this);
    }

    /**
     * Used to set the inherited attributes on this
     * monitor.
     *
     * @param inheritedAttributes the collection of inherited attributes
     */
    protected void setInheritedAttributes(Map inheritedAttributes) {
        if(inheritedAttributes != null) {
            for(Iterator i = inheritedAttributes.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                if(value != null && AttributeHolder.class.isAssignableFrom(value.getClass())) {
                    value = ((AttributeHolder) value).getValue();
                }
                set(key, value).lock();
            }
        }
    }

    /**
     * Used to invoke the monitor lifecycle method
     *   MonitoringEngine.process
     * on this monitor.
     */
    protected void process() {
        if (processed) {
            log.error("This monitor has already been processed: " + this);
        } else {
            MonitoringEngine.getInstance().process(this);
            processed = true;
        }
    }

    protected AttributeMap createAttributeMap() {
        return new AttributeMap();
    }

    protected AttributeMap getAttributes() {
        return attributes;
    }
    
    // ** PRIVATE Methods
    private static Set buildInvalidCharSet() {
        Set set = new HashSet();
        char[] invalidArr = invalidCharacters.toCharArray();
        for (int i = 0; i < invalidArr.length; i++) {
            set.add(new Character(invalidArr[i]));
        }
        return set;
    }
}
