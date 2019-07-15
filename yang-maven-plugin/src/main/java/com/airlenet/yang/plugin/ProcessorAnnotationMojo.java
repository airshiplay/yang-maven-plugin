/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.airlenet.yang.plugin;

import com.airlenet.yang.codegen.IOUtil;
import com.airlenet.yang.codegen.ProcessUtil;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ProcessorAnnotationMojo calls APT processors for code generation
 *
 * @goal process
 * @phase generate-sources
 * @requiresDependencyResolution compile
 * @threadSafe true
 */
public class ProcessorAnnotationMojo extends AbstractProcessorMojo {

    public void execute() throws MojoExecutionException {
        if (getOutputDirectory() == null) {
            return;
        }
        if (skip) {
            return;
        }

        if (!getOutputDirectory().exists()) {
            getOutputDirectory().mkdirs();
        }
        checkPyang();

        getLog().debug("Using build context: " + buildContext);
        List<String> yangImportRoots = getYangImportRoots();

        File yangSourceRoot = getYangSourceRoot();

        try {
            List<String> yangList = getYangFileList();

            StringBuilder builder = new StringBuilder();
            builder.append(yangSourceRoot.getAbsolutePath());
            for (String importFile : yangImportRoots) {
                builder.append(System.getProperty("path.separator"));
                builder.append(importFile);
            }
            String path = builder.toString();

            getLog().info("Total " + yangList.size() + " yang files to be converter");

            String jncHome = System.getProperty("user.home") + File.separator + ".jnc";
            String jnc = jncHome + File.separator + "jnc.py";
            if (!new File(jncHome).exists()) {
                new File(jncHome).mkdirs();
            }
            if (new File(jnc).exists()) {
                new File(jnc).delete();
                new File(jncHome + File.separator + "jnc.pyc").delete();
            }
            IOUtil.cp(getClass().getClassLoader().getResourceAsStream("jnc.py"), jnc);

            getLog().info("pyang -f jnc --plugindir " + jncHome + " --jnc-output " + getOutputDirectory().getAbsolutePath() + "/" + packageName + " --jnc-prefix " + prefix + " -p " + path + " --jnc-classpath-schema-loading");

            for (String yangfile : yangList) {
                getLog().info("convert yang file " + yangfile);
                try {
                    List<String> command = new ArrayList<>();
                    if (pythonUsing) {
                        jython = null;
                        if (windows) {
                            command.add(python.getAbsolutePath());
                            command.add(new File(pythonHome, "Scripts/pyang").getAbsolutePath());
                        } else {
                            command.add("pyang");
                        }
                    } else {
                        command.add(pyang.getAbsolutePath());
                    }
                    command.add("-f");
                    command.add("jnc");
                    command.add("--plugindir");
                    command.add(jncHome);
                    command.add("--jnc-output");
                    command.add(getOutputDirectory().getAbsolutePath() + "/" + packageName);
                    command.add("--jnc-prefix");
                    command.add(prefix);
                    if(ignoreErrors)
                    command.add("--ignore-errors");
                    if(extraCommands!=null){
                        command.addAll(extraCommands);
                    }
                    command.add("-p");
                    command.add(path);
                    command.add("--jnc-classpath-schema-loading");
                    command.add("--lax-quote-checks");
                    command.add(yangfile);
                    ProcessUtil.process(jython, command);
                } catch (Exception e) {
                    getLog().error("convert yang file error " + yangfile, e);
                    if (errorAbort)
                        throw e;
                }
            }
        } catch (Exception e) {
            getLog().error("execute error", e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
