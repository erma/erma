package com.orbitz.monitoring.lib.factory;

import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.MonitorProcessor;
import com.orbitz.monitoring.api.MonitoringLevel;
import com.orbitz.monitoring.api.MonitoringEngine;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.List;
import java.util.ArrayList;

/**
 * An object that contains the configuration for what processors should be
 * called for which monitors.
 *
 * @@org.springframework.jmx.export.metadata.ManagedResource
 * (description="ProcessGroup can be enabled/disabled and MonitoringLevel adjusted")
 */
@ManagedResource(description="ProcessGroup can be enabled/disabled and MonitoringLevel adjusted")
public class ProcessGroup {
    private static final Logger log = Logger.getLogger(ProcessGroup.class);

    private MonitoringLevel _monitoringLevel = MonitoringLevel.INFO;

    private boolean _active = true;
    private Expression _appliesExpression;
    private MonitorProcessor[] _processors;

    public ProcessGroup(MonitorProcessor processor) {
        this(new MonitorProcessor[] { processor });
    }

    public ProcessGroup(MonitorProcessor[] processors) {
        _processors = processors;
    }

    /**
     * Returns the list of processors within this ProcessGroup that apply for the
     * MonitoringLevel of the given monitor.
     *
     * @param monitor Monitor instance ready for processing
     * @return list of MonitorProcessors
     */
    public List getProcessorsFor(Monitor monitor) {
        List processorsForMonitor = new ArrayList();

        if (! isActive()) {
            return processorsForMonitor;
        }

        // check level of monitor against processors and group
        MonitoringLevel monitorLevel = monitor.getLevel();

        for (int i=0; i<_processors.length; i++) {
            String processorName = _processors[i].getName();
            MonitoringLevel monitorProcessorLevel =
                    MonitoringEngine.getInstance().getProcessorLevel(processorName);
            boolean levelApplies = true;

            if (monitorProcessorLevel != null) {
                // a MonitorProcessor level overrides that of the ProcessGroup if set
                levelApplies = monitorLevel.hasHigherOrEqualPriorityThan(monitorProcessorLevel);
            } else {
                // apply the level of the ProcessGroup
                levelApplies = monitorLevel.hasHigherOrEqualPriorityThan(_monitoringLevel);
            }

            if (levelApplies) {
                processorsForMonitor.add(_processors[i]);
            }
        }

        // for performance reasons evaluate the Jexl expression for the group only
        // after verifying at least one processor passes the level check
        if (processorsForMonitor.size() > 0) {
            if (! matchesExpressionFor(monitor)) {
                processorsForMonitor.clear();
            }
        }

        return processorsForMonitor;
    }

    /**
     * appliesTo will determine if this ProcessGroup should have the monitor.
     * @param monitor being processed
     * @return true if the monitor will be handled by this process group, else false
     */
    private boolean matchesExpressionFor(Monitor monitor) {
        boolean applies = true;

        if (_appliesExpression != null) {
            JexlContext context = JexlHelper.createContext();
            context.getVars().put("m", monitor);
            context.getVars().putAll(monitor.getAll());
            try {
                Object result = _appliesExpression.evaluate(context);
                if (result != null && result instanceof Boolean) {
                    Boolean expressionResult = (Boolean) result;
                    applies = expressionResult.booleanValue();
                } else {
                    applies = false;
                }
            } catch (Exception e) {
                log.debug("Exception while applying expression: ", e);
                applies = false;
            }
        }

        return applies;
    }

    public void setExpression(String expressionString) {
        Expression expression = null;

        if (expressionString != null) {
            try {
                expression = ExpressionFactory.createExpression(expressionString);
            } catch (Exception e) {
                log.error("Error setting expression: ", e);
            }
        }

        _appliesExpression = expression;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute
     * (description="Returns true if this process group is enabled")
     */
    @ManagedAttribute(description="Returns true if this process group is enabled")
    public boolean isActive() {
        return _active;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute
     * (description="Set to true/false to activate/deactivate the process group")
     * @@org.springframework.jmx.export.metadata.ManagedOperationParameter
     * (index=0, name="active", description="boolean value for activating the process group")
     */
    @ManagedAttribute(description="Set to true/false to activate/deactivate the process group")
    public void setActive(boolean active) {
        _active = active;

        log.info(this.toString() + (active ? " activated" : " deactivated"));
    }

    public MonitorProcessor[] getProcessors() {
        return _processors;
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute
     * (description="get the string representation of the monitoring level for this process group")
     */
    @ManagedAttribute(description="get the string representation of the monitoring level for this process group")
    public String getMonitoringLevel() {
        return _monitoringLevel.toString();
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedOperation
     * (description="Set the monitoring level for this process group")
     * @@org.springframework.jmx.export.metadata.ManagedOperationParameter
     * (index=0, name="levelString", description="new MonitoringLevel to apply")
     */
    @ManagedOperation(description="Set the monitoring level for this process group")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name="levelString", description="new MonitoringLevel to apply")})
    public void updateMonitoringLevel(final String levelString) {
        if (! MonitoringLevel.isValidLevelStr(levelString)) {
            throw new IllegalArgumentException("levelString must match an existing MonitoringLevel");
        }

        _monitoringLevel = MonitoringLevel.toLevel(levelString);

        log.info(this.toString() + " -> " + levelString);
    }

    /**
     * Used for spring wiring, just wraps the above runtime method
     * @param levelString new MonitoringLevel to apply
     */
    public void setMonitoringLevel(final String levelString) {
        updateMonitoringLevel(levelString);
    }
}
