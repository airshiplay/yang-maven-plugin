package demo;

import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import org.junit.Test;

/**
 *
 * @author airlenet
 * @version 1.0.0
 *
 */
public class DemoTest {
    String listStr = "<interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <interface nc:operation=\"delete\">\n" +
            "    <name>vxlan-tunnel2</name>\n" +
            "  </interface>\n" +
            "  <interface nc:operation=\"delete\">\n" +
            "    <name>ipsec-tunnel2</name>\n" +
            "  </interface>\n" +
            "</interfaces>\n";
    String objStr = "<interface xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\" >" +
            "    <name>ipsec-tunnel2</name>\n" +
            "</interface>";
    Element elementList;
    Element elementObj;

    public DemoTest() throws JNCException {
        elementList = Element.readXml(listStr);
        elementObj = Element.readXml(objStr);
    }


    @Test
    public void testElementJson() {
        System.out.println(elementList.toJSONString());
        System.out.println(elementObj.toJSONString());
    }

    @Test
    public void testElementXml() {
        System.out.println(elementList.toXMLString());
        System.out.println(elementObj.toXMLString());
    }

}
