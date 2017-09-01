package com.airlenet.yang.netconf.service;

import com.airlenet.yang.common.PlayNetconfDevice;
import com.airlenet.yang.common.PlayNetconfSession;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by lig on 17/8/31.
 */
@Service
public class NetconfService {
    public void editeConfig(PlayNetconfDevice playNetconfDevice, Element... elements) throws IOException, JNCException {
        PlayNetconfSession playNetconfSession = playNetconfDevice.getNetconfSession();
        for (Element element : elements) {
            playNetconfSession.editConfig(element);
        }
    }
}
