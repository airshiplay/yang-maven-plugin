package com.tailf.jnc;

public interface YangPackage {

    public boolean can(String ns, String module, String revision);

    public String getPackage(String ns, String module, String revision);
}
