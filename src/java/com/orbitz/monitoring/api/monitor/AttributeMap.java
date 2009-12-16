package com.orbitz.monitoring.api.monitor;

import com.orbitz.monitoring.api.AttributeUndefinedException;
import com.orbitz.monitoring.api.CantCoerceException;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A map-like class that can be used to hold attributes for a Monitor. This
 * class requires that keys are an instance of {@link String}. Also, it has
 * methods for getting and setting primitives as the values of those attributes.
 * Additionally, any value set to null is implicitly removed from the the map.
 *
 * @author Doug Barth
 */

public class AttributeMap implements Serializable {
    private static final Logger logger = Logger.getLogger(AttributeMap.class);
    /**
     * Hard version used to support evolutionary compatibility during
     * serialization between client and server.  Basically, an older instance
     * of this class can be compatible with a newer one, if the new additions
     * are optional / ancillary to core functionality / backward compatible.
     *
     * NOTE: Changing this value requires coordination with other teams.
     *       !! TREAD LIGHTLY !!
     */
    private static final long serialVersionUID = 2L;

    private ConcurrentHashMap attributes;

    protected static final Pattern p = Pattern.compile("[a-zA-Z_]+[a-zA-Z_0-9]*");

    public AttributeMap() {
        attributes = new ConcurrentHashMap();
    }

    public AttributeMap(Map attributeMap) {
        this();
        if (attributeMap != null) {
            setAll(attributeMap);
        }
    }

    public ConcurrentHashMap getAttributes() {
        return attributes;
    }
    
    public AttributeHolder set(String key, short value) {
        return internalSetAttribute(key, new Short(value));
    }

    public AttributeHolder set(String key, int value) {
        return internalSetAttribute(key, new Integer(value));
    }

    public AttributeHolder set(String key, long value) {
        return internalSetAttribute(key, new Long(value));
    }

    public AttributeHolder set(String key, float value) {
        return internalSetAttribute(key, new Float(value));
    }

    public AttributeHolder set(String key, double value) {
        return internalSetAttribute(key, new Double(value));
    }

    public AttributeHolder set(String key, char value) {
        return internalSetAttribute(key, new Character(value));
    }

    public AttributeHolder set(String key, byte value) {
        return internalSetAttribute(key, new Byte(value));
    }

    public AttributeHolder set(String key, boolean value) {
        return internalSetAttribute(key, Boolean.valueOf(value));
    }

    public AttributeHolder set(String key, Object value) {
        return internalSetAttribute(key, value);
    }

    public void setAll(Map attributes) {
        setAllAttributeHolders(attributes);
    }

    public void setAllAttributeHolders(Map attributeHolders) {
        if (attributeHolders == null) return;

        for (Iterator i = attributeHolders.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();

            if(value != null) {
                if (AttributeHolder.class.isAssignableFrom(value.getClass())) {
                    AttributeHolder original = (AttributeHolder) value;
                    AttributeHolder copy = (AttributeHolder) original.clone();
                    getAttributes().put(key, copy);
                } else {
                    set(key, value);
                }
            }
        }
    }

    public void unset(String key) {
        attributes.remove(key);
    }

    public void clear() {
        attributes.clear();
    }

    public Object get(String key) {
        if (hasAttribute(key)) {
            AttributeHolder attributeHolder = (AttributeHolder)attributes.get(key);
            return attributeHolder.getValue();
        } else {
            throw new AttributeUndefinedException(key);
        }
    }

    public Map getAsMap(String key) {
        Object value = get(key);

        if (value instanceof Map) {
            return (Map) value;
        } else {
            throw new CantCoerceException(key, value, "Map");
        }
    }

    public List getAsList(String key) {
        Object value = get(key);

        if (value != null && !(value instanceof List)) {
            if (value instanceof Object[]) {
                value = Arrays.asList((Object[]) value);
            } else {
                throw new CantCoerceException(key, value, "List");
            }
        }

        return (List) value;
    }

    public Set getAsSet(String key) {
        Object value = get(key);

        if (value instanceof Set) {
            return (Set) value;
        } else {
            throw new CantCoerceException(key, value, "Set");
        }
    }

    public String getAsString(String key) {
        Object attribute = get(key);

        return attribute == null ? null : attribute.toString();
    }

    public short getAsShort(String key) {
        Object value = get(key);

        if (value == null) {
            throw new AttributeUndefinedException(key);
        }

        if (!(value instanceof Number)) {
            try {
                return Short.parseShort(value.toString());
            } catch (NumberFormatException e) {
                throw new CantCoerceException(key, value, "short");
            }
        }

        return ((Number) value).shortValue();
    }

    public int getAsInt(String key) {
        Object value = get(key);

        if (value == null) {
            throw new AttributeUndefinedException(key);
        }

        if (!(value instanceof Number)) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                throw new CantCoerceException(key, value, "int");
            }
        }

        return ((Number) value).intValue();
    }

    public long getAsLong(String key) {
        Object value = get(key);

        if (value == null) {
            throw new AttributeUndefinedException(key);
        }

        if (!(value instanceof Number)) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                throw new CantCoerceException(key, value, "long");
            }
        }

        return ((Number) value).longValue();
    }

    public float getAsFloat(String key) {
        Object value = get(key);

        if (value == null) {
            throw new AttributeUndefinedException(key);
        }

        if (!(value instanceof Number)) {
            try {
                return Float.parseFloat(value.toString());
            } catch (NumberFormatException e) {
                throw new CantCoerceException(key, value, "float");
            }
        }

        return ((Number) value).floatValue();
    }

    public double getAsDouble(String key) {
        Object value = get(key);

        if (value == null) {
            throw new AttributeUndefinedException(key);
        }

        if (!(value instanceof Number)) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                throw new CantCoerceException(key, value, "double");
            }
        }

        return ((Number) value).doubleValue();
    }

    public char getAsChar(String key) {
        Object value = get(key);

        if (value == null) {
            throw new AttributeUndefinedException(key);
        }

        if (!(value instanceof Character)) {
            if (value instanceof String) {
                String stringValue = (String) value;
                if (stringValue.length() == 1) {
                    return stringValue.charAt(0);
                }
            }
            throw new CantCoerceException(key, value, "char");
        }

        return ((Character) value).charValue();
    }

    public byte getAsByte(String key) {
        Object value = get(key);

        if (value == null) {
            throw new AttributeUndefinedException(key);
        }

        if (!(value instanceof Number)) {
            try {
                return Byte.parseByte(value.toString());
            } catch (NumberFormatException e) {
                throw new CantCoerceException(key, value, "byte");
            }
        }

        return ((Number) value).byteValue();
    }

    public boolean getAsBoolean(String key) {
        Object value = get(key);

        if (value == null) {
            throw new AttributeUndefinedException(key);
        }

        if (!(value instanceof Boolean)) {
            if ("true".equalsIgnoreCase(value.toString())) {
                return true;
            } else if ("false".equalsIgnoreCase(value.toString())) {
                return false;
            }
            throw new CantCoerceException(key, value, "boolean");
        }

        return ((Boolean) value).booleanValue();
    }

    public Map getAll() {
        Map all = new HashMap();
        for (Iterator i = attributes.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            AttributeHolder attributeHolder = (AttributeHolder) entry.getValue();

            Object value = attributeHolder.getValue();
            all.put(key, value);
        }
        return all;
    }

    public Map getAllAttributeHolders() {
        return new HashMap(attributes);
    }

    public Map getAllSerializable() {
        Map allSerializable = new HashMap();
        for (Iterator i = attributes.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            AttributeHolder attributeHolder = (AttributeHolder) entry.getValue();
            
            if (attributeHolder.isSerializable()) {
                Object value = attributeHolder.getValue();
                allSerializable.put(key, value);
            }
        }
        return allSerializable;
    }

    public boolean getAsBoolean(String key, boolean defaultValue) {
        if (!hasAttribute(key)) {
            return defaultValue;
        } else {
            return getAsBoolean(key);
        }
    }

    public short getAsShort(String key, short defaultValue) {
        if (!(hasAttribute(key))) {
            return defaultValue;
        } else {
            return getAsShort(key);
        }
    }

    public byte getAsByte(String key, byte defaultValue) {
        if (!(hasAttribute(key))) {
            return defaultValue;
        } else {
            return getAsByte(key);
        }
    }

    public int getAsInt(String key, int defaultValue) {
        if (!(hasAttribute(key))) {
            return defaultValue;
        } else {
            return getAsInt(key);
        }
    }

    public long getAsLong(String key, long defaultValue) {
        if (!(hasAttribute(key))) {
            return defaultValue;
        } else {
            return getAsLong(key);
        }
    }

    public float getAsFloat(String key, float defaultValue) {
        if (!(hasAttribute(key))) {
            return defaultValue;
        } else {
            return getAsFloat(key);
        }
    }

    public double getAsDouble(String key, double defaultValue) {
        if (!(hasAttribute(key))) {
            return defaultValue;
        } else {
            return getAsDouble(key);
        }
    }

    public char getAsChar(String key, char defaultValue) {
        if (! hasAttribute(key)) {
            return defaultValue;
        } else {
            return getAsChar(key);
        }
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    public String toString() {
        return getAll().toString();
    }

    protected AttributeHolder internalSetAttribute(String key, Object value) {
        Matcher m = p.matcher(key);
        if (! m.matches()) {
            throw new IllegalArgumentException("Attribute [" + key +
                    "] violates attribute name restriction, attribute not added.");
        }

        AttributeHolder attributeHolder = (AttributeHolder) attributes.get(key);

        if (attributeHolder == null) {
            // create a new holder in the map with the given value
            attributeHolder = createHolderForValue(value);
            attributes.put(key, attributeHolder);
        } else {
            // if an existing attribute holder is locked, just ignore the attempt to
            // overwrite its value
            if (attributeHolder.isLocked()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempt to overwrite locked attribute with key '" + key + "'");
                }
            } else {
                attributeHolder = createHolderForValue(attributeHolder, value);
                attributes.put(key, attributeHolder);
            }
        }

        return attributeHolder;
    }

    protected AttributeHolder createHolderForValue(AttributeHolder old, Object value) {
        AttributeHolder attributeHolder = new AttributeHolder(value);
        if (old.isSerializable()) attributeHolder.serializable();
        return attributeHolder;
    }


    protected AttributeHolder createHolderForValue(Object value) {
        return new AttributeHolder(value);
    }
}
