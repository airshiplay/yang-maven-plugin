package com.airlenet.yang.netconf.rest;

import com.airlenet.yang.common.PlayNetconfDevice;
import com.airlenet.yang.netconf.service.NetconfService;
import com.airlenet.yang.netconf.service.SysService;
import com.tailf.jnc.JNCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Created by airshiplay on 17/8/31.
 */

@Controller
public class NetconfRest {
    @Autowired
    SysService sysService;

    PlayNetconfDevice netconfDevice = new PlayNetconfDevice(1L, "admin", "admin", "admin", "admin", "172.16.129.181", 2022);
    @Autowired
    NetconfService netconfService;
    @RequestMapping("/")
    @ResponseBody
    public Object getVersion(){
        try {
         return    sysService.getVersion(netconfDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
//    @RequestMapping(name = "/get/{xpath}")
//    public Object get(@PathVariable("xpath") String xpath){
//        try {
//            return netconfService.get(netconfDevice,xpath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JNCException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    @RequestMapping("/getConfig/{xpath}")
    @ResponseBody
    public Object getConfig(@PathVariable("xpath") String xpath){
        try {
            return netconfService.getConfig(netconfDevice,xpath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JNCException e) {
            e.printStackTrace();
        }
        return "";
    }
}
