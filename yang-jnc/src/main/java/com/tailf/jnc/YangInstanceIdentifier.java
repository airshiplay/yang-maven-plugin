package com.tailf.jnc;

public class YangInstanceIdentifier extends YangBaseType<Path> {
    public YangInstanceIdentifier() {
    }

    public YangInstanceIdentifier(String s) throws YangException {
        super(s);
    }

    public YangInstanceIdentifier(Path value) throws YangException {
        super(value);
    }

    @Override
    protected Path fromString(String s) throws YangException {
        return null;
    }

    @Override
    protected YangBaseType<Path> cloneShallow() throws YangException {
        return null;
    }

    @Override
    public boolean canEqual(Object obj) {
        return false;
    }
}
