package com.orbitz.monitoring.api.monitor;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.orbitz.monitoring.api.AttributeUndefinedException;
import com.orbitz.monitoring.api.CantCoerceException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 * A map-like class that can be used to hold attributes for a Monitor. This class requires that keys
 * are an instance of {@link String}. Also, it has methods for getting and setting primitives as the
 * values of those attributes. Additionally, any value set to null is implicitly removed from the
 * the map.
 * 
 * @author Doug Barth
 */

public class AttributeMap implements Serializable {
  private static final Logger logger = Logger.getLogger(AttributeMap.class);
  /**
   * Hard version used to support evolutionary compatibility during serialization between client and
   * server. Basically, an older instance of this class can be compatible with a newer one, if the
   * new additions are optional / ancillary to core functionality / backward compatible.
   * 
   * NOTE: Changing this value requires coordination with other teams. !! TREAD LIGHTLY !!
   */
  private static final long serialVersionUID = 2L;
  
  private ConcurrentHashMap<String, AttributeHolder> attributes;
  
  protected static final Pattern p = Pattern.compile("[a-zA-Z_]+[a-zA-Z_0-9]*");
  
  public AttributeMap() {
    attributes = new ConcurrentHashMap();
  }
  
  public AttributeMap(final Map attributeMap) {
    this();
    if (attributeMap != null) {
      setAll(attributeMap);
    }
  }
  
  public ConcurrentHashMap getAttributes() {
    return attributes;
  }
  
  /**
   * Finds all {@link CompositeAttributeHolder composite attributes}
   * @return a new map containing the composite attributes
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Map<String, CompositeAttributeHolder> findCompositeAttributes() {
    return (Map)Maps
        .filterValues(attributes, Predicates.instanceOf(CompositeAttributeHolder.class));
  }
  
  public AttributeHolder set(final String key, final short value) {
    return internalSetAttribute(key, new Short(value));
  }
  
  public AttributeHolder set(final String key, final int value) {
    return internalSetAttribute(key, new Integer(value));
  }
  
  public AttributeHolder set(final String key, final long value) {
    return internalSetAttribute(key, new Long(value));
  }
  
  public AttributeHolder set(final String key, final float value) {
    return internalSetAttribute(key, new Float(value));
  }
  
  public AttributeHolder set(final String key, final double value) {
    return internalSetAttribute(key, new Double(value));
  }
  
  public AttributeHolder set(final String key, final char value) {
    return internalSetAttribute(key, new Character(value));
  }
  
  public AttributeHolder set(final String key, final byte value) {
    return internalSetAttribute(key, new Byte(value));
  }
  
  public AttributeHolder set(final String key, final boolean value) {
    return internalSetAttribute(key, Boolean.valueOf(value));
  }
  
  public AttributeHolder set(final String key, final Object value) {
    return internalSetAttribute(key, value);
  }
  
  public void setAll(final Map attributes) {
    setAllAttributeHolders(attributes);
  }
  
  public void setAllAttributeHolders(final Map attributeHolders) {
    if (attributeHolders == null) {
      return;
    }
    
    for (final Iterator i = attributeHolders.entrySet().iterator(); i.hasNext();) {
      final Map.Entry entry = (Map.Entry)i.next();
      final String key = (String)entry.getKey();
      final Object value = entry.getValue();
      
      if (value != null) {
        if (AttributeHolder.class.isAssignableFrom(value.getClass())) {
          final AttributeHolder original = (AttributeHolder)value;
          final AttributeHolder copy = (AttributeHolder)original.clone();
          getAttributes().put(key, copy);
        }
        else {
          set(key, value);
        }
      }
    }
  }
  
  public void unset(final String key) {
    attributes.remove(key);
  }
  
  public void clear() {
    attributes.clear();
  }
  
  public Object get(final String key) {
    if (hasAttribute(key)) {
      final AttributeHolder attributeHolder = attributes.get(key);
      return attributeHolder.getValue();
    }
    else {
      throw new AttributeUndefinedException(key);
    }
  }
  
  public Map getAsMap(final String key) {
    final Object value = get(key);
    
    if (value instanceof Map) {
      return (Map)value;
    }
    else {
      throw new CantCoerceException(key, value, "Map");
    }
  }
  
  public List getAsList(final String key) {
    Object value = get(key);
    
    if (value != null && !(value instanceof List)) {
      if (value instanceof Object[]) {
        value = Arrays.asList((Object[])value);
      }
      else {
        throw new CantCoerceException(key, value, "List");
      }
    }
    
    return (List)value;
  }
  
  public Set getAsSet(final String key) {
    final Object value = get(key);
    
    if (value instanceof Set) {
      return (Set)value;
    }
    else {
      throw new CantCoerceException(key, value, "Set");
    }
  }
  
  public String getAsString(final String key) {
    final Object attribute = get(key);
    
    return attribute == null ? null : attribute.toString();
  }
  
  public short getAsShort(final String key) {
    final Object value = get(key);
    
    if (value == null) {
      throw new AttributeUndefinedException(key);
    }
    
    if (!(value instanceof Number)) {
      try {
        return Short.parseShort(value.toString());
      }
      catch (final NumberFormatException e) {
        throw new CantCoerceException(key, value, "short");
      }
    }
    
    return ((Number)value).shortValue();
  }
  
  public int getAsInt(final String key) {
    final Object value = get(key);
    
    if (value == null) {
      throw new AttributeUndefinedException(key);
    }
    
    if (!(value instanceof Number)) {
      try {
        return Integer.parseInt(value.toString());
      }
      catch (final NumberFormatException e) {
        throw new CantCoerceException(key, value, "int");
      }
    }
    
    return ((Number)value).intValue();
  }
  
  public long getAsLong(final String key) {
    final Object value = get(key);
    
    if (value == null) {
      throw new AttributeUndefinedException(key);
    }
    
    if (!(value instanceof Number)) {
      try {
        return Long.parseLong(value.toString());
      }
      catch (final NumberFormatException e) {
        throw new CantCoerceException(key, value, "long");
      }
    }
    
    return ((Number)value).longValue();
  }
  
  public float getAsFloat(final String key) {
    final Object value = get(key);
    
    if (value == null) {
      throw new AttributeUndefinedException(key);
    }
    
    if (!(value instanceof Number)) {
      try {
        return Float.parseFloat(value.toString());
      }
      catch (final NumberFormatException e) {
        throw new CantCoerceException(key, value, "float");
      }
    }
    
    return ((Number)value).floatValue();
  }
  
  public double getAsDouble(final String key) {
    final Object value = get(key);
    
    if (value == null) {
      throw new AttributeUndefinedException(key);
    }
    
    if (!(value instanceof Number)) {
      try {
        return Double.parseDouble(value.toString());
      }
      catch (final NumberFormatException e) {
        throw new CantCoerceException(key, value, "double");
      }
    }
    
    return ((Number)value).doubleValue();
  }
  
  public char getAsChar(final String key) {
    final Object value = get(key);
    
    if (value == null) {
      throw new AttributeUndefinedException(key);
    }
    
    if (!(value instanceof Character)) {
      if (value instanceof String) {
        final String stringValue = (String)value;
        if (stringValue.length() == 1) {
          return stringValue.charAt(0);
        }
      }
      throw new CantCoerceException(key, value, "char");
    }
    
    return ((Character)value).charValue();
  }
  
  public byte getAsByte(final String key) {
    final Object value = get(key);
    
    if (value == null) {
      throw new AttributeUndefinedException(key);
    }
    
    if (!(value instanceof Number)) {
      try {
        return Byte.parseByte(value.toString());
      }
      catch (final NumberFormatException e) {
        throw new CantCoerceException(key, value, "byte");
      }
    }
    
    return ((Number)value).byteValue();
  }
  
  public boolean getAsBoolean(final String key) {
    final Object value = get(key);
    
    if (value == null) {
      throw new AttributeUndefinedException(key);
    }
    
    if (!(value instanceof Boolean)) {
      if ("true".equalsIgnoreCase(value.toString())) {
        return true;
      }
      else if ("false".equalsIgnoreCase(value.toString())) {
        return false;
      }
      throw new CantCoerceException(key, value, "boolean");
    }
    
    return ((Boolean)value).booleanValue();
  }
  
  public Map getAll() {
    final Map all = new HashMap();
    for (final Iterator i = attributes.entrySet().iterator(); i.hasNext();) {
      final Map.Entry entry = (Map.Entry)i.next();
      final String key = (String)entry.getKey();
      final AttributeHolder attributeHolder = (AttributeHolder)entry.getValue();
      
      final Object value = attributeHolder.getValue();
      all.put(key, value);
    }
    return all;
  }
  
  public Map<String, AttributeHolder> getAllAttributeHolders() {
    return new HashMap<String, AttributeHolder>(attributes);
  }
  
  public Map getAllSerializable() {
    final Map allSerializable = new HashMap();
    for (final Iterator i = attributes.entrySet().iterator(); i.hasNext();) {
      final Map.Entry entry = (Map.Entry)i.next();
      final String key = (String)entry.getKey();
      final AttributeHolder attributeHolder = (AttributeHolder)entry.getValue();
      
      if (attributeHolder.isSerializable()) {
        final Object value = attributeHolder.getValue();
        allSerializable.put(key, value);
      }
    }
    return allSerializable;
  }
  
  public boolean getAsBoolean(final String key, final boolean defaultValue) {
    if (!hasAttribute(key)) {
      return defaultValue;
    }
    else {
      return getAsBoolean(key);
    }
  }
  
  public short getAsShort(final String key, final short defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsShort(key);
    }
  }
  
  public byte getAsByte(final String key, final byte defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsByte(key);
    }
  }
  
  public int getAsInt(final String key, final int defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsInt(key);
    }
  }
  
  public long getAsLong(final String key, final long defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsLong(key);
    }
  }
  
  public float getAsFloat(final String key, final float defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsFloat(key);
    }
  }
  
  public double getAsDouble(final String key, final double defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsDouble(key);
    }
  }
  
  public char getAsChar(final String key, final char defaultValue) {
    if (!hasAttribute(key)) {
      return defaultValue;
    }
    else {
      return getAsChar(key);
    }
  }
  
  public boolean hasAttribute(final String key) {
    return attributes.containsKey(key);
  }
  
  @Override
  public String toString() {
    return getAll().toString();
  }
  
  protected AttributeHolder internalSetAttribute(final String key, final Object value) {
    final Matcher m = p.matcher(key);
    if (!m.matches()) {
      throw new IllegalArgumentException("Attribute [" + key
          + "] violates attribute name restriction, attribute not added.");
    }
    
    AttributeHolder attributeHolder = attributes.get(key);
    
    if (attributeHolder == null) {
      // create a new holder in the map with the given value
      attributeHolder = createHolderForValue(value);
      attributes.put(key, attributeHolder);
    }
    else {
      // if an existing attribute holder is locked, just ignore the attempt to
      // overwrite its value
      if (attributeHolder.isLocked()) {
        if (logger.isDebugEnabled()) {
          logger.debug("Attempt to overwrite locked attribute with key '" + key + "'");
        }
      }
      else {
        attributeHolder = createHolderForValue(attributeHolder, value);
        attributes.put(key, attributeHolder);
      }
    }
    
    return attributeHolder;
  }
  
  protected CompositeAttributeHolder createHolderForValue(final AttributeHolder old,
      final Object value) {
    final CompositeAttributeHolder attributeHolder = new CompositeAttributeHolder(value);
    if (old.isSerializable()) {
      attributeHolder.serializable();
    }
    return attributeHolder;
  }
  
  protected CompositeAttributeHolder createHolderForValue(final Object value) {
    return new CompositeAttributeHolder(value);
  }
}
