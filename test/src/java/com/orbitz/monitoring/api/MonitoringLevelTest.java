package com.orbitz.monitoring.api;

import junit.framework.TestCase;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * MonitoringLevelTest
 */
public class MonitoringLevelTest extends TestCase {
    private static final MonitoringLevel DEBUG = MonitoringLevel.DEBUG;
    private static final MonitoringLevel INFO = MonitoringLevel.INFO;
    private static final MonitoringLevel ESSENTIAL = MonitoringLevel.ESSENTIAL;

    public void testLevel() {
        EventMonitor monitor = new EventMonitor("foo", DEBUG);

        assertEquals(DEBUG, monitor.getLevel());

        TransactionMonitor trans = new TransactionMonitor("bar", INFO);

        assertEquals(INFO, trans.getLevel());

        MonitoringEngine.getInstance().addMonitorLevel("foo", MonitoringLevel.ESSENTIAL);
        assertEquals(ESSENTIAL, monitor.getLevel());
    }

    public void testLevelToStr() {
        String levelStr = "DEBUG";
        MonitoringLevel debug = MonitoringLevel.toLevel(levelStr);

        assertEquals("Level should be assigned thru levelStr", levelStr, debug.toString());

        String levelStr2 = "INFO";
        MonitoringLevel info = MonitoringLevel.toLevel(levelStr2);

        assertEquals("Level should be assigned thru levelStr", levelStr2, info.toString());

        String levelStr3 = "ESSENTIAL";
        MonitoringLevel essential = MonitoringLevel.toLevel(levelStr3);

        assertEquals("Level should be assigned thru levelStr", levelStr3, essential.toString());
    }

    public void testIsValidLevelStr() {
        String validLevelStr = "essential";

        String validLevelStr2 = "info";

        String validLevelStr3 = "debug";

        String invalidLevelStr = "foo";

        assertTrue("Essential should be recognized as a valid level string",
                MonitoringLevel.isValidLevelStr(validLevelStr));
        assertTrue("Essential should be recognized as a valid level string",
                MonitoringLevel.isValidLevelStr(validLevelStr2));
        assertTrue("Essential should be recognized as a valid level string",
                MonitoringLevel.isValidLevelStr(validLevelStr3));
        
        assertFalse("Foo should be an invalid level string", MonitoringLevel.isValidLevelStr(invalidLevelStr));
    }

    public void testToString() {
        String levelStr = "DEBUG";

        assertEquals("ToString should return the levelStr", levelStr, DEBUG.toString());
    }

    public void testHasHigherPriorityThan() {
        assertFalse("debug is less than info", DEBUG.hasHigherPriorityThan(INFO));
        assertTrue("info is not less than debug", INFO.hasHigherPriorityThan(DEBUG));

        assertFalse("info is less than essential", INFO.hasHigherPriorityThan(ESSENTIAL));
        assertTrue("essential is not less than info", ESSENTIAL.hasHigherPriorityThan(INFO));

        assertFalse("debug is less than essential", DEBUG.hasHigherPriorityThan(ESSENTIAL));
        assertTrue("essential is not less than debug", ESSENTIAL.hasHigherPriorityThan(DEBUG));        
    }

    public void testHasHigherOrEqualPriorityThan() {
        assertFalse("debug is less than info", DEBUG.hasHigherOrEqualPriorityThan(INFO));
        assertTrue("info is not less than debug", INFO.hasHigherOrEqualPriorityThan(DEBUG));

        assertFalse("info is less than essential", INFO.hasHigherOrEqualPriorityThan(ESSENTIAL));
        assertTrue("essential is not less than info", ESSENTIAL.hasHigherOrEqualPriorityThan(INFO));

        assertFalse("debug is less than essential", DEBUG.hasHigherOrEqualPriorityThan(ESSENTIAL));
        assertTrue("essential is not less than debug", ESSENTIAL.hasHigherOrEqualPriorityThan(DEBUG));

        assertTrue("A level should equal itself", DEBUG.hasHigherOrEqualPriorityThan(DEBUG));
        assertTrue("A level should equal itself", INFO.hasHigherOrEqualPriorityThan(INFO));
        assertTrue("A level should equal itself", ESSENTIAL.hasHigherOrEqualPriorityThan(ESSENTIAL));
    }

    public void testEquals() {
        assertTrue(MonitoringLevel.DEBUG.equals(MonitoringLevel.DEBUG));
        assertTrue(MonitoringLevel.INFO.equals(MonitoringLevel.INFO));
        assertTrue(MonitoringLevel.ESSENTIAL.equals(MonitoringLevel.ESSENTIAL));

        assertFalse(MonitoringLevel.DEBUG.equals(MonitoringLevel.INFO));

        assertFalse(MonitoringLevel.INFO.equals(null));
    }

    public void testHashCode() {
        MonitoringLevel.INFO.hashCode();
    }
}
