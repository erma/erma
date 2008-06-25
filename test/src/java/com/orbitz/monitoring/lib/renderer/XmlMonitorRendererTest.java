package com.orbitz.monitoring.lib.renderer;

import com.orbitz.monitoring.api.MonitoringEngine;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.api.monitor.serializable.SerializableCompositeMonitor;
import com.orbitz.monitoring.lib.decomposer.AttributeDecomposer;
import com.orbitz.monitoring.test.MockDecomposer;
import com.orbitz.monitoring.test.MockMonitorProcessorFactory;
import com.orbitz.monitoring.test.MockMonitorProcessor;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for XmlMonitorRenderer.
 *
 * <p>(c) 2000-07 Orbitz, LLC. All Rights Reserved.</p>
 */
public class XmlMonitorRendererTest extends TestCase {

    private XmlMonitorRenderer defaultRenderer;

    public void setUp() {
        List attributeList = new ArrayList();
        attributeList.add("name");
        defaultRenderer = new XmlMonitorRenderer(attributeList);
    }

    public void testSimpleRender() {
        TransactionMonitor monitor = new TransactionMonitor(this.getClass().getName() + "." +
                "testSimpleRender");
        assertEquals("<TransactionMonitor name=\"" +
                "com.orbitz.monitoring.lib.renderer.XmlMonitorRendererTest.testSimpleRender\"/>",
                defaultRenderer.renderMonitor(monitor));
    }
                                                                
    public void testRenderNullMonitor() {
        assertEquals("<null/>", defaultRenderer.renderMonitor(null));
    }

    public void testRenderNullMonitorAttributes() {
        TransactionMonitor monitor = new TransactionMonitor(null);
        assertEquals("<TransactionMonitor name=\"null\"/>",
                defaultRenderer.renderMonitor(monitor));
    }

    public void testRenderNonTransactionMonitors() {
        EventMonitor monitor = new EventMonitor("event");
        assertEquals("<EventMonitor name=\"event\"/>",
                defaultRenderer.renderMonitor(monitor));

        MyTransactionMonitor myTM = new MyTransactionMonitor("myTM");
        assertEquals("<MyTransactionMonitor name=\"myTM\"/>",
                defaultRenderer.renderMonitor(myTM));
    }

    public void testConfigurableAttributes() {
        TransactionMonitor monitor = new TransactionMonitor("name");
        monitor.set("foo", "foo");
        monitor.set("bar", "bar");
        monitor.set("someInteger", 5);
        monitor.set("someBoolean", true);
        monitor.set("someFloat", 1.0);

        List allowedAttributes = new ArrayList();
        XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        assertEquals("<TransactionMonitor/>",
                renderer.renderMonitor(monitor));

        allowedAttributes.add("foo");
        allowedAttributes.add("someInteger");
        renderer = new XmlMonitorRenderer(allowedAttributes);
        assertEquals("<TransactionMonitor foo=\"foo\" someInteger=\"5\"/>",
                renderer.renderMonitor(monitor));

        allowedAttributes.add("someBoolean");
        allowedAttributes.add("someFloat");
        renderer = new XmlMonitorRenderer(allowedAttributes);
        String actual = renderer.renderMonitor(monitor);
        assertEquals("<TransactionMonitor foo=\"foo\" someInteger=\"5\" someBoolean=\"true\"" +
                " someFloat=\"1.0\"/>", actual);
    }

    public void testCompactPrintByDefault() {
        TransactionMonitor monitor = new TransactionMonitor("parent");
        monitor.set("foo", "foo");
        monitor.set("bar", "bar");

        TransactionMonitor child1 = new TransactionMonitor("child1");
        monitor.addChildMonitor(child1);

        EventMonitor child2 = new EventMonitor("child2");
        monitor.addChildMonitor(child2);

        List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("foo");
        allowedAttributes.add("bar");
        XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        String xml = renderer.renderMonitor(monitor);

        assertEquals("Compact xml should not contain tabs", -1, xml.indexOf("\t"));
        assertEquals("Compact xml should not contain newlines", -1, xml.indexOf("\n"));
        assertFalse("No whitespace should be between elements", xml.matches(".*>[\\s]+<.*"));
    }

    public void testPrettyPrint() {
        List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("foo");
        allowedAttributes.add("bar");
        XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        renderer.setPrettyPrint(true);
        assertTrue(renderer.isPrettyPrint());

        Map extraAttributes = new HashMap();
        extraAttributes.put("foo", "1");
        extraAttributes.put("bar", "2");

        TransactionMonitor httpIn = new TransactionMonitor("httpIn", extraAttributes);
        httpIn.addChildMonitor(new EventMonitor("webflowEvent", extraAttributes));
        httpIn.addChildMonitor(new EventMonitor("webflowEvent2", extraAttributes));
        TransactionMonitor action = new TransactionMonitor("com.orbitz.foo.AirSearchAction");
        httpIn.addChildMonitor(action);
        action.addChildMonitor(new TransactionMonitor("jiniOut", extraAttributes));
        httpIn.addChildMonitor(new EventMonitor("lastEvent", extraAttributes));

        String expectedXml =
                "<TransactionMonitor name=\"httpIn\" foo=\"1\" bar=\"2\">\n" +
                "  <EventMonitor name=\"webflowEvent\" foo=\"1\" bar=\"2\"/>\n" +
                "  <EventMonitor name=\"webflowEvent2\" foo=\"1\" bar=\"2\"/>\n" +
                "  <TransactionMonitor name=\"com.orbitz.foo.AirSearchAction\">\n" +
                "    <TransactionMonitor name=\"jiniOut\" foo=\"1\" bar=\"2\"/>\n" +
                "  </TransactionMonitor>\n" +
                "  <EventMonitor name=\"lastEvent\" foo=\"1\" bar=\"2\"/>\n" +
                "</TransactionMonitor>";

        String xml = renderer.renderMonitor(httpIn);
        assertEquals(expectedXml, xml);
    }

    public void testRenderWithSizeLimit() {
        List attributeList = new ArrayList();
        attributeList.add("name");
        attributeList.add("monsterAttr");
        XmlMonitorRenderer renderer = new XmlMonitorRenderer(attributeList);
        renderer.setMaxCharacters(128);

        TransactionMonitor monitor = new TransactionMonitor("parent");
        TransactionMonitor child = new TransactionMonitor("child");
        child.set("monsterAttr", "blah blah blah blah blah blah blah blah blah blah blah blah blah blah");
        monitor.addChildMonitor(child);

        String xml = renderer.renderMonitor(monitor);
        assertEquals("<TransactionMonitor name=\"parent\">\n" +
                "  Error rendering event pattern: length is 173, max length is 128\n" +
                "</TransactionMonitor>", xml);
    }

    public void testRenderWithCycleInObjectGraph() {
        List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("failureThrowable");
        XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);

        // this should never happen in real code
        Exception e1 = new Exception("foo");
        Exception e2 = new IllegalStateException("bar", e1);
        e1.initCause(e2);

        TransactionMonitor monitor = new TransactionMonitor("name");
        monitor.failedDueTo(e1);

        String xml = renderer.renderMonitor(monitor);
        assertEquals("<TransactionMonitor name=\"name\" failureThrowable=\"java.lang.Exception: foo\"/>", xml);
    }

    public void testRenderWithDynaBeanException() {
        MonitoringEngine.getInstance().setDecomposer(new AttributeDecomposer());
        MonitoringEngine.getInstance().setProcessorFactory(
                new MockMonitorProcessorFactory(new MockMonitorProcessor()));
        MonitoringEngine.getInstance().startup();

        List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("failureThrowable");
        XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);

        Exception e = new Exception("foo") {
            public String getMessage() {
                return super.getMessage() + "\n  stack trace element 1\n  stack trace element 2\n  stack trace element 3\n";
            }
        };

        TransactionMonitor monitor = new TransactionMonitor("name");
        monitor.failedDueTo(e);
        monitor.done();

        String xml = renderer.renderMonitor(monitor);
        assertEquals("<TransactionMonitor name=\"name\" failureThrowable=\"com.orbitz.monitoring.lib.renderer.XmlMonitorRendererTest$1: foo\"/>", xml);

        SerializableCompositeMonitor sMonitor = (SerializableCompositeMonitor) monitor.getSerializableMomento();

        xml = renderer.renderMonitor(sMonitor);
        assertEquals("<SerializableCompositeMonitor name=\"name\" failureThrowable=\"com.orbitz.monitoring.lib.renderer.XmlMonitorRendererTest$1\"/>", xml);

        MonitoringEngine.getInstance().shutdown();
        MonitoringEngine.getInstance().setDecomposer(new MockDecomposer());
    }

    public void testRenderWithInvalidXmlCharacters() {
        List allowedAttributes = new ArrayList();
        allowedAttributes.add("name");
        allowedAttributes.add("crap");
        XmlMonitorRenderer renderer = new XmlMonitorRenderer(allowedAttributes);
        renderer.setMaxCharacters(256);

        TransactionMonitor monitor = new TransactionMonitor("<name>&");
        monitor.set("crap", "<>&\"");
        assertEquals("<TransactionMonitor name=\"name\" crap=\"&lt;&gt;&amp;&quot;\"/>",
                renderer.renderMonitor(monitor));
    }
}

class MyTransactionMonitor extends TransactionMonitor {
    public MyTransactionMonitor(String name) {
        super(name);
    }
};

