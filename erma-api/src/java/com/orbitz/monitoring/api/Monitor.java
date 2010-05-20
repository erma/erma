package com.orbitz.monitoring.api;

import com.orbitz.monitoring.api.monitor.serializable.SerializableMonitor;
import com.orbitz.monitoring.api.monitor.AttributeHolder;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Date;

/**
 * The contract for all instrumentation objects. Any object that is created
 * to be used to monitor some event in the application must implement this
 * interface.<p>
 *
 * Monitor should be implemented as single use objects. The objects are created,
 * data is put into them, and then they are submitted to the MonitoringEngine
 * for processing. Ideally, the monitor object should use semantically rich
 * methods to wrap the submission logic.<p>
 *
 * Monitors store all their data as key/value pairs called attributes. The key
 * is a string that defines the nature of the attribute. The value can be any
 * <code>Object</code> or primitive.Keys should match the Java variable naming
 * requirements.<p>
 *
 * When retrieving an attribute using the <code>get</code> methods, an
 * expression can be supplied. Documentation on the expression syntax can be
 * found at http://jakarta.apache.org/commons/beanutils/.
 *
 * @author Doug Barth
 */
public interface Monitor {
    /**
     * This adds an attribute to this monitor with the supplied value.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as a short
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, short value);

    /**
     * This adds an attribute to this monitor with the supplied value.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as an int
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, int value);

    /**
     * This adds an attribute to this monitor with the supplied value.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as a long
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, long value);

    /**
     * This adds an attribute to this monitor with the supplied value.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as a float
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, float value);

    /**
     * This adds an attribute to this monitor with the supplied value.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as a double
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, double value);

    /**
     * This adds an attribute to this monitor with the supplied value.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as a char
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, char value);

    /**
     * This adds an attribute to this monitor with the supplied value.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as a byte
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, byte value);

    /**
     * This adds an attribute to this monitor with the supplied value.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as a boolean
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, boolean value);

    /**
     * This adds a Date attribute to this monitor.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as a Date
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, Date value);

    /**
     * This adds a String attribute to this monitor.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as a String
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, String value);

    /**
     * This adds an object attribute to this monitor. If the object is null, the
     * attribute is removed from this monitor.
     *
     * @param key the string that identifies this attribute
     * @param value the value of this attribute as an Object
     * @return an AttributeHolder containing the given value and its metadata
     */
    AttributeHolder set(String key, Object value);

    /**
     * Adds all the attributes in the supplied map to this Monitor.
     *
     * @param attributes the map of attributes to add
     */
    void setAll(Map attributes);

    /**
     * Adds all supplied attributes to this monitor, including meta-data
     * @param attributeHolders map of key -> AttributeHolder
     */
    void setAllAttributeHolders(Map attributeHolders);

    /**
     * Unset the attribute specified by the key.
     * @param key a String indicating the attribute to unset.
     */
    void unset(String key);
    
    /**
     * Gets the attribute value for the given key as an Object.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies this attribute
     * @return the value of this attribute, as an Object; null if the key is
     *         undefined
     */
    Object get(String key);

    /**
     * Get the attribute value for the given key as a boolean.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a boolean primitive
     *
     * @throws AttributeUndefinedException if the key is not defined
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         boolean
     */
    boolean getAsBoolean(String key);

    /**
     * Get the attribute value for the given key as a short.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a short primitive
     *
     * @throws AttributeUndefinedException if the key is not defined
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         short
     */
    short getAsShort(String key);

    /**
     * Get the attribute value for the given key as a byte.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a byte primitive
     *
     * @throws AttributeUndefinedException if the key is not defined
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         byte
     */
    byte getAsByte(String key);

    /**
     * Get the attribute value for the given key as an int.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to an int primitive
     *
     * @throws AttributeUndefinedException if the key is not defined
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         int
     */
    int getAsInt(String key);

    /**
     * Get the attribute value for the given key as a long.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a long primitive
     *
     * @throws AttributeUndefinedException if the key is not defined
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         long
     */
    long getAsLong(String key);

    /**
     * Get the attribute value for the given key as a float.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a float primitive
     *
     * @throws AttributeUndefinedException if the key is not defined
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         float
     */
    float getAsFloat(String key);

    /**
     * Get the attribute value for the given key as a double.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a double primitive
     *
     * @throws AttributeUndefinedException if the key is not defined
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         double
     */
    double getAsDouble(String key);

    /**
     * Get the attribute value for the given key as a char.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a char primitive
     *
     * @throws AttributeUndefinedException if the key is not defined
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         char
     */
    char getAsChar(String key);

    /**
     * Get the attribute value for the given key as a String.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a String, or null if
     *         undefined
     */
    String getAsString(String key);

    /**
     * Get the attribute value for the given key as a Map.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a Map, or null if undefined
     */
    Map getAsMap(String key);

    /**
     * Get the attribute value for the given key as a List.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a List, or null if undefined
     */
    List getAsList(String key);

    /**
     * Get the attribute value for the given key as a Set.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return the value of this attribute, cast to a Set, or null if undefined
     */
    Set getAsSet(String key);

    /**
     * Get the attribute value for the given key as a boolean. If the key is
     * undefined, the provided default value will be used.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @param defaultValue the value to be returned if the key is undefined
     * @return the value of this attribute, cast to a boolean primitive, or the
     *         default value if undefined
     *
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         boolean
     */
    boolean getAsBoolean(String key, boolean defaultValue);

    /**
     * Get the attribute value for the given key as a short. If the key is
     * undefined, the provided default value will be used.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @param defaultValue the value to be returned if the key is undefined
     * @return the value of this attribute, cast to a short primitive, or the
     *         default value if undefined
     *
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         short
     */
    short getAsShort(String key, short defaultValue);

    /**
     * Get the attribute value for the given key as a byte. If the key is
     * undefined, the provided default value will be used.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @param defaultValue the value to be returned if the key is undefined
     * @return the value of this attribute, cast to a byte primitive, or the
     *         default value if undefined
     *
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         byte
     */
    byte getAsByte(String key, byte defaultValue);

    /**
     * Get the attribute value for the given key as an int. If the key is
     * undefined, the provided default value will be used.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @param defaultValue the value to be returned if the key is undefined
     * @return the value of this attribute, cast to an int primitive, or the
     *         default value if undefined
     *
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         int
     */
    int getAsInt(String key, int defaultValue);

    /**
     * Get the attribute value for the given key as a long. If the key is
     * undefined, the provided default value will be used.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @param defaultValue the value to be returned if the key is undefined
     * @return the value of this attribute, cast to a long primitive, or the
     *         default value if undefined
     *
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         long
     */
    long getAsLong(String key, long defaultValue);

    /**
     * Get the attribute value for the given key as a float. If the key is
     * undefined, the provided default value will be used.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @param defaultValue the value to be returned if the key is undefined
     * @return the value of this attribute, cast to a float primitive, or the
     *         default value if undefined
     *
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         float
     */
    float getAsFloat(String key, float defaultValue);

    /**
     * Get the attribute value for the given key as a double. If the key is
     * undefined, the provided default value will be used.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @param defaultValue the value to be returned if the key is undefined
     * @return the value of this attribute, cast to a double primitive, or the
     *         default value if undefined
     *
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         double
     */
    double getAsDouble(String key, double defaultValue);

    /**
     * Get the attribute value for the given key as a char. If the key is
     * undefined, the provided default value will be used.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @param defaultValue the value to be returned if the key is undefined
     * @return the value of this attribute, cast to a char primitive, or the
     *         default value if undefined
     *
     * @throws CantCoerceException if the key's value cannot be coerced to a
     *         char
     */
    char getAsChar(String key, char defaultValue);

    /**
     * Gets a map of all the attributes for this Monitor.
     *
     * @return a reference to the map containing all the attributes for this
     *         Monitor
     */
    Map getAll();

    /**
     * Gets a map of all attributes that are serializable
     * @return a copy of a map containing all serializable attributes for this monitor
     */
    Map getAllSerializable();

    /**
     * Gets the MonitoringLevel associated with this Monitor
     * @return MonitoringLevel for this Monitor
     */
    MonitoringLevel getLevel();

    /**
     * Returns whether this monitor has the attribute supplied.
     *
     * <p>The key can be an expression that starts with the top level attribute
     * and digs into the object to find the desired attribute.
     *
     * @param key the string that identifies the attribute
     * @return true if this monitor has a value for the attribute, false
     *         otherwise
     */
    boolean hasAttribute(String key);

    /**
     * Returns a SerializableMonitor instance that represents the state of this
     * monitor.
     *
     * @return a SeriailizableMonitor instance with all the same attributes
     *         as this monitor
     */
    SerializableMonitor getSerializableMomento();

    /**
     * The name of this Monitor instance. This name should describe what is
     * being monitored.
     *
     * @deprecated use Attribute.NAME instead
     */
    static final String NAME = Attribute.NAME;

    /**
     * The VM id of the system that this monitor was monitoring. The
     * MonitoringEngine sets this attribute when it receives the initMonitor()
     * callback.
     *
     * @deprecated use Attribute.VMID instead
     */
    static final String VMID = Attribute.VMID;

    /**
     * The host name of the system that this monitor was monitoring. The
     * MonitoringEngine sets this attribute when it receives the initMonitor()
     * callback.
     *
     * @deprecated use Attribute.HOSTNAME instead
     */
    static final String HOSTNAME = Attribute.HOSTNAME;

    /**
     * The unqiue identifier of the thread that was being monitored. The
     * MonitoringEngine set this attribute when it receives the initMonitor()
     * callback.
     *
     * @deprecated use Attribute.THREAD_ID instead
     */
    static final String THREAD_ID = Attribute.THREAD_ID;

    /**
     * The time that this monitor was created. The MonitoringEngine sets this 
     * attribute when it receives the initMonitor() callback.
     *
     * @deprecated use Attribute.CREATED_AT instead
     */
    static final String CREATED_AT = Attribute.CREATED_AT;

    /**
     * The unqiue identifier of the monitor during a given path of execution. The
     * MonitoringEngine set this attribute when it receives the initMonitor()
     * callback.
     *
     * @deprecated use Attribute.SEQUENCE_ID instead
     */
    static final String SEQUENCE_ID = Attribute.SEQUENCE_ID;

    /**
     * The unqiue identifier of the parent monitor during a given path of execution.
     * The MonitoringEngine set this attribute when it receives the initMonitor()
     * callback.
     *
     * @deprecated use Attribute.PARENT_SEQUENCE_ID instead
     */
    static final String PARENT_SEQUENCE_ID = Attribute.PARENT_SEQUENCE_ID;

    /**
     * The class of this Monitor instance.
     *
     * @deprecated use Attribute.CLASS_NAME instead
     */
    static final String CLASS_NAME = Attribute.CLASS_NAME;
}
