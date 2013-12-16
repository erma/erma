package com.orbitz.monitoring.lib.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;

/**
 * Unit test for XmlMonitorRenderer.
 * 
 * <p>
 * (c) 2000-07 Orbitz, LLC. All Rights Reserved.
 * </p>
 */
public class XmlMonitorRendererTest extends TestCase {
    
    private XmlMonitorRenderer defaultRenderer;
    
    @Override
    public void setUp() {
        defaultRenderer = new XmlMonitorRenderer(Collections.singletonList("name"));
    }
    
    public void testSimpleRender() {
        final TransactionMonitor monitor = new TransactionMonitor(this.getClass().getName() + "."
                + "testSimpleRender");
        assertEquals("<TransactionMonitor name=\""
                + "com.orbitz.monitoring.lib.renderer.XmlMonitorRendererTest.testSimpleRender\"/>",
                defaultRenderer.renderMonitor(monitor));
    }
    
    public void testRenderNullMonitor() {
        assertEquals("<null/>", defaultRenderer.renderMonitor(null));
    }
    
    public void testRenderNullMonitorAttributes() {
        final TransactionMonitor monitor = new TransactionMonitor(null);
        assertEquals("<TransactionMonitor name=\"null\"/>", defaultRenderer.renderMonitor(monitor));
    }
    
    public void testRenderNonTransactionMonitors() {
        final EventMonitor monitor = new EventMonitor("event");
        assertEquals("<EventMonitor name=\"event\"/>", defaultRenderer.renderMonitor(monitor));
        
        final MyTransactionMonitor myTM = new MyTransactionMonitor("myTM");
        assertEquals("<MyTransactionMonitor name=\"myTM\"/>", defaultRenderer.renderMonitor(myTM));
    }
    
    public void testConfigurableAttributes() {
        final TransactionMonitor monitor = new TransactionMonitor("name");
        monitor.set("foo", "foo");
        monitor.set("bar", "bar");
        monitor.set("someInteger", 5);
        monitor.set("someBoolean", true);
        monitor.set("someFloat", 1.0);
        
        final List allowedAttributes = new ArrayList();
        XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        assertEquals("<TransactionMonitor/>", renderer.renderMonitor(monitor));
        
        allowedAttributes.add("foo");
        allowedAttributes.add("someInteger");
        renderer = new XmlMonitorRenderer(allowedAttributes);
        assertEquals("<TransactionMonitor foo=\"foo\" someInteger=\"5\"/>",
                renderer.renderMonitor(monitor));
        
        allowedAttributes.add("someBoolean");
        allowedAttributes.add("someFloat");
        renderer = new XmlMonitorRenderer(allowedAttributes);
        final String actual = renderer.renderMonitor(monitor);
        assertEquals("<TransactionMonitor foo=\"foo\" someInteger=\"5\" someBoolean=\"true\""
                + " someFloat=\"1.0\"/>", actual);
    }
    
    public void testCompactPrintByDefault() {
        final TransactionMonitor monitor = new TransactionMonitor("parent");
        monitor.set("foo", "foo");
        monitor.set("bar", "bar");
        
        final TransactionMonitor child1 = new TransactionMonitor("child1");
        monitor.addChildMonitor(child1);
        
        final EventMonitor child2 = new EventMonitor("child2");
        monitor.addChildMonitor(child2);
        
        final List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("foo");
        allowedAttributes.add("bar");
        final XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        final String xml = renderer.renderMonitor(monitor);
        
        assertEquals("Compact xml should not contain tabs", -1, xml.indexOf("\t"));
        assertEquals("Compact xml should not contain newlines", -1, xml.indexOf("\n"));
        assertFalse("No whitespace should be between elements", xml.matches(".*>[\\s]+<.*"));
    }
    
    public void testPrettyPrint() {
        final List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("foo");
        allowedAttributes.add("bar");
        final XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        renderer.setPrettyPrint(true);
        assertTrue(renderer.isPrettyPrint());
        
        final Map extraAttributes = new HashMap();
        extraAttributes.put("foo", "1");
        extraAttributes.put("bar", "2");
        
        final TransactionMonitor httpIn = new TransactionMonitor("httpIn", extraAttributes);
        httpIn.addChildMonitor(new EventMonitor("webflowEvent", extraAttributes));
        httpIn.addChildMonitor(new EventMonitor("webflowEvent2", extraAttributes));
        final TransactionMonitor action = new TransactionMonitor("com.orbitz.foo.AirSearchAction");
        httpIn.addChildMonitor(action);
        action.addChildMonitor(new TransactionMonitor("jiniOut", extraAttributes));
        httpIn.addChildMonitor(new EventMonitor("lastEvent", extraAttributes));
        
        final String expectedXml = "<TransactionMonitor name=\"httpIn\" foo=\"1\" bar=\"2\">\n"
                + "  <EventMonitor name=\"webflowEvent\" foo=\"1\" bar=\"2\"/>\n"
                + "  <EventMonitor name=\"webflowEvent2\" foo=\"1\" bar=\"2\"/>\n"
                + "  <TransactionMonitor name=\"com.orbitz.foo.AirSearchAction\">\n"
                + "    <TransactionMonitor name=\"jiniOut\" foo=\"1\" bar=\"2\"/>\n"
                + "  </TransactionMonitor>\n"
                + "  <EventMonitor name=\"lastEvent\" foo=\"1\" bar=\"2\"/>\n"
                + "</TransactionMonitor>";
        
        final String xml = renderer.renderMonitor(httpIn);
        assertEquals(expectedXml, xml);
    }
    
    public void testRenderWithSizeLimit() {
        final List attributeList = new ArrayList();
        attributeList.add("name");
        attributeList.add("monsterAttr");
        final XmlMonitorRenderer renderer = new XmlMonitorRenderer(attributeList);
        renderer.setMaxCharacters(128);
        
        final TransactionMonitor monitor = new TransactionMonitor("parent");
        final TransactionMonitor child = new TransactionMonitor("child");
        child.set("monsterAttr",
                "blah blah blah blah blah blah blah blah blah blah blah blah blah blah");
        monitor.addChildMonitor(child);
        
        final String xml = renderer.renderMonitor(monitor);
        assertEquals("<TransactionMonitor name=\"parent\">\n"
                + "  Error rendering event pattern: length is 173, max length is 128\n"
                + "</TransactionMonitor>", xml);
    }
    
    public void testRenderWithCycleInObjectGraph() {
        final List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("failureThrowable");
        final XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        
        // this should never happen in real code
        final Exception e1 = new Exception("foo");
        final Exception e2 = new IllegalStateException("bar", e1);
        e1.initCause(e2);
        
        final TransactionMonitor monitor = new TransactionMonitor("name");
        monitor.failedDueTo(e1);
        
        final String xml = renderer.renderMonitor(monitor);
        assertEquals(
                "<TransactionMonitor name=\"name\" failureThrowable=\"java.lang.Exception: foo\"/>",
                xml);
    }
    
    public void testRenderWithDynaBeanException() {
        final List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("failureThrowable");
        final XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        
        final Exception e = new Exception("foo") {
            @Override
            public String getMessage() {
                return super.getMessage()
                        + "\n  stack trace element 1\n  stack trace element 2\n  stack trace element 3\n";
            }
        };
        
        final TransactionMonitor monitor = new TransactionMonitor("name");
        monitor.failedDueTo(e);
        monitor.done();
        
        String xml = renderer.renderMonitor(monitor);
        assertEquals(
                "<TransactionMonitor name=\"name\" failureThrowable=\"com.orbitz.monitoring.lib.renderer.XmlMonitorRendererTest$1: foo\"/>",
                xml);
    }
    
    public void testRenderWithInvalidXmlCharacters() {
        final List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("crap");
        final XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        renderer.setMaxCharacters(256);
        
        final TransactionMonitor monitor = new TransactionMonitor("<name>&");
        monitor.set("crap", "<>&\"");
        assertEquals("<TransactionMonitor name=\"name\" crap=\"&lt;&gt;&amp;&quot;\"/>",
                renderer.renderMonitor(monitor));
    }
}

class MyTransactionMonitor extends TransactionMonitor {
    public MyTransactionMonitor(final String name) {
        super(name);
    }
};
