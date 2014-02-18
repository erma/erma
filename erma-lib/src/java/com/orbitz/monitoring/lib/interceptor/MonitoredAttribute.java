package com.orbitz.monitoring.lib.interceptor;

/**
 * @author Ray Krueger
 */
public class MonitoredAttribute {

    private String monitorName;
    private String levelStr;
    private boolean includeResult;
    private boolean includeArguments;

    public MonitoredAttribute() {
    }

    public MonitoredAttribute(String monitorName) {
        this(monitorName, false, false);
    }

    public MonitoredAttribute(boolean includeResult, boolean includeArguments) {
        this.includeResult = includeResult;
        this.includeArguments = includeArguments;
    }

    public MonitoredAttribute(String monitorName, boolean includeResult, boolean includeArguments) {
        this.monitorName = monitorName;
        this.includeResult = includeResult;
        this.includeArguments = includeArguments;
    }

    public MonitoredAttribute(final String monitorName, final String levelStr, final boolean includeResult, final boolean includeArguments) {
        this.monitorName = monitorName;
        this.levelStr = levelStr;
        this.includeResult = includeResult;
        this.includeArguments = includeArguments;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getLevelStr() {
        return levelStr;
    }

    public void setLevelStr(final String levelStr) {
        this.levelStr = levelStr;
    }

    public boolean isIncludeResult() {
        return includeResult;
    }

    public void setIncludeResult(boolean includeResult) {
        this.includeResult = includeResult;
    }

    public boolean isIncludeArguments() {
        return includeArguments;
    }

    public void setIncludeArguments(boolean includeArguments) {
        this.includeArguments = includeArguments;
    }

    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}

        final MonitoredAttribute that = (MonitoredAttribute) o;

        if (includeArguments != that.includeArguments) {return false;}
        if (includeResult != that.includeResult) {return false;}
        if (monitorName != null ? !monitorName.equals(that.monitorName) : that.monitorName != null) {return false;}

        return true;
    }

    public int hashCode() {
        int result;
        result = (monitorName != null ? monitorName.hashCode() : 0);
        result = 29 * result + (includeResult ? 1 : 0);
        result = 29 * result + (includeArguments ? 1 : 0);
        return result;
    }
}
