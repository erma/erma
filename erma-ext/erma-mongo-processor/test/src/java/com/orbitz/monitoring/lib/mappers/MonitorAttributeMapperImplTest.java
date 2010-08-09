package com.orbitz.monitoring.lib.mappers;

import com.orbitz.monitoring.api.mappers.MonitorAttributeMapper;
import com.orbitz.monitoring.api.mappers.ObjectAttributeMapper;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class MonitorAttributeMapperImplTest {

    MonitorAttributeMapper mapper;
    ObjectAttributeMapper identityMapper;

    @Before
    public void setUp()
    throws Exception {

        mapper         = new MonitorAttributeMapperImpl(null);
        identityMapper = new IdentityAttributeMapperImpl();

    }

    @Test
    public void testGeneralObjectAttributeMapper()
    throws Exception {

        EventMonitor monitor = new EventMonitor(null);

        char charPrimitive     = 'a';
        short shortPrimitive   = 5;
        int intPrimitive       = 3423;
        long longPrimitive     = 232323l;
        float floatPrimitive   = 2.345f;
        double doublePrimitive = 34.349908;
        boolean boolPrimitive  = true;
        String stringObj       = RandomStringUtils.randomAlphabetic(10);
        Date dateObj           = new Date();

        monitor.set("charPrimitive", charPrimitive);
        monitor.set("shortPrimitive",shortPrimitive);
        monitor.set("intPrimitive", intPrimitive);
        monitor.set("longPrimitive", longPrimitive);
        monitor.set("floatPrimitive", floatPrimitive);
        monitor.set("doublePrimitive",doublePrimitive);
        monitor.set("boolPrimitive", boolPrimitive);
        monitor.set("stringObj", stringObj);
        monitor.set("dateObj", dateObj);

        Map<String, Object> mapped = mapper.map(monitor);

        assertEquals(charPrimitive, mapped.get("charPrimitive"));
        assertEquals(shortPrimitive, mapped.get("shortPrimitive"));
        assertEquals(intPrimitive, mapped.get("intPrimitive"));
        assertEquals(longPrimitive, mapped.get("longPrimitive"));
        assertEquals(floatPrimitive, mapped.get("floatPrimitive"));
        assertEquals(doublePrimitive, mapped.get("doublePrimitive"));
        assertEquals(boolPrimitive, mapped.get("boolPrimitive"));
        assertEquals(stringObj, mapped.get("stringObj"));
        assertEquals(dateObj, mapped.get("dateObj"));
    }

    @Test
    public void testGeneralObjectAttributeMapperMinusFloatMapper()
    throws Exception {

        EventMonitor monitor = new EventMonitor(null);

        long longPrimitive     = 232323l;
        float floatPrimitive   = 2.345f;
        double doublePrimitive = 34.349908;
        String stringObj       = RandomStringUtils.randomAlphabetic(42);

        monitor.set("longPrimitive", longPrimitive);
        monitor.set("floatPrimitive", floatPrimitive);
        monitor.set("doublePrimitive",doublePrimitive);
        monitor.set("stringObj", stringObj);
        Map<String, ObjectAttributeMapper> mappersMap = new HashMap<String, ObjectAttributeMapper>();
        mappersMap.put("java.lang.Float", null);

        mapper = new MonitorAttributeMapperImpl(mappersMap);

        Map<String, Object> mapped = mapper.map(monitor);

        assertEquals(longPrimitive, mapped.get("longPrimitive"));
        assertEquals(String.format(MonitorAttributeMapperImpl.NO_MAPPER, Float.class.getCanonicalName()), mapped.get("floatPrimitive"));
        assertEquals(doublePrimitive, mapped.get("doublePrimitive"));
        assertEquals(stringObj, mapped.get("stringObj"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testComplexObjectMapAttributeMapper()
    throws Exception {

        Map<String, ObjectAttributeMapper> mappersMap = new HashMap<String, ObjectAttributeMapper>();

        mappersMap.put("com.orbitz.monitoring.lib.mappers.MonitorAttributeMapperImplTest.ComplexObject", new ComplexObjectAttributeMapper());

        mapper = new MonitorAttributeMapperImpl(mappersMap);
        EventMonitor monitor = new EventMonitor(null);

        String key1        = RandomStringUtils.randomAlphabetic(12);
        ComplexObject val1 = ComplexObject.newRandomInstance();

        Map<String, ComplexObject> map = new HashMap<String, ComplexObject>();

        map.put(key1, val1);
        monitor.set("the_map", map);


        Map<String, Object> mapped = mapper.map(monitor);

        assertTrue(mapped.containsKey("the_map"));

        Map<String, Object> theMap = (Map<String, Object>) mapped.get("the_map");

        theMap = (Map<String, Object>) theMap.get(key1);

        assertEquals("id", val1.getId(), theMap.get("id"));
        assertEquals("name", val1.getName(), theMap.get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testComplexObjectAttributeMapper()
    throws Exception {

        Map<String, ObjectAttributeMapper> mappersMap = new HashMap<String, ObjectAttributeMapper>();

        mappersMap.put("com.orbitz.monitoring.lib.mappers.MonitorAttributeMapperImplTest.ComplexObject", new ComplexObjectAttributeMapper());

        mapper = new MonitorAttributeMapperImpl(mappersMap);
        EventMonitor monitor = new EventMonitor(null);

        String key1        = RandomStringUtils.randomAlphabetic(12);
        ComplexObject val1 = ComplexObject.newRandomInstance();

        monitor.set(key1, val1);

        Map<String, Object> mapped = mapper.map(monitor);

        assertTrue(mapped.containsKey(key1));

        Map<String, Object> theMap = (Map<String, Object>) mapped.get(key1);

        assertEquals("id", val1.getId(), theMap.get("id"));
        assertEquals("name", val1.getName(), theMap.get("name"));
    }

    @Test
    public void testNullMonitor() {

        try {
            mapper.map(null);
            fail("should've thrown a IllegalArgumentException");
        } catch (Exception e) {
            assertEquals("expection", e.getClass(), IllegalArgumentException.class);
            /* swallow exception */
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testArrayAttributeMapper() {

        EventMonitor monitor = new EventMonitor(null);
        String[] array       = new String[2];

        array[0] = RandomStringUtils.randomAlphabetic(10);
        array[1] = RandomStringUtils.randomAlphabetic(9);

        monitor.set("the_array", array);

        Map<String, Object> mapped = mapper.map(monitor);

        assertTrue(mapped.containsKey("the_array"));

        List<String> theList = (List<String>) mapped.get("the_array");

        assertEquals("the_array[0]", array[0], theList.get(0));
        assertEquals("the_array[1]", array[1], theList.get(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testComplexObjectCollectionAttributeMapper() {

        EventMonitor monitor        = new EventMonitor(null);
        List<ComplexObject> list    = new ArrayList<ComplexObject>();
        ComplexObject complexObject = ComplexObject.newRandomInstance();

        list.add(complexObject);

        monitor.set("the_list", list);

        Map<String, Object> mapped = mapper.map(monitor);

        assertTrue(mapped.containsKey("the_list"));

        List<ComplexObject> theList = (List<ComplexObject>) mapped.get("the_list");

        assertNotNull("the_list", theList);
        assertEquals("size of list", 1, theList.size());
        assertEquals(String.format(MonitorAttributeMapperImpl.NO_MAPPER, ComplexObject.class.getCanonicalName()), theList.get(0));
    }

    @Test
    public void testIdentityAttributeMapperCollection()
    throws Exception {

        EventMonitor monitor = new EventMonitor(null);

        Collection<Serializable> collection = new ArrayList<Serializable>();

        collection.add(RandomUtils.nextInt());
        collection.add(RandomUtils.nextLong());
        collection.add(RandomStringUtils.randomAlphanumeric(10));

        monitor.set("collection", collection);

        Map<String, Object> map = mapper.map(monitor);

        assertEquals(monitor.get("collection"), map.get("collection"));
    }

    @Test
    public void testIdentityAttributeMapperMap()
    throws Exception {

        EventMonitor monitor = new EventMonitor(null);

        Map<Serializable, Serializable> map = new HashMap<Serializable, Serializable>();

        map.put("key1", RandomUtils.nextInt());
        map.put("key2", RandomUtils.nextLong());
        map.put("key3", RandomStringUtils.randomAlphanumeric(10));

        monitor.set("map", map);

        Map<String, Object> mapped = mapper.map(monitor);

        assertEquals(monitor.get("map"), mapped.get("map"));
    }

    @Test
    public void testListOfMapsAttributeMapper()
    throws Exception {

        EventMonitor monitor = new EventMonitor(null);
        Map<String, ObjectAttributeMapper> mappersMap = new HashMap<String, ObjectAttributeMapper>();

        mappersMap.put("com.orbitz.monitoring.lib.mappers.MonitorAttributeMapperImplTest.ComplexObject", new ComplexObjectAttributeMapper());

        mapper = new MonitorAttributeMapperImpl(mappersMap);

        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();

        Map<String, Object> record1 = new HashMap<String, Object>();
        record1.put("id", RandomUtils.nextInt(5));
        record1.put("name", RandomStringUtils.randomAlphabetic(10));

        Map<String, Object> record2 = new HashMap<String, Object>();
        record2.put("id", RandomUtils.nextInt(5));
        record2.put("name", RandomStringUtils.randomAlphabetic(10));

        Map<String, Object> record3 = new HashMap<String, Object>();
        record3.put("record1_key", record1);
        record3.put("record2_key", "SomeOtherRecord2");

        list.add(record1);
        list.add(record2);
        list.add(record3);

        monitor.set("the_list", list);
        try {
            Map<String, Object> map = mapper.map(monitor);
            fail("IllegalArgumentException was excepted");
        } catch (IllegalArgumentException e) {
            // swallow error
        }
    }

    protected static class ComplexObject {
        private int id;
        private String name;
        public static ComplexObject newRandomInstance() {
            ComplexObject obj = new ComplexObject();

            obj.id   = RandomUtils.nextInt();
            obj.name = RandomStringUtils.randomAlphabetic(10);

            return obj;
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    protected static class ComplexObjectAttributeMapper implements ObjectAttributeMapper {

        public Object map(Object obj) {

            ComplexObject testObj = (ComplexObject) obj;
            Map<String, Object> map = new HashMap<String, Object>();

            map.put("id", testObj.id);
            map.put("name", testObj.name);

            return map;
        }
    }
}
