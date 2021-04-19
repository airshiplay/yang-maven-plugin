package com.tailf.jnc;

public class YangNsPackage {
    private String ns;
    private String module;
    private String revision;
    private String name;
    private String pkg;

    protected YangNsPackage(String ns, String module, String revision) {
        this.ns = ns;
        this.module = module;
        this.revision = revision;
    }

    public YangNsPackage(String ns, String module, String revision, String name, String pkg) {
        this.ns = ns;
        this.module = module;
        this.revision = revision;
        this.name = name;
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

    public boolean match(String ns, String module, String revision, String name) {
        if (revision != null && !revision.equals(this.revision)) {
            return false;
        }
        if (module != null && !module.equals(this.module)) {
            return false;
        }
        if (name.equals(this.name) && ns.equals(this.ns)) {
            return true;
        }
        return false;
    }
}
