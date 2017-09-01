package com.airlenet.yang.common;

import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NetconfSession;
import com.tailf.jnc.NodeSet;

import java.io.IOException;

/**
 * Created by airlenet on 17/8/24.
 */
public class PlayNetconfSession {
    private final NetconfSession netconfSession;
    /**
     * true service拦截，开启事物处理；false 自己开启事物处理
     */
    private boolean openTransaction;
    private boolean candidate;
    private boolean confirmedCommit;

    public PlayNetconfSession(NetconfSession netconfSession) {
        this.netconfSession = netconfSession;
    }

    public boolean isOpenTransaction() {
        return openTransaction;
    }

    public void setOpenTransaction(boolean openTransaction) {
        this.openTransaction = openTransaction;
    }

    public boolean isCandidate() {
        return candidate;
    }

    public void setCandidate(boolean candidate) {
        this.candidate = candidate;
    }

    public boolean isConfirmedCommit() {
        return confirmedCommit;
    }

    public void setConfirmedCommit(boolean confirmedCommit) {
        this.confirmedCommit = confirmedCommit;
    }

    public void editConfig(Element configTree) throws IOException, JNCException {
        if (openTransaction) {
            if (candidate) {
                this.netconfSession.editConfig(NetconfSession.CANDIDATE, configTree);
            } else {
                netconfSession.editConfig(configTree);
            }
        } else {
            if (candidate) {
                try {
                    netconfSession.discardChanges();//现将 上次没有提交的配置 还原
                    netconfSession.lock(NetconfSession.CANDIDATE);
                    netconfSession.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
                    this.netconfSession.editConfig(NetconfSession.CANDIDATE, configTree);
                    if (confirmedCommit) {
                        netconfSession.confirmedCommit(60);// candidates are now updated 1分钟内没有确认 则还原配置
                    }
                    netconfSession.commit();//now commit them 确认提交
                } finally {
                    netconfSession.unlock(NetconfSession.CANDIDATE);
                }
            } else {
                netconfSession.editConfig(configTree);
            }
        }
    }

    public void copyConfig(Element configTree) throws IOException, JNCException {
        netconfSession.copyConfig(configTree, NetconfSession.RUNNING);
    }

    public NodeSet get(String xpath) throws IOException, JNCException {
        return this.netconfSession.get(xpath);
    }

    public NetconfSession getNetconfSession(){
        return this.getNetconfSession();
    }
}
