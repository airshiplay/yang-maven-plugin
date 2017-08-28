package com.airlenet.yang.common;

import com.tailf.jnc.IOSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lig on 17/8/24.
 */
public class PlayNotification extends IOSubscriber {

   private static Logger logger = LoggerFactory.getLogger(PlayNotification.class);
    private PlayNetconfDevice playNetconfDevice;
    /**
     * Empty constructor. The rawmode, inb and outb fields will be unassigned.
     */
    public PlayNotification(PlayNetconfDevice playNetconfDevice) {
        super(false);
        this.playNetconfDevice = playNetconfDevice;
    }

    /**
     * Will get called as soon as we have input (data which is received).
     *
     * @param s Text being received
     */
    @Override
    public void input(String s) {
        logger.info("receive from ip:"+ this.playNetconfDevice.getMgmt_ip()+" message:"+s);
        this.playNetconfDevice.setOpenTransaction(false);
    }

    /**
     * Will get called as soon as we have output (data which is being sent).
     *
     * @param s Text being sent
     */
    @Override
    public void output(String s) {
        logger.debug("send to ip:"+ this.playNetconfDevice.getMgmt_ip()+" message:"+s);
    }
}
