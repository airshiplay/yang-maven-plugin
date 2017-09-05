package com.airlenet.yang.netconf.service;

import com.airlenet.yang.common.PlayNetconfDevice;
import com.airlenet.yang.common.PlayNetconfSession;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NodeSet;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by airshiplay on 17/8/31.
 * @author airshiplay
 */
@Service
public class NetconfService {
    /**
     *
     * @param playNetconfDevice
     * @param elements
     * @throws IOException
     * @throws JNCException
     */
    public void editConfig(PlayNetconfDevice playNetconfDevice, Element... elements) throws IOException, JNCException {
        PlayNetconfSession playNetconfSession = playNetconfDevice.getDefaultNetconfSession();
        for (Element element : elements) {
            playNetconfSession.editConfig(element);
        }
    }

    public NodeSet get(PlayNetconfDevice playNetconfDevice) throws IOException, JNCException {
        PlayNetconfSession playNetconfSession = playNetconfDevice.getDefaultNetconfSession();
        NodeSet nodeSet = playNetconfSession.get();
        return nodeSet;
    }

    /**
     *
     * @param playNetconfDevice
     * @param xpath
     * @return
     * @throws IOException
     * @throws JNCException
     */
    public NodeSet get(PlayNetconfDevice playNetconfDevice, String xpath) throws IOException, JNCException {
        PlayNetconfSession playNetconfSession = playNetconfDevice.getDefaultNetconfSession();
        NodeSet nodeSet = playNetconfSession.get(xpath);
        return nodeSet;
    }

    /**
     *
     * @param playNetconfDevice
     * @param xpath
     * @return
     * @throws IOException
     * @throws JNCException
     */
    public NodeSet getConfig(PlayNetconfDevice playNetconfDevice, String xpath) throws IOException, JNCException {
        PlayNetconfSession playNetconfSession = playNetconfDevice.getDefaultNetconfSession();
        NodeSet nodeSet = playNetconfSession.getConfig(xpath);
        return nodeSet;
    }

    public NodeSet getConfig(PlayNetconfDevice netconfDevice) throws IOException, JNCException{
        PlayNetconfSession playNetconfSession = netconfDevice.getDefaultNetconfSession();
        NodeSet nodeSet = playNetconfSession.getConfig();
        return nodeSet;
    }
}
