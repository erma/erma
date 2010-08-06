package com.orbitz.monitoring.api.mappers;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.orbitz.monitoring.api.Monitor;

/**
 * Maps {@link Monitor} into a {@link Map} object containing
 * {@link String} keys and one of the following values:
 * 1. {@link String}
 * 2. {@link Number}
 * 3. {@link Date}
 * 4. {@link Boolean}
 * 5. {@link Collection}
 * 6. {@link Map}
 * 7. {@link Array}
 * 8. null
 * @author hbouabdallah
 *
 */
public interface MonitorAttributeMapper {

    public Map<String, Object> map(Monitor monitor);
}