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
            if (!new File(jnc).exists()) {
                IOUtil.cp(getClass().getClassLoader().getResourceAsStream("jnc.py"), jnc);
            }

            getLog().info("pyang -f jnc --plugindir " + jncHome + " --jnc-output " + getOutputDirectory().getAbsolutePath() + "/" + packageName + " -p " + path + " --jnc-classpath-schema-loading");

            for (String yangfile : yangList) {
                getLog().info("convert yang file " + yangfile);
                try {
                    if (pythonUsing) {
                        if (windows) {
                            ProcessUtil.process(python.getAbsolutePath(), new File(pythonHome, "Scripts/pyang").getAbsolutePath(), "-f", "jnc",
                                    "--plugindir", jncHome,
                                    "--jnc-output", getOutputDirectory().getAbsolutePath() + "/" + packageName,
                                    "-p", path, "--jnc-classpath-schema-loading", "--lax-quote-checks",
                                    yangfile);
                        } else {
                            ProcessUtil.process("pyang", "-f", "jnc",
                                    "--plugindir", jncHome,
                                    "--jnc-output", getOutputDirectory().getAbsolutePath() + "/" + packageName,
                                    "-p", path, "--jnc-classpath-schema-loading", "--lax-quote-checks",
                                    yangfile);
                        }
                    } else {
                        ProcessUtil.process(jython, pyang.getAbsolutePath(), "-f", "jnc",
                                "--plugindir", jncHome,
                                "--jnc-output", getOutputDirectory().getAbsolutePath() + "/" + packageName,
                                "-p", path, "--jnc-classpath-schema-loading", "--lax-quote-checks",
                                yangfile);
                    }
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
