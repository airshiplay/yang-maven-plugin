package com.airlenet.yang.netconf.service;

import com.airlenet.yang.common.PlayNetconfDevice;
import com.airlenet.yang.common.PlayNetconfSession;
import com.tailf.jnc.Element;
import com.tailf.jnc.NodeSet;
import org.springframework.stereotype.Service;

/**
 * Created by airlenet on 17/8/24.
 */
@Service
public class SysService {

    public Element getVersion(PlayNetconfDevice playNetconfDevice) throws Exception {
        PlayNetconfSession netconfSession = playNetconfDevice.getDefaultNetconfSession();
        NodeSet nodeSet = netconfSession.get("sys-info");
        if (nodeSet != null && !nodeSet.isEmpty()) {
            return nodeSet.get(0);
        }
        return null;
    }
}
