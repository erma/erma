package com.orbitz.monitoring.lib.processor;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.List;
import java.util.ArrayList;

/**
 * Description of class goes here.
 * <p/>
 * <p>(c) 2000-07 Orbitz, LLC. All Rights Reserved.</p>
 */
public class TestAppender extends AppenderSkeleton {
    private List logEvents = new ArrayList();

    public void append(LoggingEvent event) {
        logEvents.add(event);
    }

    public List getEvents() {
        return logEvents;
    }

    public boolean requiresLayout() {
        return false;
    }

    public void close() {}

    public void reset() {
        logEvents.clear();
    }
}

