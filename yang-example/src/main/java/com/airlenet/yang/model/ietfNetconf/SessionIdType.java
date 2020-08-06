/* 
 * @(#)SessionIdType.java        1.0
 *
 * This file has been auto-generated by JNC, the
 * Java output format plug-in of pyang.
 * Origin: module "ietf-netconf", revision: "2011-06-01".
 */

package com.airlenet.yang.model.ietfNetconf;

import com.tailf.jnc.YangException;
import com.tailf.jnc.YangUInt32;

/**
 * This class represents an element from 
 * the namespace 
 * generated to "src/main/java/com.airlenet.yang.model/ietfNetconf/session-id-type"
 * <p>
 * See line 166 in
 * src/main/yang/module/ietf/ietf-netconf.yang
 *
 * @version 1.0
 * @author Auto Generated
 */
public class SessionIdType extends YangUInt32 {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for SessionIdType object from a string.
     * @param value Value to construct the SessionIdType from.
     */
    public SessionIdType(String value) throws YangException {
        super(value);
        check();
    }

    /**
     * Constructor for SessionIdType object from a long.
     * @param value Value to construct the SessionIdType from.
     */
    public SessionIdType(long value) throws YangException {
        super(value);
        check();
    }

    /**
     * Sets the value using a string value.
     * @param value The value to set.
     */
    public void setValue(String value) throws YangException {
        super.setValue(value);
        check();
    }

    /**
     * Sets the value using a value of type long.
     * @param value The value to set.
     */
    public void setValue(long value) throws YangException {
        super.setValue(value);
        check();
    }

    /**
     * Checks all restrictions (if any).
     */
    public void check() throws YangException {
    }

}