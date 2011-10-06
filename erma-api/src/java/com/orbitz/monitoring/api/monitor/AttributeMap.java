package com.orbitz.monitoring.api.monitor;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.orbitz.monitoring.api.AttributeUndefinedException;
import com.orbitz.monitoring.api.CantCoerceException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 * A map-like class that can be used to hold attributes for a Monitor. This class requires that keys
 * are {@link String strings}. Also, it has methods for getting and setting primitives as the values
 * of those attributes. This map does not support null values. Values set to null are removed from
 * the the map.
 * @author Doug Barth
 */

public class AttributeMap implements Serializable {
  private static final Pattern ATTRIBUTE_NAME_PATTERN = Pattern.compile("[a-zA-Z_]+[a-zA-Z_0-9]*");
  private static final long serialVersionUID = 2L;
  
  private final ConcurrentHashMap<String, AttributeHolder> attributes;
  private transient final Logger logger = Logger.getLogger(AttributeMap.class);
  
  /**
   * Creates an empty attribute map
   */
  public AttributeMap() {
    attributes = new ConcurrentHashMap<String, AttributeHolder>();
  }
  
  /**
   * Creates an attribute map with a pre-filled set of attributes. Attributes will be copied from
   * the specified attributeMap.
   * @param attributeMap the initial values. Ignored if null.
   */
  public AttributeMap(final Map<String, Object> attributeMap) {
    this();
    if (attributeMap != null) {
      setAll(attributeMap);
    }
  }
  
  /**
   * Removes all entries from this map
   */
  public void clear() {
    attributes.clear();
  }
  
  /**
   * Creates a view of the list that lazily converts elements of the list that are arrays to
   * {@link List lists}. The conversion is applied recursively.
   * @param items the list to convert
   * @return a view of the list that creates lists for array elements
   */
  private List<?> convertArraysToLists(final List<?> items) {
    return Lists.transform(items, new Function<Object, Object>() {
      public Object apply(final Object in) {
        if (in instanceof Object[]) {
          return convertArraysToLists(Lists.newArrayList((Object[])in));
        }
        return in;
      }
    });
  }
  
  /**
   * Creates a new {@link CompositeAttributeHolder} for a value
   * @param old the existing {@link AttributeHolder}. Note that the value of the holder is ignored
   *        and it does not have to be "{@link CompositeAttributeHolder composite}".
   * @param value the value to be held
   * @return a new {@link CompositeAttributeHolder composite holder} holding the value with its
   *         {@link CompositeAttributeHolder#isSerializable() serializable} attribute set to the
   *         same value as the specified holder.
   */
  protected CompositeAttributeHolder createHolderForValue(final AttributeHolder old,
      final Object value) {
    final CompositeAttributeHolder attributeHolder = new CompositeAttributeHolder(value);
    if (old.isSerializable()) {
      attributeHolder.serializable();
    }
    return attributeHolder;
  }
  
  /**
   * Creates a new {@link CompositeAttributeHolder} holding the specified value
   * @param value the value to hold
   * @return the holder
   */
  protected CompositeAttributeHolder createHolderForValue(final Object value) {
    return new CompositeAttributeHolder(value);
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
  
  /**
   * Gets the value of a key
   * @param key the key to find
   * @return the value at the specified key
   * @throws AttributeUndefinedException if the key doesn't exist
   */
  public Object get(final String key) {
    if (this.hasAttribute(key)) {
      AttributeHolder attribute = attributes.get(key);
      return (attribute == null) ? attribute : attribute.getValue();
    }
    throw new AttributeUndefinedException(key);
  }
  
  /**
   * Gets all values from this attribute map
   * @return a map of all keys to all values
   */
  @SuppressWarnings("unchecked")
  public <V> Map<String, V> getAll() {
    return Maps.transformValues(attributes, new Function<AttributeHolder, V>() {
      public V apply(final AttributeHolder attribute) {
        return (V)attribute.getValue();
      }
    });
  }
  
  /**
   * Creates a new map with the same mapping of {@link String strings} to {@link AttributeHolder
   * attribute holders} as this map
   * @return the new map
   */
  public Map<String, AttributeHolder> getAllAttributeHolders() {
    return new HashMap<String, AttributeHolder>(attributes);
  }
  
  /**
   * Gets the items from this map that have indicated they are {@link Serializable} through
   * {@link AttributeHolder#isSerializable()}.
   * @return a new map of keys to the {@link Serializable} values.
   */
  public <V> Map<String, V> getAllSerializable() {
    final Map<String, V> allSerializable = new HashMap<String, V>();
    for (Entry<String, AttributeHolder> entry : attributes.entrySet()) {
      final AttributeHolder attributeHolder = entry.getValue();
      if (attributeHolder.isSerializable()) {
        @SuppressWarnings("unchecked")
        final V value = (V)attributeHolder.getValue();
        allSerializable.put(entry.getKey(), value);
      }
    }
    return allSerializable;
  }
  
  /**
   * Gets the value at the specified key as a boolean
   * @param key the key to find
   * @return If the value is a {@link Boolean}, it is returned. If its {@link Object#toString()}
   *         matches the words "true" or "false", one of those values is returned.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the value is not a boolean and can't be converted to one
   */
  public boolean getAsBoolean(final String key) {
    final Object value = getNotNull(key);
    if (value instanceof Boolean) {
      return ((Boolean)value);
    }
    if ("true".equalsIgnoreCase(value.toString())) {
      return true;
    }
    if ("false".equalsIgnoreCase(value.toString())) {
      return false;
    }
    throw new CantCoerceException(key, value, "boolean");
  }
  
  /**
   * Gets the value at the specified key as a boolean
   * @param key the key of the value
   * @param defaultValue the value to return if the specified key doesn't exist
   * @return If the value is a {@link Boolean}, it is returned. If its {@link Object#toString()}
   *         matches the words "true" or "false", one of those values is returned. If the key
   *         doesn't exist, the specified default value is returned.
   * @throws CantCoerceException if the key exists, but the value is not a boolean and can't be
   *         converted to one
   */
  public boolean getAsBoolean(final String key, final boolean defaultValue) {
    if (!hasAttribute(key)) {
      return defaultValue;
    }
    else {
      return getAsBoolean(key);
    }
  }
  
  /**
   * Gets the value at the specified key as a byte
   * @param key the key of the value
   * @return If the value is a {@link Number}, its byte value is returned. Otherwise, its
   *         {@link Object#toString()} is called and the result is passed to
   *         {@link Byte#parseByte(String)}.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the key exists, but the value is not a byte and can't be
   *         converted to one
   */
  public byte getAsByte(final String key) {
    final Object value = getNotNull(key);
    if (value instanceof Number) {
      return ((Number)value).byteValue();
    }
    try {
      return Byte.parseByte(value.toString());
    }
    catch (final NumberFormatException e) {
      throw new CantCoerceException(key, value, "byte");
    }
  }
  
  /**
   * Gets the value at the specified key as a byte
   * @param key the key of the value
   * @param defaultValue the value to return if the key doesn't exist
   * @return If the value is a {@link Number}, its byte value is returned. Otherwise, its
   *         {@link Object#toString()} is called and the result is passed to
   *         {@link Byte#parseByte(String)}. If the key doesn't exist, the specified default value
   *         is returned.
   * @throws CantCoerceException if the key exists, but the value is not a byte and can't be
   *         converted to one
   */
  public byte getAsByte(final String key, final byte defaultValue) {
    if (hasAttribute(key)) {
      return getAsByte(key);
    }
    else {
      return defaultValue;
    }
  }
  
  /**
   * Gets the value at the specified key as a character
   * @param key the key of the value
   * @return If the value is a {@link Character}, the value. If it is a {@link String} and is of
   *         length 1, the character of the string.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the key exists, but the value is not a character and can't be
   *         converted to one
   */
  public char getAsChar(final String key) {
    final Object value = getNotNull(key);
    if (value instanceof Character) {
      return ((Character)value);
    }
    if (value instanceof String) {
      final String stringValue = (String)value;
      if (stringValue.length() == 1) {
        return stringValue.charAt(0);
      }
    }
    throw new CantCoerceException(key, value, "char");
  }
  
  /**
   * Gets the value at the specified key as a character
   * @param key the key of the value
   * @param defaultValue the value to return if the key doesn't exist
   * @return If the value is a {@link Character}, the value. If it is a {@link String} and is of
   *         length 1, the character of the string. If the value doesn't exist, defaultValue.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the key exists, but the value is not a character and can't be
   *         converted to one
   */
  public char getAsChar(final String key, final char defaultValue) {
    if (!hasAttribute(key)) {
      return defaultValue;
    }
    else {
      return getAsChar(key);
    }
  }
  
  /**
   * Gets the value at the specified key as a double
   * @param key the key of the value
   * @return If the value is a {@link Number}, the value's {@link Number#doubleValue()}. Otherwise,
   *         its {@link Object#toString()} is called and the result is parsed as a double.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the key exists, but the value is not a double and can't be
   *         converted to one
   */
  public double getAsDouble(final String key) {
    final Object value = getNotNull(key);
    if (value instanceof Number) {
      return ((Number)value).doubleValue();
    }
    try {
      return Double.parseDouble(value.toString());
    }
    catch (final NumberFormatException e) {
      throw new CantCoerceException(key, value, "double");
    }
  }
  
  /**
   * Gets the value at the specified key as a double
   * @param key the key of the value
   * @param defaultValue the value to return if the key doesn't exist
   * @return If the value is a {@link Number}, the value's {@link Number#doubleValue()}. If it is
   *         not a number, its {@link Object#toString()} is called and the result is parsed as a
   *         double. If the key doesn't exist, defaultValue.
   * @throws CantCoerceException if the key exists, but the value is not a double and can't be
   *         converted to one
   */
  public double getAsDouble(final String key, final double defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsDouble(key);
    }
  }
  
  /**
   * Gets the value at the specified key as a float
   * @param key the key of the value
   * @return If the value is a {@link Number}, the value's {@link Number#floatValue()}. Otherwise,
   *         its {@link Object#toString()} is called and the result is parsed as a float.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the key exists, but the value is not a float and can't be
   *         converted to one
   */
  public float getAsFloat(final String key) {
    final Object value = getNotNull(key);
    if (value instanceof Number) {
      return ((Number)value).floatValue();
    }
    try {
      return Float.parseFloat(value.toString());
    }
    catch (final NumberFormatException e) {
      throw new CantCoerceException(key, value, "float");
    }
  }
  
  /**
   * Gets the value at the specified key as a float
   * @param key the key of the value
   * @param defaultValue the value to return if the key doesn't exist
   * @return If the value is a {@link Number}, the value's {@link Number#floatValue()}. If it is not
   *         a number, its {@link Object#toString()} is called and the result is parsed as a float.
   *         If the key doesn't exist, defaultValue.
   * @throws CantCoerceException if the key exists, but the value is not a float and can't be
   *         converted to one
   */
  public float getAsFloat(final String key, final float defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsFloat(key);
    }
  }
  
  /**
   * Gets the value at the specified key as an integer
   * @param key the key of the value
   * @return If the value is a {@link Number}, the value's {@link Number#intValue()}. Otherwise, its
   *         {@link Object#toString()} is called and the result is parsed as an integer.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the key exists, but the value is not an integer and can't be
   *         converted to one
   */
  public int getAsInt(final String key) {
    final Object value = getNotNull(key);
    if (value instanceof Number) {
      return ((Number)value).intValue();
    }
    try {
      return Integer.parseInt(value.toString());
    }
    catch (final NumberFormatException e) {
      throw new CantCoerceException(key, value, "int");
    }
  }
  
  /**
   * Gets the value at the specified key as an integer
   * @param key the key of the value
   * @param defaultValue the value to return if the key doesn't exist
   * @return If the value is a {@link Number}, the value's {@link Number#intValue()}. If it is not a
   *         number, its {@link Object#toString()} is called and the result is parsed as an integer.
   *         If the key does not exist, defaultValue.
   * @throws CantCoerceException if the key exists, but the value is not an integer and can't be
   *         converted to one
   */
  public int getAsInt(final String key, final int defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsInt(key);
    }
  }
  
  /**
   * Gets the value at the specified key as a list
   * @param key the key of the value
   * @return If the value is a list or null, the value. If it's an array, it will be converted to a
   *         list, along with any sub-arrays.
   * @throws CantCoerceException if the key exists, but the value is not a list and can't be
   *         converted to one
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> getAsList(final String key) {
    Object value = get(key);
    // TODO: Other methods throw AttributeUndefinedException if the value is null. Be consistent.
    if ((value == null) || (value instanceof List)) {
      return (List<T>)value;
    }
    // TODO: Remove coercion. If the client wants to get a list, they should put a list in.
    if (value instanceof Object[]) {
      final List<?> result = Arrays.asList((Object[])value);
      return Lists.transform(result, new Function<Object, T>() {
        public T apply(final Object in) {
          if (in instanceof Object[]) {
            return (T)convertArraysToLists(Arrays.asList((Object[])in));
          }
          return (T)in;
        }
      });
    }
    throw new CantCoerceException(key, value, "List");
  }
  
  /**
   * Gets the value at the specified key as a long
   * @param key the key of the value
   * @return If the value is a {@link Number}, the value's {@link Number#longValue()}. Otherwise,
   *         its {@link Object#toString()} is called and the result is parsed as a long.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the key exists, but the value is not a long and can't be
   *         converted to one
   */
  public long getAsLong(final String key) {
    final Object value = getNotNull(key);
    if (value instanceof Number) {
      return ((Number)value).longValue();
    }
    try {
      return Long.parseLong(value.toString());
    }
    catch (final NumberFormatException e) {
      throw new CantCoerceException(key, value, "long");
    }
  }
  
  /**
   * Gets the value at the specified key as a long
   * @param key the key of the value
   * @param defaultValue the value to return if the key doesn't exist
   * @return If the value is a {@link Number}, the value's {@link Number#longValue()}. If it's not a
   *         number, its {@link Object#toString()} is called and the result is parsed as a long. If
   *         the key doesn't exist, defaultValue.
   * @throws CantCoerceException if the key exists, but the value is not a long and can't be
   *         converted to one
   */
  public long getAsLong(final String key, final long defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsLong(key);
    }
  }
  
  /**
   * Gets the value at the specified key as a map
   * @param key the key of the value
   * @return the value
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the value is not a set
   */
  @SuppressWarnings("unchecked")
  public <K, V> Map<K, V> getAsMap(final String key) {
    final Object value = get(key);
    if (value instanceof Map) {
      return (Map<K, V>)value;
    }
    else {
      throw new CantCoerceException(key, value, "Map");
    }
  }
  
  /**
   * Gets the value at the specified key as a set
   * @param key the key of the value
   * @return the value
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the value is not a set
   */
  @SuppressWarnings("unchecked")
  public <T> Set<T> getAsSet(final String key) {
    Object value = get(key);
    try {
      return (Set<T>)value;
    }
    catch (ClassCastException ex) {
      throw new CantCoerceException(key, value, "Set");
    }
  }
  
  /**
   * Gets the value at the specified key as a short
   * @param key the key of the value
   * @return If the value is a {@link Number}, the value's {@link Number#shortValue()}. Otherwise,
   *         its {@link Object#toString()} is called and the result is parsed as a short.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the key exists, but the value is not a short and can't be
   *         converted to one
   */
  public short getAsShort(final String key) {
    final Object value = getNotNull(key);
    if (value instanceof Number) {
      return ((Number)value).shortValue();
    }
    try {
      return Short.parseShort(value.toString());
    }
    catch (final NumberFormatException e) {
      throw new CantCoerceException(key, value, "short");
    }
  }
  
  /**
   * Gets the value at the specified key as a short
   * @param key the key of the value
   * @param defaultValue the value to return if the key doesn't exist
   * @return If the value is a {@link Number}, the value's {@link Number#shortValue()}. If the value
   *         is not a number, its {@link Object#toString()} is called and the result is parsed as a
   *         short. If the key doesn't exist, defaultValue.
   * @throws AttributeUndefinedException if the key doesn't exist
   * @throws CantCoerceException if the key exists, but the value is not a short and can't be
   *         converted to one
   */
  public short getAsShort(final String key, final short defaultValue) {
    if (!(hasAttribute(key))) {
      return defaultValue;
    }
    else {
      return getAsShort(key);
    }
  }
  
  /**
   * Gets the value at the specified key as a string
   * @param key the key of the value
   * @return the result of the value's {@link Object#toString()}
   * @throws AttributeUndefinedException if the key doesn't exist
   */
  public String getAsString(final String key) {
    final Object attribute = get(key);
    // TODO: Other methods throw AttributeUndefinedException if the value is null. Be consistent.
    return attribute == null ? null : attribute.toString();
  }
  
  /**
   * Gets the raw map of strings to attribute holders
   * @return the map
   */
  public ConcurrentHashMap<String, AttributeHolder> getAttributes() {
    return attributes;
  }
  
  /**
   * Gets the value at the specified key, checking for null
   * @param key the key to find
   * @return the value at the key
   * @throws AttributeUndefinedException if the key doesn't exist or if its value is null
   */
  public Object getNotNull(final String key) {
    // TODO: If the documentation for this class is correct, AttributeMap does not support null
    // values. If that is true, this method should be removed.
    Object result = this.get(key);
    if (result == null) {
      throw new AttributeUndefinedException(key);
    }
    return result;
  }
  
  /**
   * Determines whether a key exists
   * @param key the key to check
   * @return true if it exists, false otherwise
   */
  public boolean hasAttribute(final String key) {
    return attributes.containsKey(key);
  }
  
  /**
   * Puts an attribute into the map.
   * <ul>
   * <li>If the key does not exist, the new value is put.</li>
   * <li>If the key exists, but the related {@link AttributeHolder} is locked (
   * {@link AttributeHolder#isLocked()}), nothing happens</li>
   * <li>If the key exists and the related {@link AttributeHolder} is not locked (
   * {@link AttributeHolder#isLocked()}), the new value is put and the settings of the existing
   * {@link AttributeHolder}, such as {@link AttributeHolder#isSerializable()} and
   * {@link CompositeAttributeHolder#isInheritable()}, are copied to the new holder.</li>
   * </ul>
   * @param key the key of the value to put
   * @param value the value to put
   * @return if the value was put, the new {@link AttributeHolder}. Otherwise, the the
   *         {@link AttributeHolder} of value that already existed for the specified key.
   */
  protected AttributeHolder internalSetAttribute(final String key, final Object value) {
    verifyValidKeyName(key);
    AttributeHolder attributeHolder = attributes.get(key);
    if (attributeHolder == null) {
      attributeHolder = createHolderForValue(value);
      attributes.put(key, attributeHolder);
    }
    else {
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
  
  /**
   * Sets a value. See {@link #internalSetAttribute(String, Object)} for information about whether
   * the value will be set and how it will be set.
   * @param key the key that identifies the value
   * @param value the value to set
   * @return if the new value was put into the map, the {@link AttributeHolder} created to hold it.
   *         If the new value was not put, the {@link AttributeHolder} for the value that already
   *         existed in the map.
   */
  public AttributeHolder set(final String key, final boolean value) {
    return internalSetAttribute(key, Boolean.valueOf(value));
  }
  
  /**
   * Sets a value. See {@link #internalSetAttribute(String, Object)} for information about whether
   * the value will be set and how it will be set.
   * @param key the key that identifies the value
   * @param value the value to set
   * @return if the new value was put into the map, the {@link AttributeHolder} created to hold it.
   *         If the new value was not put, the {@link AttributeHolder} for the value that already
   *         existed in the map.
   */
  public AttributeHolder set(final String key, final byte value) {
    return internalSetAttribute(key, Byte.valueOf(value));
  }
  
  /**
   * Sets a value. See {@link #internalSetAttribute(String, Object)} for information about whether
   * the value will be set and how it will be set.
   * @param key the key that identifies the value
   * @param value the value to set
   * @return if the new value was put into the map, the {@link AttributeHolder} created to hold it.
   *         If the new value was not put, the {@link AttributeHolder} for the value that already
   *         existed in the map.
   */
  public AttributeHolder set(final String key, final char value) {
    return internalSetAttribute(key, Character.valueOf(value));
  }
  
  /**
   * Sets a value. See {@link #internalSetAttribute(String, Object)} for information about whether
   * the value will be set and how it will be set.
   * @param key the key that identifies the value
   * @param value the value to set
   * @return if the new value was put into the map, the {@link AttributeHolder} created to hold it.
   *         If the new value was not put, the {@link AttributeHolder} for the value that already
   *         existed in the map.
   */
  public AttributeHolder set(final String key, final double value) {
    return internalSetAttribute(key, new Double(value));
  }
  
  /**
   * Sets a value. See {@link #internalSetAttribute(String, Object)} for information about whether
   * the value will be set and how it will be set.
   * @param key the key that identifies the value
   * @param value the value to set
   * @return if the new value was put into the map, the {@link AttributeHolder} created to hold it.
   *         If the new value was not put, the {@link AttributeHolder} for the value that already
   *         existed in the map.
   */
  public AttributeHolder set(final String key, final float value) {
    return internalSetAttribute(key, new Float(value));
  }
  
  /**
   * Sets a value. See {@link #internalSetAttribute(String, Object)} for information about whether
   * the value will be set and how it will be set.
   * @param key the key that identifies the value
   * @param value the value to set
   * @return if the new value was put into the map, the {@link AttributeHolder} created to hold it.
   *         If the new value was not put, the {@link AttributeHolder} for the value that already
   *         existed in the map.
   */
  public AttributeHolder set(final String key, final int value) {
    return internalSetAttribute(key, Integer.valueOf(value));
  }
  
  /**
   * Sets a value. See {@link #internalSetAttribute(String, Object)} for information about whether
   * the value will be set and how it will be set.
   * @param key the key that identifies the value
   * @param value the value to set
   * @return if the new value was put into the map, the {@link AttributeHolder} created to hold it.
   *         If the new value was not put, the {@link AttributeHolder} for the value that already
   *         existed in the map.
   */
  public AttributeHolder set(final String key, final long value) {
    return internalSetAttribute(key, Long.valueOf(value));
  }
  
  /**
   * Sets a value into the map
   * @param key the key
   * @param value the value, which should not be an {@link AttributeHolder}
   * @return the {@link AttributeHolder} that was created to hold the specified value
   */
  public AttributeHolder set(final String key, final Object value) {
    return internalSetAttribute(key, value);
  }
  
  /**
   * Sets a value. See {@link #internalSetAttribute(String, Object)} for information about whether
   * the value will be set and how it will be set.
   * @param key the key that identifies the value
   * @param value the value to set
   * @return if the new value was put into the map, the {@link AttributeHolder} created to hold it.
   *         If the new value was not put, the {@link AttributeHolder} for the value that already
   *         existed in the map.
   */
  public AttributeHolder set(final String key, final short value) {
    return internalSetAttribute(key, Short.valueOf(value));
  }
  
  /**
   * Sets zero or more attribute holders from a collection. If an entry value is an
   * {@link AttributeHolder}, it is cloned and its clone is put in this map. If it is not an
   * {@link AttributeHolder}, it is placed in a new attribute holder and put in this map.
   * @param attributes a map of string keys to their holders or not holders. Really, anything.
   */
  public void setAll(final Map<String, ?> attributes) {
    setAllAttributeHolders(attributes);
  }
  
  /**
   * Sets zero or more attribute holders from a collection. If an entry value is an
   * {@link AttributeHolder}, it is cloned and its clone is put in this map. If it is not an
   * {@link AttributeHolder}, it is placed in a new attribute holder and put in this map.
   * @param attributeHolders a map of string keys to their holders or not holders. Really, anything.
   */
  public void setAllAttributeHolders(final Map<String, ?> attributeHolders) {
    // TODO: This method should do what its name implies, not infer what to put in the map based on
    // type
    if (attributeHolders == null) {
      return;
    }
    for (Entry<String, ?> entry : attributeHolders.entrySet()) {
      final String key = entry.getKey();
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
  
  @Override
  public String toString() {
    return getAll().toString();
  }
  
  /**
   * Removes a key and value pair from the map
   * @param key the key to remove
   */
  public void unset(final String key) {
    attributes.remove(key);
  }
  
  private void verifyValidKeyName(final String key) {
    final Matcher matcher = ATTRIBUTE_NAME_PATTERN.matcher(key);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Attribute [" + key
          + "] violates attribute name restriction, attribute not added.");
    }
  }
}
