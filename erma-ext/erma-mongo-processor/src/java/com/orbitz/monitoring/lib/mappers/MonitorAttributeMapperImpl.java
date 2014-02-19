package com.orbitz.monitoring.lib.mappers;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.mappers.MonitorAttributeMapper;
import com.orbitz.monitoring.api.mappers.ObjectAttributeMapper;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * By default, this class maps {@link String}, {@link Number},
 * {@link java.util.Date}, {@link Boolean}, {@link Collection}, {@link Map} and {@link java.lang.reflect.Array}.
 * The mapper also accepts custom {@link ObjectAttributeMapper}.
 * Currently does not support nested {@link Collection}, {@link Map} and {@link java.lang.reflect.Array}.
 * @author hbouabdallah
 *
 */
public class MonitorAttributeMapperImpl implements MonitorAttributeMapper {

  protected static final String NO_MAPPER = "<Unable to find Mapper for [%s]>";
  private static final Logger log     = Logger.getLogger(MonitorAttributeMapperImpl.class);

  private Map<String, ObjectAttributeMapper> mappers;

  public MonitorAttributeMapperImpl(Map<String, ObjectAttributeMapper> mappers) {

    super();
    this.mappers = new HashMap<String, ObjectAttributeMapper>();
    ObjectAttributeMapper identityAttributeMapper = new IdentityAttributeMapperImpl();

    this.mappers.put("java.lang.Character", identityAttributeMapper);
    this.mappers.put("java.lang.Short", identityAttributeMapper);
    this.mappers.put("java.lang.Integer", identityAttributeMapper);
    this.mappers.put("java.lang.Long", identityAttributeMapper);
    this.mappers.put("java.lang.Float", identityAttributeMapper);
    this.mappers.put("java.lang.Double", identityAttributeMapper);
    this.mappers.put("java.lang.Boolean", identityAttributeMapper);
    this.mappers.put("java.lang.String", identityAttributeMapper);
    this.mappers.put("java.util.Date", identityAttributeMapper);

    if (null != mappers) {
      this.mappers.putAll(mappers);
    }
  }

  /**
   * Maps {@link Monitor} using Spring injected mapper(s)
   * @param map
   * @param attrName
   * @param attrObj
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> map(Monitor monitor) {

    // step one: check if monitor or monitor internal map are null
    Map<String, Object> monitorMap = (null == monitor) ? null : monitor.getAll();

    if (null == monitorMap) {
      throw new IllegalArgumentException("Monitor cannot be null");
    }

    // step two: map objects
    Map<String, Object> mappedMap = new HashMap<String, Object>();

    for (Map.Entry<String, Object> entry : monitorMap.entrySet()) {
      String attrName = entry.getKey();
      Object attrObj  = entry.getValue();

      Object mappedObject = null;

      if (null == attrObj) {
        mappedObject = null;

      } else if (attrObj instanceof Collection) {
        mappedObject = handleCollection((Collection<Object>)attrObj);

      } else if (attrObj instanceof Map) {
        mappedObject = handleMap((Map<Object, Object>)attrObj);

      } else if (attrObj.getClass().isArray()) {
        mappedObject = handleArray((Object[]) attrObj);

      } else {
        mappedObject = handleObject(attrObj);
      }

      mappedMap.put(attrName, mappedObject);

    }

    return mappedMap;
  }

  /**
   * Maps array using Spring injected mapper(s)
   * @param attrArray
   * @return
   */
  private Object handleArray(Object[] attrArray) {
    Collection<Object> mappedColl = new ArrayList<Object>();

    for (Object obj : attrArray) {
      mappedColl.add(handleObject(obj));
    }

    return mappedColl;
  }

  /**
   * Maps {@link Collection} using Spring injected mapper(s)
   * @param attrColl
   */
  private Object handleCollection(Collection<Object> attrColl) {

    Collection<Object> mappedColl = new ArrayList<Object>();

    for (Object obj : attrColl) {
      mappedColl.add(handleObject(obj));
    }

    return mappedColl;
  }

  /**
   * Maps {@link Object} using Spring injected mapper(s)
   * @param attrObj
   */
  @SuppressWarnings("unchecked")
  private Object handleObject(Object attrObj) {

    if (null == attrObj) {
      return null;
    }

    if ((attrObj instanceof Map)
        || (attrObj instanceof Collection)
        || (attrObj.getClass().isArray())) {
      throw new IllegalArgumentException(
          String.format("Cannot map the following object [%s] with class [%s]", attrObj, attrObj.getClass()));
    }

    String attrClassName     = attrObj.getClass().getCanonicalName();
    ObjectAttributeMapper mapper = mappers.get(attrClassName);

    if (null == mapper) {
      return String.format(NO_MAPPER, attrClassName);
    }

    return mapper.map(attrObj);
  }

  /**
   * Maps {@link Map} using Spring injected mapper(s)
   * @param map
   * @param attrMap
   */
  private Object handleMap(Map<Object, Object> attrMap) {

    Map<String, Object> mappedMap = new HashMap<String, Object>();

    for (Entry<Object, Object> entry : attrMap.entrySet()) {
      String key = String.valueOf(entry.getKey());
      Object obj = entry.getValue();

      mappedMap.put(key, handleObject(obj));
    }

    return mappedMap;
  }
}
