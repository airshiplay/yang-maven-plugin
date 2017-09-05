package com.airlenet.yang.common;

import com.tailf.jnc.*;
import jdk.nashorn.internal.objects.annotations.Getter;

import java.io.IOException;
import java.util.List;

/**
 * Created by airlenet on 17/8/24.
 */
public class PlayNetconfDevice {

    private Long id;
    private String localUser;
    private String remoteUser;
    private String password;
    private String name;
    private String mgmt_ip;
    private int mgmt_port;
    private Device device;
    /**
     * true service拦截，开启事物处理；false 自己开启事物处理
     */
    private boolean openTransaction;

    private List<PlayNetconfSession> playNetconfSessionList;
    public PlayNetconfDevice(Long id, String localUser, String remoteUser, String password, String name, String mgmt_ip, int mgmt_port) {
        this.id = id;
        this.localUser = localUser;
        this.remoteUser = remoteUser;
        this.password = password;
        this.name = name;
        this.mgmt_ip = mgmt_ip;
        this.mgmt_port = mgmt_port;
    }

    public PlayNetconfSession getDefaultNetconfSession() throws IOException, JNCException {
        DeviceUser duser = new DeviceUser(this.localUser, this.remoteUser, this.password);

        if(device==null){
            device = new Device(this.name, duser, this.mgmt_ip, this.mgmt_port);
            device.connect(this.localUser);
            device.newSession(new PlayNotification(this),"defaultPlaySession");
        }else{
            NetconfSession netconfSession = device.getSession("defaultPlaySession");
            if(netconfSession==null){
                device.connect(this.localUser);
                device.newSession(new PlayNotification(this),"defaultPlaySession");
            }
        }
       return new PlayNetconfSession(this, device.getSession("defaultPlaySession"));
    }

    public void closeDefaultNetconfSession() {
        device.closeSession("defaultPlaySession");
    }

    public Long getId() {
        return id;
    }

    public String getLocalUser() {
        return localUser;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getMgmt_ip() {
        return mgmt_ip;
    }

    public int getMgmt_port() {
        return mgmt_port;
    }

    public boolean isOpenTransaction() {
        return openTransaction;
    }

    public void setOpenTransaction(boolean openTransaction) {
        this.openTransaction = openTransaction;
    }


}
