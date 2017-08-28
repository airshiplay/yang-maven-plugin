package demo;

//import com.air.ianaIfType.Ianaift;
import com.tailf.jnc.JNCException;
import org.python.util.PythonInterpreter;

import java.util.Properties;

/**
 * Created by lig on 17/8/27.
 */
public class JPython {
    public static void main(String args[]) throws JNCException {
        Properties props = new Properties();
//        Ianaift.enable();
        props.put("python.home","/opt/local/Library/Frameworks/Python.framework/Versions/2.7/lib");


        props.put("python.console.encoding", "UTF-8");
        props.put("python.security.respectJavaAccessibility", "false");
        props.put("python.import.site","false");
        Properties preprops = System.getProperties();
        PythonInterpreter.initialize(preprops, props, new String[0]);
        final PythonInterpreter inter = new PythonInterpreter();
//        final PythonInterpreter inter = JythonEnvironment.getInstance().getPythonInterpreter();
//        inter.execfile("/Users/lig/Documents/workspace/play-yang/yang-core/src/main/resources/pyang/setup.py");
        inter.execfile("/Users/lig/Documents/workspace/play-yang/yang-core/src/main/resources/pyang/bin/pyang");


//        inter.exec("pyang -f jnc --jnc-output target/generated-sources/java/src  -p src/main/yang/   src/main/yang/ietf-interfaces.yang");
    }
}
