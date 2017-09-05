package com.airlenet.yang.netconf.rest;

import com.airlenet.play.repo.domain.Result;
import com.airlenet.yang.common.PlayNetconfDevice;
import com.airlenet.yang.netconf.service.NetconfService;
import com.airlenet.yang.netconf.service.SysService;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Created by airshiplay on 17/8/31.
 *
 * @author airshiplay
 */
@Controller
@RequestMapping("/netconf")
public class NetconfRest {
    @Autowired
    SysService sysService;

    PlayNetconfDevice netconfDevice = new PlayNetconfDevice(1L, "admin", "admin", "admin", "admin", "172.16.129.181", 2022);
    @Autowired
    NetconfService netconfService;

    @RequestMapping(path = {"","/"})
    @ResponseBody
    public Object getVersion() {
        try {
            return sysService.getVersion(netconfDevice);
        } catch (IOException e) {
            return Result.exception().message(e.getMessage());
        } catch (JNCException e) {
            return Result.exception().addContent(e.getRpcErrors());
        } catch (Exception e) {
            return Result.exception().message(e.getMessage());
        }
    }

    @RequestMapping("/get")
    @ResponseBody
    public Object get() {
        try {
            return Result.success().addContent(netconfService.get(netconfDevice));
        } catch (IOException e) {
            return Result.exception().message(e.getMessage());
        } catch (JNCException e) {
            return Result.exception().addContent(e.getRpcErrors());
        } catch (Exception e) {
            return Result.exception().message(e.getMessage());
        }
    }

    @RequestMapping("/get/{xpath}")
    @ResponseBody
    public Object getXpath(@PathVariable("xpath") String xpath) {
        try {
            return Result.success().addContent(netconfService.get(netconfDevice, xpath));
        } catch (IOException e) {
            return Result.exception().message(e.getMessage());
        } catch (JNCException e) {
            return Result.exception().addContent(e.getRpcErrors());
        } catch (Exception e) {
            return Result.exception().message(e.getMessage());
        }
    }
    @RequestMapping("/getConfig")
    @ResponseBody
    public Object getConfig() {
        try {
            return netconfService.getConfig(netconfDevice);
        } catch (IOException e) {
            return Result.exception().message(e.getMessage());
        } catch (JNCException e) {
            return Result.exception().addContent(e.getRpcErrors());
        } catch (Exception e) {
            return Result.exception().message(e.getMessage());
        }
    }

    @RequestMapping("/getConfig/{xpath}")
    @ResponseBody
    public Object getConfigXpath(@PathVariable("xpath") String xpath) {
        try {
            return netconfService.getConfig(netconfDevice, xpath);
        } catch (IOException e) {
            return Result.exception().message(e.getMessage());
        } catch (JNCException e) {
            return Result.exception().addContent(e.getRpcErrors());
        } catch (Exception e) {
            return Result.exception().message(e.getMessage());
        }
    }
}
