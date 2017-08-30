package demo;

//import com.air.ianaIfType.Ianaift;
import com.tailf.jnc.JNCException;

import java.util.Properties;

/**
 * Created by lig on 17/8/27.
 */
public class JPython {
    public static void main(String args[]) throws JNCException {
//        Properties props = new Properties();
////        Ianaift.enable();
//        //props.setProperty("python.home","/Users/lig/Documents/workspace/play-yang/yang-codegen/src/main/resources/jython2.7.1/");
        System.setProperty("python.path","/Users/lig/Documents/workspace/play-yang/yang-codegen/src/main/resources/jython2.7.1/Lib");
        System.setProperty("org.python.netty.noUnsafe","true");
////        props.put("python.console.encoding", "UTF-8");
////        props.put("python.security.respectJavaAccessibility", "false");
////        props.put("python.import.site","false");
//        Properties preprops = System.getProperties();
//
//        PythonInterpreter.initialize(preprops, props, new String[0]);
//        final PythonInterpreter inter = new PythonInterpreter();
//        final PythonInterpreter inter = JythonEnvironment.getInstance().getPythonInterpreter();
//        inter.execfile("/Users/lig/Documents/workspace/play-yang/yang-core/src/main/resources/pyang/setup.py");
//        inter.exec("import sys");
//        inter.exec("sys.path.append('/Users/lig/Documents/workspace/play-yang/yang-codegen/src/main/resources/pyang/pyang')");//我们自己写的
//        inter.exec("sys.path.append('/opt/local/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/site-packages/pyang-1.7.3-py2.7.egg/yang')");
//        inter.exec("sys.path.append('/Users/lig/Documents/workspace/play-yang/yang-codegen/src/main/resources/pyang/pyang/translators')");
//        inter.execfile("/Users/lig/Documents/workspace/play-yang/yang-codegen/src/main/resources/pyang/bin/pyang");

//        org.python.util.jython.main(new String[]{"/Users/lig/jython2.7.1/bin/pyang","-v"});
//        inter.exec("pyang -f jnc --jnc-output target/generated-sources/java/src  -p src/main/yang/   src/main/yang/ietf-interfaces.yang");
//        org.python.util.jython.main(args);
//        org.python.util.jython.main(new String[]{"-V"});
//        org.python.util.jython.main(new String[]{"/Users/lig/Documents/workspace/play-yang/yang-codegen/src/main/resources/pyang/bin/pyang","-v"});
    }
}
