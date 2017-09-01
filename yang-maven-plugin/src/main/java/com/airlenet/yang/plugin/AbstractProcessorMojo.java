/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.airlenet.yang.plugin;

import com.airlenet.yang.codegen.Codegen;
import com.airlenet.yang.codegen.ProcessUtil;
import com.airlenet.yang.codegen.PyangInstall;
import com.google.common.base.Joiner;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.StringUtils;
import org.python.util.install.Installation;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.tools.*;
import javax.tools.Diagnostic.Kind;
import java.io.*;
import java.util.*;

/**
 * Base class for AnnotationProcessorMojo implementations
 *
 * @author tiwe
 */
public abstract class AbstractProcessorMojo extends AbstractMojo {

    private static final String YANG_FILE_FILTER = "*.yang";
    private static final String[] ALL_YANG_FILES_FILTER = new String[]{"**/" + YANG_FILE_FILTER};

    /**
     * @component
     */
    private BuildContext buildContext;

    /**
     * @parameter expression="${project}" readonly=true required=true
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private String[] processors;

    /**
     * @parameter
     */
    private String processor;

    /**
     * @parameter expression="${project.build.sourceEncoding}" required=true
     */
    private String sourceEncoding;

    /**
     * @parameter
     */
    private Map<String, String> options;

    /**
     * @parameter
     */
    private Map<String, String> compilerOptions;

    /**
     *
     * @parameter
     */
    private Set<String> includes = new HashSet<String>();

    /**
     * @parameter
     */
    private boolean showWarnings = true;
    /**
     * @parameter
     */
    private boolean skip = false;

    /**
     * @parameter required=true
     */
    private String packageName="com.airlenet.yang.model";
    /**
     * @parameter
     */
    private List<String> excludes;
    /**
     * @parameter
     */
    private boolean logOnlyOnError = false;

    /**
     * @parameter expression="${plugin.artifacts}" readonly=true required=true
     */
    private List<Artifact> pluginArtifacts;

    /**
     * A list of additional source roots for the apt processor
     *
     * @parameter required=false
     */
    private List<String> additionalSourceRoots;

    /**
     * A list of additional test source roots for the apt processor
     *
     * @parameter required=false
     */
    private List<String> additionalTestSourceRoots;

    /**
     * @parameter
     */
    private boolean ignoreDelta = true;

    @SuppressWarnings("unchecked")
    private String buildCompileClasspath() {
        List<String> pathElements = null;
        try {
            if (isForTest()) {
                pathElements = project.getTestClasspathElements();
            } else {
                pathElements = project.getCompileClasspathElements();
            }
        } catch (DependencyResolutionRequiredException e) {
            super.getLog().warn("exception calling getCompileClasspathElements", e);
            return null;
        }

        if (pluginArtifacts != null) {
            for (Artifact a : pluginArtifacts) {
                if (a.getFile() != null) {
                    pathElements.add(a.getFile().getAbsolutePath());
                }
            }
        }

        if (pathElements.isEmpty()) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        for (i = 0; i < pathElements.size() - 1; ++i) {
            result.append(pathElements.get(i)).append(File.pathSeparatorChar);
        }
        result.append(pathElements.get(i));
        return result.toString();
    }

    private String buildProcessor() {
        if (processors != null) {
            StringBuilder result = new StringBuilder();
            for (String processor : processors) {
                if (result.length() > 0) {
                    result.append(",");
                }
                result.append(processor);
            }
            return result.toString();
        } else if (processor != null) {
            return processor;
        } else {
            String error = "Either processor or processors need to be given";
            getLog().error(error);
//            throw new IllegalArgumentException(error);
            return "";
        }
    }

    private List<String> buildCompilerOptions(String processor, String compileClassPath,
                                              String outputDirectory) throws IOException {
        Map<String, String> compilerOpts = new LinkedHashMap<String, String>();

        // Default options
        compilerOpts.put("cp", compileClassPath);

        if (sourceEncoding != null) {
            compilerOpts.put("encoding", sourceEncoding);
        }

        compilerOpts.put("proc:only", null);
        compilerOpts.put("processor", processor);

        if (options != null) {
            for (Map.Entry<String, String> entry : options.entrySet()) {
                if (entry.getValue() != null) {
                    compilerOpts.put("A" + entry.getKey() + "=" + entry.getValue(), null);
                } else {
                    compilerOpts.put("A" + entry.getKey() + "=", null);
                }

            }
        }

        if (outputDirectory != null) {
            compilerOpts.put("s", outputDirectory);
        }

        if (!showWarnings) {
            compilerOpts.put("nowarn", null);
        }

        StringBuilder builder = new StringBuilder();
        for (File file : getSourceDirectories()) {
            if (builder.length() > 0) {
                builder.append(";");
            }
            builder.append(file.getCanonicalPath());
        }
        compilerOpts.put("sourcepath", builder.toString());

        // User options override default options
        if (compilerOptions != null) {
            compilerOpts.putAll(compilerOptions);
        }

        List<String> opts = new ArrayList<String>(compilerOpts.size() * 2);

        for (Map.Entry<String, String> compilerOption : compilerOpts.entrySet()) {
            opts.add("-" + compilerOption.getKey());
            String value = compilerOption.getValue();
            if (StringUtils.isNotBlank(value)) {
                opts.add(value);
            }
        }
        return opts;
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
    private Set<File> filterFiles(Set<File> directories) {
        String[] filters = ALL_YANG_FILES_FILTER;
        if (includes != null && !includes.isEmpty()) {
            filters = includes.toArray(new String[includes.size()]);
        }

        Set<File> files = new HashSet<File>();
        for (File directory : directories) {
            // support for incremental build in m2e context
            Scanner scanner = buildContext.newScanner(directory, false);
            scanner.setIncludes(filters);
            if(excludes !=null && !excludes.isEmpty()){
                scanner.setExcludes(excludes.toArray(new String[0]));
            }
            scanner.scan();
            String[] includedFiles = scanner.getIncludedFiles();

            // check also for possible deletions
            if (buildContext.isIncremental() && (includedFiles == null || includedFiles.length == 0)) {
                scanner = buildContext.newDeleteScanner(directory);
                scanner.setIncludes(filters);
                if(excludes !=null && !excludes.isEmpty()){
                    scanner.setExcludes(excludes.toArray(new String[0]));
                }
                scanner.scan();
                includedFiles = scanner.getIncludedFiles();
            }

            // get all sources if ignoreDelta and at least one source file has changed
            if (ignoreDelta && buildContext.isIncremental() && includedFiles != null && includedFiles.length > 0) {
                scanner = buildContext.newScanner(directory, true);
                scanner.setIncludes(filters);
                if(excludes !=null && !excludes.isEmpty()){
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

    /**
     * Add messages through the buildContext:
     * <ul>
     * <li>cli build creates log output</li>
     * <li>m2e build creates markers for eclipse</li>
     * </ul>
     *
     * @param diagnostics
     */
    private void processDiagnostics(final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            JavaFileObject javaFileObject = diagnostic.getSource();
            if (javaFileObject != null) { // message was created without element parameter
                File file = new File(javaFileObject.toUri().getPath());
                Kind kind = diagnostic.getKind();
                int lineNumber = (int) diagnostic.getLineNumber();
                int columnNumber = (int) diagnostic.getColumnNumber();
                String message = diagnostic.getMessage(Locale.getDefault());
                switch (kind) {
                    case ERROR:
                        buildContext.addMessage(file, lineNumber, columnNumber, message, BuildContext.SEVERITY_ERROR, null);
                        break;
                    case WARNING:
                    case MANDATORY_WARNING:
                        buildContext.addMessage(file, lineNumber, columnNumber, message, BuildContext.SEVERITY_WARNING, null);
                        break;
                    case NOTE:
                    case OTHER:
                    default:
                        break;
                }
            }
        }
    }

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
        boolean pythonUsing=false;

        File jythonHome = new File(System.getProperty("user.home"), ".jython");
        File jython = new File(jythonHome, "bin/jython");
        File pyang = new File(jythonHome, "bin/pyang");
        File pyangSource = new File(jythonHome,"pyang");
        String osName=System.getProperty("os.name");
        boolean linux =false;
        if(osName.startsWith("linux")|| osName.startsWith("Linux")){
            linux=true;
        }
        try{//检测 Python
            ProcessUtil.process(showWarnings,"python", "-V");
            pythonUsing=true;
        }catch (Exception ep){// 没有 Python  检测jython，使用jython代替
            //检测jython 是否安装
            try {
                ProcessUtil.process(showWarnings,new File(jythonHome, "/bin/jython").getAbsolutePath(), "-V");
            } catch (Exception e) {//安装jython
                getLog().info("Jython is not installed. Start installation");
                Installation.main(new String[]{"-s", "-d", jythonHome.getAbsolutePath(), "-t", "standard", "-e", "demo", "doc"});
                try {//再次检测
                    ProcessUtil.process(showWarnings,jython.getAbsolutePath(), "-V");
                } catch (Exception e1) {
                    getLog().error("install jython fail,please install python 2.7.* from https://www.python.org/downloads/", e1);
                    throw new MojoExecutionException(e1.getMessage(), e1);
                }
            }
        }

        try {//检测 pyang
            List<String> commandList= new ArrayList<>();
            if(pythonUsing){
                if(linux){
                    commandList.add("pyang");
                }else{
                    commandList.add("pyang");//待调整
                }
            }else{
                commandList.add(jython.getAbsolutePath());
                commandList.add(pyang.getAbsolutePath());
            }
            commandList.add("-v");
            ProcessUtil.process(showWarnings,commandList);
        } catch (Exception e) {
            try {//安装pyang
                getLog().info("pyang is not installed. Start installation");
                PyangInstall.copy(jythonHome);
                if(pythonUsing){
                    ProcessUtil.process(showWarnings,pyangSource,"python", new File(pyangSource,"setup.py").getAbsolutePath(), "install");
                    ProcessUtil.process(showWarnings,"pyang", "-v");
                }else{
                    ProcessUtil.process(showWarnings,pyangSource,jython.getAbsolutePath(), new File(pyangSource,"setup.py").getAbsolutePath(), "install");
                    ProcessUtil.process(showWarnings,jython.getAbsolutePath(), pyang.getAbsolutePath(), "-v");
                }
            } catch (Exception e1) {
                getLog().error("install pyang fail", e1);
                throw new MojoExecutionException(e1.getMessage(), e1);
            }
        }


        // make sure to add compileSourceRoots also during configuration build in m2e context

        project.addCompileSourceRoot(getOutputDirectory().getAbsolutePath());


        Set<File> yangSourceDirectories = getYangSourceDirectories();

        getLog().debug("Using build context: " + buildContext);
        List<String> yangImportRoots = getYangImportRoots();

        File yangSourceRoot = getYangSourceRoot();

        Codegen codegen = new Codegen(yangSourceRoot, getOutputDirectory(), packageName);

        try {
            List<String> yangList = new ArrayList<>();

            Set<File> files = filterFiles(yangSourceDirectories);
            for (File f : files) {
                yangList.add(f.getAbsolutePath());
            }
            getLog().info("yang files count = "+yangList.size()+" to be converted");
            codegen.setYangList(yangList);
            codegen.setYangImportList(yangImportRoots);
            codegen.generatorCode(pythonUsing,showWarnings,jython.getAbsolutePath(),pyang.getAbsolutePath());
        } catch (Exception e) {
            getLog().error("execute error", e);
            throw new MojoExecutionException(e.getMessage(), e);
        }

//        StandardJavaFileManager fileManager = null;
//
//        try {
//            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//            if (compiler == null) {
//                throw new MojoExecutionException("You need to run build with JDK or have tools.jar on the classpath."
//                        + "If this occures during eclipse build make sure you run eclipse under JDK as well");
//            }
//            List<String> yangImportRoots = getYangImportRoots();
//            Set<File> files = filterFiles(yangSourceDirectories);
//            if (files.isEmpty()) {
//                getLog().debug("No Yang sources found (skipping)");
//                return;
//            }
//
//            fileManager = compiler.getStandardFileManager(null, null, null);
//            Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(files);
//            // clean all markers
//            for (JavaFileObject javaFileObject : compilationUnits1) {
//                buildContext.removeMessages(new File(javaFileObject.toUri().getPath()));
//            }
//
//            String compileClassPath = buildCompileClasspath();
//
//            String processor = buildProcessor();
//
//            String outputDirectory = getOutputDirectory().getPath();
//            File tempDirectory = null;
//
//            if (buildContext.isIncremental()) {
//                tempDirectory = new File(project.getBuild().getDirectory(), "apt" + System.currentTimeMillis());
//                tempDirectory.mkdirs();
//                outputDirectory = tempDirectory.getAbsolutePath();
//            }
//
//            List<String> compilerOptions = buildCompilerOptions(processor, compileClassPath, outputDirectory);
//
//            Writer out = null;
//            if (logOnlyOnError) {
//                out = new StringWriter();
//            }
//            ExecutorService executor = Executors.newSingleThreadExecutor();
//            try {
//                DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
//                CompilationTask task = compiler.getTask(out, fileManager, diagnosticCollector, compilerOptions, null, compilationUnits1);
//                Future<Boolean> future = executor.submit(task);
//                Boolean rv = future.get();
//
//                if (Boolean.FALSE.equals(rv) && logOnlyOnError) {
//                    getLog().error(out.toString());
//                }
//                processDiagnostics(diagnosticCollector.getDiagnostics());
//            } finally {
//                executor.shutdown();
//                if (tempDirectory != null) {
//                    FileSync.syncFiles(tempDirectory, getOutputDirectory());
//                    FileUtils.deleteDirectory(tempDirectory);
//                }
//            }
//
//            buildContext.refresh(getOutputDirectory());
//        } catch (Exception e1) {
//            getLog().error("execute error", e1);
//            throw new MojoExecutionException(e1.getMessage(), e1);
//
//        } finally {
//            if (fileManager != null) {
//                try {
//                    fileManager.close();
//                } catch (Exception e) {
//                    getLog().warn("Unable to close fileManager", e);
//                }
//            }
//        }
    }

    protected abstract File getOutputDirectory();

    @SuppressWarnings("unchecked")
    protected Set<File> getSourceDirectories() {
        File outputDirectory = getOutputDirectory();
        String outputPath = outputDirectory.getAbsolutePath();
        Set<File> directories = new HashSet<File>();
        List<String> directoryNames = isForTest() ? getTestCompileSourceRoots()
                : getCompileSourceRoots();
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
        String outputPath = outputDirectory.getAbsolutePath();
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

    private File getYangSourceRoot() {
        return new File(project.getBasedir(), "src/main/yang");
    }

    private List<String> getYangImportRoots() {
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

    protected boolean isForTest() {
        return false;
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

}
