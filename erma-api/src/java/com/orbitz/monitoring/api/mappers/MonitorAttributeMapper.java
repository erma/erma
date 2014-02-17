package com.orbitz.monitoring.api.mappers;

import java.util.Map;

import com.orbitz.monitoring.api.Monitor;

/**
 * Maps {@link Monitor} into a {@link Map} object containing
 * {@link String} keys and one of the following values:
 * 1. {@link String}
 * 2. {@link Number}
 * 3. {@link java.util.Date}
 * 4. {@link Boolean}
 * 5. {@link java.util.Collection}
 * 6. {@link java.util.Map}
 * 7. {@link java.lang.reflect.Array}
 * 8. null
 * @author hbouabdallah
 *
 */
public interface MonitorAttributeMapper {

    public Map<String, Object> map(Monitor monitor);
}
