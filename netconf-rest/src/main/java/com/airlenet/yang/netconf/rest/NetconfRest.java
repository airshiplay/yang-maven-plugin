package com.airlenet.yang.netconf.rest;

import com.airlenet.yang.common.PlayNetconfDevice;
import com.airlenet.yang.netconf.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lig on 17/8/31.
 */

@RestController
public class NetconfRest {
    @Autowired
    SysService sysService;

    @RequestMapping("/")
    public Object getVersion(){
        PlayNetconfDevice netconfDevice = new PlayNetconfDevice(1L, "admin", "admin", "admin", "admin", "172.", 2022);

        try {
            sysService.getVersion(netconfDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
