package com.orbitz.monitoring.lib.interceptor;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * Parses a string into a MonitoredAttribute.
 * Understands the following formats:
 * <ul>
 * <li>MONITOR_NAME=MyMonitor</li>
 * <li>MONITOR_NAME=MyMonitor, INCLUDE_RESULT</li>
 * <li>MONITOR_NAME=MyMonitor, INCLUDE_ARGUMENTS</li>
 * <li>MONITOR_NAME=MyMonitor, INCLUDE_RESULT, INCLUDE_ARGUMENTS</li>
 * </ul>
 *
 * @author Ray Krueger
 */
public class MonitoredAttributeEditor extends PropertyEditorSupport {

  public static final String MONITOR_NAME_PREFIX = "MONITOR_NAME=";
  public static final String INCLUDE_RESULT = "INCLUDE_RESULT";
  public static final String INCLUDE_ARGUMENTS = "INCLUDE_ARGUMENTS";

  public void setAsText(String s) throws IllegalArgumentException {
    if (s == null || "".equals(s)) {
      setValue(null);
    } else {
      // tokenize it with ","
      String[] tokens = StringUtils.commaDelimitedListToStringArray(s);
      MonitoredAttribute attr = new MonitoredAttribute();

      for (int i = 0; i < tokens.length; i++) {
        String token = tokens[i].trim().replace(' ', '_');

        if (token.startsWith(MONITOR_NAME_PREFIX)) {
          attr.setMonitorName(token.substring(MONITOR_NAME_PREFIX.length()));

        } else if (token.startsWith(INCLUDE_RESULT)) {
          attr.setIncludeResult(true);

        } else if (token.startsWith(INCLUDE_ARGUMENTS)) {
          attr.setIncludeArguments(true);

        } else {
          throw new IllegalArgumentException("Illegal monitor attribute token: [" + token + "]");
        }
      }

      setValue(attr);
    }
  }

}
