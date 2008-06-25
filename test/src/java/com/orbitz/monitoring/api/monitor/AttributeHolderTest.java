package com.orbitz.monitoring.api.monitor;

import junit.framework.TestCase;

/**
 * Test cases for AttributeHolder
 * <p/>
 * <p>(c) 2000-08 Orbitz, LLC. All Rights Reserved.</p>
 */
public class AttributeHolderTest extends TestCase {

    public void testEquals() {
        AttributeHolder a1 = new AttributeHolder(null);
        AttributeHolder a2 = new AttributeHolder(null);
        assertEquals(a1, a2);

        // metadata doesn't matter when testing for equality
        a2.lock().serializable();
        assertEquals(a1, a2);

        assertFalse(a1.equals("aString"));
        assertFalse(a1.equals(null));

        a1 = new AttributeHolder("a");
        assertFalse(a1.equals(a2));
        a2 = new AttributeHolder("a");
        assertTrue(a1.equals(a2));

        a1 = new AttributeHolder(null);
        assertFalse(a2.equals(a1));

        a1 = new AttributeHolder("b");
        assertFalse(a1.equals(a2));
    }

    public void testToString() {
        AttributeHolder ah = new AttributeHolder("a");
        assertEquals("a", ah.toString());

        ah = new AttributeHolder(null);
        assertEquals("null", ah.toString());
    }

    public void testHashCode() {
        AttributeHolder ah = new AttributeHolder("a");
        assertEquals("a".hashCode(), ah.hashCode());

        ah = new AttributeHolder(null);
        assertEquals("null".hashCode(), ah.hashCode());
    }
}
