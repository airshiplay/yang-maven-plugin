package com.tailf.jnc;

public class YangNsPackage {
    private String ns;
    private String module;
    private String revision;
    private String pkg;

    public YangNsPackage(String ns, String pkg) {
        this.ns = ns;
        this.pkg = pkg;
    }

    public YangNsPackage(String ns, String module, String revision, String pkg) {
        this.ns = ns;
        this.module = module;
        this.revision = revision;
        this.pkg = pkg;
    }

    public String getNs() {
        return ns;
    }

    public String getModule() {
        return module;
    }

    public String getRevision() {
        return revision;
    }

    public String getPkg() {
        return pkg;
    }
}
