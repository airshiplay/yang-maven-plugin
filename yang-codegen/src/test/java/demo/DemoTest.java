package demo;

import com.airlenet.yang.common.PlayNetconfDevice;
import com.airlenet.yang.common.PlayNetconfSession;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.YangXMLParser;
//import demo.employees.Employee;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by lig on 17/8/24.
 */
public class DemoTest {
    public  static void main(String args[]) throws JNCException, IOException {
       //Demo.enable();
//        Employees employees = new Employees();
//        Employee     employee = employees.addEmployee();
//        employee.setCityValue("nj");
//        employee.setIdValue(12);
//        employee.markCityDelete();
//        employee.setTitleValue("title");
//        System.out.print(new ObjectMapper().writeValueAsString(employees));
        PlayNetconfDevice playNetconfDevice = new PlayNetconfDevice(1L, "admin", "admin", "admin", "admin", "172.16.129.187", 2022);

//        PlayNetconfSession session = playNetconfDevice.getNetconfSession();
//       System.out.println( employees.toXMLString());

String str="<interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
        "  <interface nc:operation=\"delete\">\n" +
        "    <name>vxlan-tunnel2</name>\n" +
        "  </interface>\n" +
        "  <interface nc:operation=\"delete\">\n" +
        "    <name>ipsec-tunnel2</name>\n" +
        "  </interface>\n" +
        "</interfaces>\n";


        Element element = new YangXMLParser().parse(new InputSource(new ByteArrayInputStream(str.getBytes())));


//        session.editConfig(element);
//        Element element = Element.read(str);
        System.out.print(element.toXMLString());
//        System.out.print(element.toJsonString());
    }
}
