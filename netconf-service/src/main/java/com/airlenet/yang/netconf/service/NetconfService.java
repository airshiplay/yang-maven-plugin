package com.airlenet.yang.netconf.service;

import com.airlenet.yang.common.PlayNetconfDevice;
import com.airlenet.yang.common.PlayNetconfSession;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by airshiplay on 17/8/31.
 */
@Service
public class NetconfService {
    public void editeConfig(PlayNetconfDevice playNetconfDevice, Element... elements) throws IOException, JNCException {
        PlayNetconfSession playNetconfSession = playNetconfDevice.getDefaultNetconfSession();
        for (Element element : elements) {
            playNetconfSession.editConfig(element);
        }
    }
    public Element get(PlayNetconfDevice playNetconfDevice,String xpath)throws IOException, JNCException{
        PlayNetconfSession playNetconfSession = playNetconfDevice.getDefaultNetconfSession();
        return playNetconfSession.get(xpath).get(0);
    }

    public Element getConfig(PlayNetconfDevice playNetconfDevice,String xpath)throws IOException, JNCException{
        PlayNetconfSession playNetconfSession = playNetconfDevice.getDefaultNetconfSession();
        return playNetconfSession.getConfig(xpath).get(0);
    }
}
