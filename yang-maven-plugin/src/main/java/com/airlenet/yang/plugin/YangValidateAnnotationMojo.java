package com.airlenet.yang.plugin;

/**
 * Created by airshiplay on 2017/9/2.
 */

import com.airlenet.yang.codegen.ProcessUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.util.List;

/**
 *
 *
 * Says "Hi" to the user.
 *
 */

/**
 * ProcessorAnnotationMojo calls APT processors for code generation
 *
 * @goal validate
 * @threadSafe true
 */
@Mojo(name = "validate")
public class YangValidateAnnotationMojo extends AbstractProcessorMojo {
    public void execute() throws MojoExecutionException {
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

            getLog().info("Total " + yangList.size() + " yang files to be validate");
            String path = builder.toString();

            getLog().info("pyang   -p " + path );

            for (String yangfile : yangList) {
                getLog().info("convert yang file " + yangfile);
                try {
                    if (pythonUsing) {
                        if (windows) {
                            if (showWarnings) {
                                ProcessUtil.process(python.getAbsolutePath(), new File(pythonHome, "Scripts/pyang").getAbsolutePath(), yangfile, "-p", path, "--lax-quote-checks");
                            } else {
                                ProcessUtil.process(python.getAbsolutePath(), new File(pythonHome, "Scripts/pyang").getAbsolutePath(), yangfile, "-p", path, "--lax-quote-checks", "-W", "none");
                            }
                        } else {
                            ProcessUtil.process("pyang", yangfile, "-p", path, "--lax-quote-checks", "-W", "none");
                        }

                    } else {
                        ProcessUtil.process(jython, pyang.getAbsolutePath(), yangfile,
                                "-p", path, "--lax-quote-checks ");
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