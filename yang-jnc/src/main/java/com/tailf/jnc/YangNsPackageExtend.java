package com.tailf.jnc;

public interface YangNsPackageExtend {

    public boolean can(String ns, String module, String revision);

    public String getPackage(String ns, String module, String revision);
}
