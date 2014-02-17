package com.orbitz.monitoring.lib.interceptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.util.PatternMatchUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author Ray Krueger
 */
public class NameMatchMonitoredAttributeSource implements MonitoredAttributeSource {

    private static final Logger log = Logger.getLogger(NameMatchMonitoredAttributeSource.class);

    private Map methodMap = new HashMap();

    public NameMatchMonitoredAttributeSource() {
    }

    public void setNameMap(Map nameMap) {
        Iterator it = nameMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();

            // Check whether we need to convert from String.
            MonitoredAttribute attr;
            if (entry.getValue() instanceof MonitoredAttribute) {
                attr = (MonitoredAttribute) entry.getValue();
                if (StringUtils.trimToNull(attr.getMonitorName()) == null) {
                    attr.setMonitorName(name);
                }
            } else {
                MonitoredAttributeEditor editor = new MonitoredAttributeEditor();
                editor.setAsText(entry.getValue().toString());
                attr = (MonitoredAttribute) editor.getValue();
            }

            addMonitoredMethod(name, attr);
        }
    }

    public void setProperties(Properties props) {
        Validate.notNull(props, "Properties must not be null");
        Iterator iterator = props.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            MonitoredAttributeEditor editor = new MonitoredAttributeEditor();
            editor.setAsText(entry.getValue().toString());
            MonitoredAttribute attr = (MonitoredAttribute) editor.getValue();
            addMonitoredMethod(entry.getKey().toString(), attr);
        }
    }

    public void addMonitoredMethod(String name, MonitoredAttribute attr) {
        if (log.isDebugEnabled()) {
            log.debug("Adding monitored method [" + name + "] with attribute [" + attr + "]");
        }
        this.methodMap.put(name, attr);
    }

    public MonitoredAttribute getMonitoredAttribute(Method method, Class targetClass) {
        Set keys = methodMap.keySet();
        for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            if (PatternMatchUtils.simpleMatch(key, method.getName())) {
                return (MonitoredAttribute) methodMap.get(key);
            }
        }
        return null;
    }
}
