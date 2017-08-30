package com.airlenet.yang.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Created by airlenet on 17/8/28.
 */
public class Codegen {
    static Logger logger  = LoggerFactory.getLogger(Codegen.class);
    private File yangRoot;
    private File outDir;
    private String basePkgName;
    private List<String> yangImportList;

    private List<String> yangList;
    public Codegen(File yangRoot, File outDir, String basePkgName) {
        this.yangRoot = yangRoot;
        this.outDir = outDir;
        this.basePkgName = basePkgName;
    }

    public void generatorCode(boolean showWarnings, String jython, String pyang) throws Exception {

        String jncHome= System.getProperty("user.home")+File.separator+".jnc";
        if(!new File(jncHome).exists()){
            new File(jncHome).mkdirs();
        }
        String jnc = jncHome+File.separator+"jnc.py";
        IOUtil.cp(getClass().getClassLoader().getResourceAsStream("jnc.py"),jnc);

        StringBuilder builder = new StringBuilder();
        builder.append(yangRoot.getAbsolutePath());
        for(String importFile:yangImportList){
            builder.append(":");
            builder.append(importFile);
        }
        String path = builder.toString();
        //logger.info("pyang -f jnc --plugindir "+jncHome+" --jnc-output "+ outDir.getAbsolutePath()+"/"+basePkgName+" -p "+path+" --jnc-classpath-schema-loading");
        for(String yangfile:yangList){
            logger.info("convert yang file "+ yangfile);
            ProcessUtil.process(showWarnings,jython,pyang,"-f","jnc",
                    "--plugindir",jncHome,
                    "--jnc-output",outDir.getAbsolutePath()+"/"+basePkgName,
                    "-p",path,"--jnc-classpath-schema-loading",
                    yangfile);
        }

    }
    public File getYangRoot() {
        return yangRoot;
    }

    public void setYangRoot(File yangRoot) {
        this.yangRoot = yangRoot;
    }

    public File getOutDir() {
        return outDir;
    }

    public void setOutDir(File outDir) {
        this.outDir = outDir;
    }

    public String getBasePkgName() {
        return basePkgName;
    }

    public void setBasePkgName(String basePkgName) {
        this.basePkgName = basePkgName;
    }

    public List<String> getYangList() {
        return yangList;
    }

    public void setYangList(List<String> yangList) {
        this.yangList = yangList;
    }

    public List<String> getYangImportList() {
        return yangImportList;
    }

    public void setYangImportList(List<String> yangImportList) {
        this.yangImportList = yangImportList;
    }
}
