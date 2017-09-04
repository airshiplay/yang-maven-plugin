/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.airlenet.yang.plugin;

import com.airlenet.yang.codegen.ProcessUtil;
import com.airlenet.yang.codegen.PyangInstall;
import com.google.common.base.Joiner;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Scanner;
import org.python.util.install.Installation;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * Base class for ProcessorAnnotationMojo implementations
 *
 * @author tiwe
 */
public abstract class AbstractProcessorMojo extends AbstractMojo {

    private static final String YANG_FILE_FILTER = "*.yang";
    private static final String[] ALL_YANG_FILES_FILTER = new String[]{"**/" + YANG_FILE_FILTER};
    /**
     * @parameter
     */
    private File outputDirectory;
    /**
     * @component
     */
    protected BuildContext buildContext;

    /**
     * @parameter expression="${project}" readonly=true required=true
     */
    protected MavenProject project;

    /**
     * @parameter
     */
    protected String[] processors;

    /**
     * @parameter
     */
    protected String processor;

    /**
     * @parameter expression="${project.build.sourceEncoding}" required=true
     */
    protected String sourceEncoding;

    /**
     * @parameter
     */
    protected Map<String, String> options;

    /**
     * @parameter
     */
    protected Map<String, String> compilerOptions;

    /**
     * @parameter
     */
    protected Set<String> includes = new HashSet<String>();

    /**
     * @parameter
     */
    protected boolean showWarnings = true;
    /**
     * @parameter
     */
    protected boolean skip = false;

    /**
     * yang Error 是否终止
     *
     * @parameter
     */
    protected boolean errorAbort = false;
    /**
     * @parameter required=true
     */
    protected String packageName = "com.airlenet.yang.model";
    /**
     * @parameter
     */
    protected List<String> excludes;
    /**
     * @parameter
     */
    protected boolean logOnlyOnError = false;

    /**
     * @parameter expression="${plugin.artifacts}" readonly=true required=true
     */
    protected List<Artifact> pluginArtifacts;

    /**
     * A list of additional source roots for the apt processor
     *
     * @parameter required=false
     */
    protected List<String> additionalSourceRoots;

    /**
     * A list of additional test source roots for the apt processor
     *
     * @parameter required=false
     */
    protected List<String> additionalTestSourceRoots;

    /**
     * @parameter
     */
    protected boolean ignoreDelta = true;


    boolean pythonUsing = false;

    File jythonHome = new File(System.getProperty("user.home"), ".jython");
    File jython = new File(jythonHome, "bin/jython");
    File pyang = new File(jythonHome, "bin/pyang");
    File pyangSource = new File(jythonHome, "pyang");
    String osName = System.getProperty("os.name");
    File python = null;
    File pythonHome = null;
    boolean windows = false;

    public void checkPyang() throws MojoExecutionException {

        if (osName.startsWith("Window") || osName.startsWith("window")) {
            windows = true;
        }
        try {//检测 Python
            ProcessUtil.process("python", "-V");
            pythonUsing = true;
            String envPath = System.getenv("PATH");
            String[] envPaths = envPath.split(System.getProperty("path.separator"));

            for (String path : envPaths) {
                File[] pythons = new File(path).listFiles(new FileFilter() {
                    /**
                     * Tests whether or not the specified abstract pathname should be
                     * included in a pathname list.
                     *
                     * @param pathname The abstract pathname to be tested
                     * @return <code>true</code> if and only if <code>pathname</code>
                     * should be included
                     */
                    @Override
                    public boolean accept(File pathname) {
                        String absolutePath = pathname.getAbsolutePath();
                        return absolutePath.endsWith("python") || absolutePath.endsWith("python.exe");
                    }

                });
                if (pythons.length == 1) {
                    python = pythons[0];
                    pythonHome = python.getParentFile();
                    break;
                }
            }
        } catch (Exception ep) {// 没有 Python  检测jython，使用jython代替
            //检测jython 是否安装
            try {
                ProcessUtil.process(new File(jythonHome, "/bin/jython").getAbsolutePath(), "-V");
            } catch (Exception e) {//安装jython
                getLog().info("Jython is not installed. Start installation");
                Installation.main(new String[]{"-s", "-d", jythonHome.getAbsolutePath(), "-t", "standard", "-e", "demo", "doc"});
                try {//再次检测
                    ProcessUtil.process(jython.getAbsolutePath(), "-V");
                } catch (Exception e1) {
                    getLog().error("install jython fail,please install python 2.7.* from https://www.python.org/downloads/", e1);
                    throw new MojoExecutionException(e1.getMessage(), e1);
                }
            }
        }

        try {//检测 pyang
            List<String> commandList = new ArrayList<>();
            if (pythonUsing) {
                if (windows) {
                    commandList.add(python.getAbsolutePath());
                    commandList.add(new File(pythonHome, "Scripts/pyang").getAbsolutePath());
                } else {
                    commandList.add("pyang");
                }
            } else {
                commandList.add(jython.getAbsolutePath());
                commandList.add(pyang.getAbsolutePath());
            }
            commandList.add("-v");
            ProcessUtil.process(commandList);
        } catch (Exception e) {
            try {//安装pyang
                getLog().info("pyang is not installed. Start installation");
                PyangInstall.copy(jythonHome);
                if (pythonUsing) {
                    if (windows) {
                        ProcessUtil.process(pyangSource, "python", new File(pyangSource, "setup.py").getAbsolutePath(), "install");
                        ProcessUtil.process(python.getAbsolutePath(), new File(pythonHome, "Scripts/pyang").getAbsolutePath(), "-v");
                    } else {
                        ProcessUtil.process(pyangSource, "python", new File(pyangSource, "setup.py").getAbsolutePath(), "install");
                        ProcessUtil.process("pyang", "-v");
                    }
                } else {
                    ProcessUtil.process(pyangSource, jython.getAbsolutePath(), new File(pyangSource, "setup.py").getAbsolutePath(), "install");
                    ProcessUtil.process(jython.getAbsolutePath(), pyang.getAbsolutePath(), "-v");
                }
            } catch (Exception e1) {
                getLog().error("install pyang fail", e1);
                throw new MojoExecutionException(e1.getMessage(), e1);
            }
        }
    }

    /**
     * Filter files for apt processing based on the {@link #includes} filter and
     * also taking into account m2e {@link BuildContext} to filter-out unchanged
     * files when invoked as incremental build
     *
     * @param directories source directories in which files are located for apt processing
     * @return files for apt processing. Returns empty set when there is no
     * files to process
     */
    protected Set<File> filterFiles(Set<File> directories) {
        String[] filters = ALL_YANG_FILES_FILTER;
        if (includes != null && !includes.isEmpty()) {
            filters = includes.toArray(new String[includes.size()]);
        }

        Set<File> files = new HashSet<File>();
        for (File directory : directories) {
            // support for incremental build in m2e context
            Scanner scanner = buildContext.newScanner(directory, false);
            scanner.setIncludes(filters);
            if (excludes != null && !excludes.isEmpty()) {
                scanner.setExcludes(excludes.toArray(new String[0]));
            }
            scanner.scan();
            String[] includedFiles = scanner.getIncludedFiles();

            // check also for possible deletions
            if (buildContext.isIncremental() && (includedFiles == null || includedFiles.length == 0)) {
                scanner = buildContext.newDeleteScanner(directory);
                scanner.setIncludes(filters);
                if (excludes != null && !excludes.isEmpty()) {
                    scanner.setExcludes(excludes.toArray(new String[0]));
                }
                scanner.scan();
                includedFiles = scanner.getIncludedFiles();
            }

            // get all sources if ignoreDelta and at least one source file has changed
            if (ignoreDelta && buildContext.isIncremental() && includedFiles != null && includedFiles.length > 0) {
                scanner = buildContext.newScanner(directory, true);
                scanner.setIncludes(filters);
                if (excludes != null && !excludes.isEmpty()) {
                    scanner.setExcludes(excludes.toArray(new String[0]));
                }
                scanner.scan();
                includedFiles = scanner.getIncludedFiles();
            }

            if (includedFiles != null) {
                for (String includedFile : includedFiles) {
                    files.add(new File(scanner.getBasedir(), includedFile));
                }
            }
        }
        return files;
    }

    @SuppressWarnings("unchecked")
    protected Set<File> getSourceDirectories() {
        File outputDirectory = getOutputDirectory();
        String outputPath = outputDirectory == null ? "" : outputDirectory.getAbsolutePath();
        Set<File> directories = new HashSet<File>();
        List<String> directoryNames = getCompileSourceRoots();
        for (String name : directoryNames) {
            File file = new File(name);
            if (!file.getAbsolutePath().equals(outputPath) && file.exists() && file.isDirectory()) {
                directories.add(file);
            }
        }
        return directories;
    }

    protected Set<File> getYangSourceDirectories() {
        File outputDirectory = getOutputDirectory();
        String outputPath = outputDirectory == null ? "" : outputDirectory.getAbsolutePath();
        Set<File> directories = new HashSet<File>();
        List<String> directoryNames = Arrays.asList(getYangSourceRoot().getAbsolutePath());
        for (String name : directoryNames) {
            File file = new File(name);
            if (!file.getAbsolutePath().equals(outputPath) && file.exists() && file.isDirectory()) {
                directories.add(file);
            }
        }
        return directories;
    }

    public List<String> getYangFileList() {
        List<String> yangList = new ArrayList<>();

        Set<File> files = filterFiles(getYangSourceDirectories());
        for (File f : files) {
            yangList.add(f.getAbsolutePath());
        }
        return yangList;
    }

    private List<String> getTestCompileSourceRoots() {
        @SuppressWarnings("unchecked")
        final List<String> testCompileSourceRoots = project.getTestCompileSourceRoots();
        if (additionalTestSourceRoots == null) {
            return testCompileSourceRoots;
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Adding additional test source roots: " + Joiner.on(", ").skipNulls().join(additionalTestSourceRoots));
        }
        List<String> sourceRoots = new ArrayList<String>(testCompileSourceRoots);
        sourceRoots.addAll(additionalTestSourceRoots);
        return sourceRoots;
    }

    private List<String> getCompileSourceRoots() {
        @SuppressWarnings("unchecked")
        final List<String> compileSourceRoots = project.getCompileSourceRoots();
        if (additionalSourceRoots == null) {
            return compileSourceRoots;
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Adding additional source roots: " + Joiner.on(", ").skipNulls().join(additionalSourceRoots));
        }
        List<String> sourceRoots = new ArrayList<String>(compileSourceRoots);
        sourceRoots.addAll(additionalSourceRoots);
        return sourceRoots;
    }

    protected File getYangSourceRoot() {
        return new File(project.getBasedir(), "src/main/yang");
    }

    protected List<String> getYangImportRoots() {
        File root = getYangSourceRoot();
        List<String> result = new ArrayList<>();
        result.add(root.getAbsolutePath());
        getFolder(result, root);
        return result;
    }

    private List<String> getFolder(List<String> list, File file) {

        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File f : files) {
            getFolder(list, f);
            list.add(f.getAbsolutePath());
        }
        return list;
    }

    public void setBuildContext(BuildContext buildContext) {
        this.buildContext = buildContext;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setProcessors(String[] processors) {
        this.processors = processors;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public void setCompilerOptions(Map<String, String> compilerOptions) {
        this.compilerOptions = compilerOptions;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes;
    }

    public void setShowWarnings(boolean showWarnings) {
        this.showWarnings = showWarnings;
    }

    public void setLogOnlyOnError(boolean logOnlyOnError) {
        this.logOnlyOnError = logOnlyOnError;
    }

    public void setPluginArtifacts(List<Artifact> pluginArtifacts) {
        this.pluginArtifacts = pluginArtifacts;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

}
