package com.airlenet.yang.test;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NodeSet;

import java.io.IOException;

public class YangModelTest {
    public static void main(String[] args) throws JNCException, IOException {

//        FlexbngSysmgrPrefix.enable();
//        SmgrWconfdPrefix.enable();

        Device test = new Device("test", "172.19.106.72", 2022);
        test.addUser(new DeviceUser("admin", "admin", "admin"));
        test.connect("admin");
        test.newSession("test");
        NodeSet nodeSet = test.getSession("test").get("sys-info");

//        SysInfo sysInfo =(SysInfo) nodeSet.get(0);

//        sysInfo.getVersion();

    }
}
