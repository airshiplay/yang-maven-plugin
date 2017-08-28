package com.airlenet.yang.common;

import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;

import java.io.IOException;

/**
 * Created by lig on 17/8/24.
 */
public class PlayNetconfSession {
    private NetconfSession netconfSession;

    public PlayNetconfSession(NetconfSession netconfSession) {
        this.netconfSession = netconfSession;
    }

    public void editConfig(Element configTree) throws IOException, JNCException {
        netconfSession.discardChanges();//现将 上次没有提交的配置 还原
        netconfSession.lock(NetconfSession.CANDIDATE);
        netconfSession.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
        this.netconfSession.editConfig(NetconfSession.CANDIDATE,configTree);
//        netconfSession.confirmedCommit(60);// candidates are now updated 1分钟内没有确认 则还原配置
        netconfSession.commit();//now commit them 确认提交
    }

    public NodeSet get(String xpath) throws IOException, JNCException {
     return this.netconfSession.get(xpath);
    }
}
