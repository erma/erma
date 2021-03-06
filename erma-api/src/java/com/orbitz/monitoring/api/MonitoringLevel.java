package com.orbitz.monitoring.api;

import java.io.Serializable;

/**
 * MonitoringLevel
 */
public final class MonitoringLevel implements Serializable {

  /** DEBUG - Level with lowest processing priority */
  public static final MonitoringLevel DEBUG = new MonitoringLevel("DEBUG", 300);
  /** INFO - Default level for Monitors */
  public static final MonitoringLevel INFO = new MonitoringLevel("INFO", 200);
  /** ESSENTIAL - Level with highest processing priority */
  public static final MonitoringLevel ESSENTIAL = new MonitoringLevel("ESSENTIAL", 100);

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("checkstyle:membername")
  private String _levelStr;
  @SuppressWarnings("checkstyle:membername")
  private int _level;

  private MonitoringLevel(String levelStr, int level) {
    _levelStr = levelStr;
    _level = level;
  }

  /**
   * Given a string representing a level, return the appropriate MonitoringLevel.
   * If the string provided does not match a level, INFO will be returned as the default
   *
   * @param levelStr representing the level desired
   * @return a MonitoringLevel matching the string provided, or INFO if no match was found
   */
  public static MonitoringLevel toLevel(String levelStr) {
    if (ESSENTIAL._levelStr.equalsIgnoreCase(levelStr)) {
      return MonitoringLevel.ESSENTIAL;
    } else if (DEBUG._levelStr.equalsIgnoreCase(levelStr)) {
      return MonitoringLevel.DEBUG;
    } else {
      return MonitoringLevel.INFO;
    }
  }

  /**
   * Given a string representing a level, return whether or not that string matches a 
   * MonitoringLevel
   * @param levelStr represents a level
   * @return true if the string corresponds to a MonitoringLevel, otherwise false
   */
  public static boolean isValidLevelStr(String levelStr) {
    return ESSENTIAL._levelStr.equalsIgnoreCase(levelStr) 
        || INFO._levelStr.equalsIgnoreCase(levelStr)
        || DEBUG._levelStr.equalsIgnoreCase(levelStr);
  }

  public boolean hasHigherPriorityThan(MonitoringLevel monitoringLevel) {
    return _level < monitoringLevel._level;
  }

  public boolean hasHigherOrEqualPriorityThan(MonitoringLevel monitoringLevel) {
    return _level <= monitoringLevel._level;
  }

  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }

    MonitoringLevel that = (MonitoringLevel) o;

    if (_level != that._level) { return false; }

    return true;
  }

  public int hashCode() {
    return _level;
  }

  public String toString() {
    return _levelStr;
  }
}
