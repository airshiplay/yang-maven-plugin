package com.airlenet.yang.common;

import com.tailf.jnc.JNCException;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by airlenet on 17/8/24.
 */
public class PlayNetconfPool {


    private static ConcurrentHashMap<PlayNetconfDevice,BlockingQueue<PlayNetconfSession>> playDeviceMap;


    public static PlayNetconfSession getNetconfSession(PlayNetconfDevice playNetconfDevice) throws IOException, JNCException, InterruptedException {
        BlockingQueue<PlayNetconfSession> playNetconfSessions = playDeviceMap.get(playNetconfDevice);
        if(playNetconfSessions.isEmpty()){
            playNetconfSessions.add(playNetconfDevice.getDefaultNetconfSession());
        }
        return playNetconfSessions.take();
    }
}
