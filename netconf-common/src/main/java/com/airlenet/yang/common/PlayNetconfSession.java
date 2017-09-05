package com.airlenet.yang.common;

import com.tailf.jnc.*;

import java.io.IOException;

/**
 * Created by airlenet on 17/8/24.
 * @author airlenet
 */
public class PlayNetconfSession {
    private final NetconfSession netconfSession;
    private final PlayNetconfDevice playNetconfDevic;

    public PlayNetconfSession(PlayNetconfDevice playNetconfDevice, NetconfSession defaultPlaySession) {
        this.netconfSession = defaultPlaySession;
        this.playNetconfDevic =playNetconfDevice;
    }

    public boolean isOpenTransaction() {
        return playNetconfDevic.isOpenTransaction();
    }

    public boolean isCandidate() {
        return netconfSession.hasCapability(Capabilities.CANDIDATE_CAPABILITY);
    }

    public boolean isConfirmedCommit() {
        return this.netconfSession.hasCapability(Capabilities.CONFIRMED_COMMIT_CAPABILITY);
    }
    public boolean isWritableRunning(){
        return this.netconfSession.hasCapability(Capabilities.WRITABLE_RUNNING_CAPABILITY);
    }
    public long getSessionId(){
        return this.netconfSession.sessionId;
    }
    public void editConfig(Element configTree) throws IOException, JNCException {
        if (isOpenTransaction()) {
            if (isCandidate()) {
                this.netconfSession.editConfig(NetconfSession.CANDIDATE, configTree);
            } else {
                netconfSession.editConfig(configTree);
            }
        } else {
            if (isCandidate()) {
                try {
                    netconfSession.discardChanges();//现将 上次没有提交的配置 还原
                    netconfSession.lock(NetconfSession.CANDIDATE);
                    netconfSession.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
                    this.netconfSession.editConfig(NetconfSession.CANDIDATE, configTree);
                    if (isConfirmedCommit()) {
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

    public NodeSet get() throws IOException, JNCException {
        return this.netconfSession.get();
    }

    public NodeSet get(String xpath) throws IOException, JNCException {
        return this.netconfSession.get(xpath);
    }

    public NodeSet getConfig(String xpath) throws IOException,JNCException{
        return  this.netconfSession.getConfig(xpath);
    }

    public NodeSet getConfig() throws IOException,JNCException{
        return  this.netconfSession.getConfig();
    }

    public NetconfSession getNetconfSession(){
        return this.netconfSession;
    }

}
