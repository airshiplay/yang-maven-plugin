package com.tailf.jnc;

public class YangInstanceIdentifier extends YangBaseString {

    /**
     * Creates a YangString object from a java.lang.String.
     *
     * @param value The Java String.
     * @throws YangException If an invariant was broken during assignment.
     */
    public YangInstanceIdentifier(String value) throws YangException {
        super(value);
    }
}
