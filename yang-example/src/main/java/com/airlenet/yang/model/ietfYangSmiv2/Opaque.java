/* 
 * @(#)Opaque.java        1.0
 *
 * This file has been auto-generated by JNC, the
 * Java output format plug-in of pyang.
 * Origin: module "ietf-yang-smiv2", revision: "2011-11-25".
 */

package com.airlenet.yang.model.ietfYangSmiv2;

import com.tailf.jnc.YangBinary;
import com.tailf.jnc.YangException;

/**
 * This class represents an element from 
 * the namespace 
 * generated to "src/main/java/com.airlenet.yang.model/ietfYangSmiv2/opaque"
 * <p>
 * See line 54 in
 * src/main/yang/module/ietf/ietf-yang-smiv2.yang
 *
 * @version 1.0
 * @author Auto Generated
 */
public class Opaque extends YangBinary {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for Opaque object from a string.
     * @param value Value to construct the Opaque from.
     */
    public Opaque(String value) throws YangException {
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
     * Checks all restrictions (if any).
     */
    public void check() throws YangException {
    }

}