/*
 * Copyright (c) 2012 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.airlenet.yang.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * AddTestCompileSourceRootMojo adds the folder for generated tests sources to the POM
 * 
 * @goal add-test-sources
 * @phase generate-sources
 * @threadSafe true
 */
public class AddTestCompileSourceRootMojo extends AbstractMojo {
    
    /**
     * @parameter expression="${project}" readonly=true required=true
     */
    private MavenProject project;
    
    /**
     * @parameter
     */
    private File outputDirectory;
    
    /**
     * @parameter
     */
    private File testOutputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File directory = testOutputDirectory != null ? testOutputDirectory : outputDirectory;
        if (!directory.exists()) {
            directory.mkdirs();
        }
        project.addTestCompileSourceRoot(directory.getAbsolutePath());
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setTestOutputDirectory(File testOutputDirectory) {
        this.testOutputDirectory = testOutputDirectory;
    }
    
}
