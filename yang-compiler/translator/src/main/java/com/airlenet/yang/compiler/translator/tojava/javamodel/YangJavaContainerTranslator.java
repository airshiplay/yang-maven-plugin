/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.airlenet.yang.compiler.translator.tojava.javamodel;

import com.airlenet.yang.compiler.datamodel.YangLeaf;
import com.airlenet.yang.compiler.datamodel.YangModule;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaContainer;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaLeaf;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaList;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaModule;
import com.airlenet.yang.compiler.translator.exception.TranslatorException;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGenerator;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGeneratorInfo;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoTranslator;
import com.airlenet.yang.compiler.translator.tojava.TempJavaCodeFragmentFiles;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaClass;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaMethod;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.YangElement;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.airlenet.yang.compiler.translator.tojava.GeneratedJavaFileType.GENERATE_INTERFACE_WITH_BUILDER;
import static com.airlenet.yang.compiler.translator.tojava.YangJavaModelUtils.*;
import static com.airlenet.yang.compiler.translator.tojava.utils.JavaIdentifierSyntax.getRootPackage;

/**
 * Represents container information extended to support java code generation.
 */
public class YangJavaContainerTranslator
        extends YangJavaContainer
        implements JavaCodeGeneratorInfo, JavaCodeGenerator {

    private static final long serialVersionUID = 806201630L;

    /**
     * File handle to maintain temporary java code fragments as per the code
     * snippet types.
     */
    private transient TempJavaCodeFragmentFiles tempFileHandle;

    /**
     * Creates YANG java container object.
     */
    public YangJavaContainerTranslator() {
        super();
        setJavaFileInfo(new JavaFileInfoTranslator());
        getJavaFileInfo().setGeneratedFileTypes(GENERATE_INTERFACE_WITH_BUILDER);
    }

    /**
     * Returns the generated java file information.
     *
     * @return generated java file information
     */
    @Override
    public JavaFileInfoTranslator getJavaFileInfo() {
        if (javaFileInfo == null) {
            throw new TranslatorException("Missing java info in java datamodel node " +
                    getName() + " in " +
                    getLineNumber() + " at " +
                    getCharPosition()
                    + " in " + getFileName());
        }
        return (JavaFileInfoTranslator) javaFileInfo;
    }

    /**
     * Sets the java file info object.
     *
     * @param javaInfo java file info object
     */
    @Override
    public void setJavaFileInfo(JavaFileInfoTranslator javaInfo) {
        javaFileInfo = javaInfo;
    }

    /**
     * Returns the temporary file handle.
     *
     * @return temporary file handle
     */
    @Override
    public TempJavaCodeFragmentFiles getTempJavaCodeFragmentFiles() {
        return tempFileHandle;
    }

    /**
     * Sets temporary file handle.
     *
     * @param fileHandle temporary file handle
     */
    @Override
    public void setTempJavaCodeFragmentFiles(TempJavaCodeFragmentFiles fileHandle) {
        tempFileHandle = fileHandle;
    }

    /**
     * Prepare the information for java code generation corresponding to YANG
     * container info.
     *
     * @param yangPlugin YANG plugin config
     * @throws TranslatorException translator operation fail
     */
    @Override
    public void generateCodeEntry(YangPluginConfig yangPlugin) throws TranslatorException {
        updateJNCPackageInfo(this, yangPlugin);


//        try {
//            generateCodeAndUpdateInParent(this, yangPlugin, false);
//        } catch (IOException e) {
//            throw new TranslatorException(
//                    "Failed to prepare generate code entry for container node " +
//                            getName() + " in " +
//                            getLineNumber() + " at " +
//                            getCharPosition()
//                            + " in " + getFileName() + " " + e.getLocalizedMessage());
//        }
    }

    /**
     * Create a java file using the YANG container info.
     *
     * @throws TranslatorException translator operation fail
     */
    @Override
    public void generateCodeExit() throws TranslatorException {

        String classname = YangElement.normalize(this.getName());

        JavaClass javaClass = new JavaClass(classname, this.getJavaFileInfo().getPackage(), "");
        YangJavaModule yangJavaModule = (YangJavaModule) this.getRoot();

        javaClass.setExtend("com.tailf.jnc.YangElement");
        javaClass.addMethod(new JavaMethod(javaClass.getName(), "").setModifiers("public").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName())
                .addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");")
                .addLine("setDefaultPrefix();")
                .addLine("setPrefix(" + yangJavaModule.getPrefixClassName() + ".PREFIX);"));
        {
            JavaMethod childrenNames = new JavaMethod("childrenNames", "String[]");
            childrenNames.setModifiers("public")
                    .addLine("return new String[]{");
            YangNode child = getChild();
            while (child != null) {
                childrenNames.addLine("\t\t\"" + child.getName() + "\",");
                child = child.getNextSibling();
            }
            childrenNames.addLine("};");
            javaClass.addMethod(childrenNames);
        }
        javaClass.addMethod(new JavaMethod("keyNames", "String[]").setModifiers("public")
                .addLine("return null;")
        );

        javaClass.addMethod(new JavaMethod("cloneShallow", "com.tailf.jnc.Element").setModifiers("public")
                .addLine("return null;")
        );


        YangNode child = getChild();
        while (child != null) {

            {
                JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public");
                addMethod.setExceptions("JNCException");

                addMethod.addLine(YangElement.normalize(child.getName()) + " " +  YangElement.camelize(child.getName()) + "= new " + YangElement.normalize(child.getName()) + "();");
                addMethod.addLine("insertChild(" + YangElement.camelize(child.getName()) + ", childrenNames());");
                addMethod.addLine("return " + YangElement.camelize(child.getName() ) + ";");
                javaClass.addMethod(addMethod);

            }
            {
                JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public");
                addMethod.setExceptions("JNCException");
                addMethod.addDependency(child.getJavaPackage() + "." + YangElement.normalize(child.getName()));
                addMethod.addDependency(JNCException.class.getName());
                addMethod.addParameter(YangElement.normalize(child.getName()), YangElement.camelize(child.getName() ));
                addMethod.addLine("insertChild(" +  YangElement.camelize(child.getName()) + ", childrenNames());");
                addMethod.addLine("return " + YangElement.camelize(child.getName() ) + ";");
                javaClass.addMethod(addMethod);
            }

            if (child instanceof YangJavaList) {
                List<YangLeaf> listOfKeyLeaf = ((YangJavaList) child).getListOfKeyLeaf();
                if (!listOfKeyLeaf.isEmpty()) {

                    JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public");
                    addMethod.setExceptions("JNCException");
                    for (YangLeaf keyLeaf : listOfKeyLeaf) {
                        addMethod.addParameter(AttributesJavaDataType.getJNCDataType(keyLeaf.getDataType()), YangElement.camelize(keyLeaf.getName() + "Value"));
                    }
                    addMethod.addLine(YangElement.normalize(child.getName()) + " " + YangElement.camelize(child.getName() ) + " = new "
                            + YangElement.normalize(child.getName()) + "(" + listOfKeyLeaf.stream().map(k -> k.getName() + "Value").collect(Collectors.joining(",")) + ");");
                    addMethod.addLine("return " + "add" + YangElement.normalize(child.getName()) + "(" +YangElement.camelize(child.getName() ) + ");");
                    javaClass.addMethod(addMethod);


                    addMethod = new JavaMethod("add" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public");
                    addMethod.setExceptions("JNCException");
                    for (YangLeaf keyLeaf : listOfKeyLeaf) {
                        addMethod.addParameter("String", keyLeaf.getName() + "Value");
                    }
                    addMethod.addLine(YangElement.normalize(child.getName()) + " " +  YangElement.camelize(child.getName() ) + " = new "
                            + YangElement.normalize(child.getName()) + "(" + listOfKeyLeaf.stream().map(k -> k.getName() + "Value").collect(Collectors.joining(",")) + ");");
                    addMethod.addLine("return " + "add" + YangElement.normalize(child.getName()) + "(" + YangElement.camelize(child.getName() ) + ");");
                    javaClass.addMethod(addMethod);


//                    public void deleteEmployee(com.tailf.jnc.YangUInt32 idValue)
//            throws JNCException {
//                        String path = "employee[id='" + idValue + "']";
//                        delete(path);
//                    }
                    {
                        JavaMethod getMethod = new JavaMethod("get" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public").setExceptions("JNCException");

                        for (YangLeaf keyLeaf : listOfKeyLeaf) {
                            getMethod.addParameter(AttributesJavaDataType.getJNCDataType(keyLeaf.getDataType()), keyLeaf.getName() + "Value");
                        }
                        getMethod.addLine("String path = \"" + child.getName() + listOfKeyLeaf.stream().map(k -> "[" + k.getName() + "='\"+" + k.getName() + "Value" + "+\"']").collect(Collectors.joining("")) + "\";");
                        getMethod.addLine("return ("+YangElement.normalize(child.getName())+")searchOne(path);");
                        javaClass.addMethod(getMethod);
                    }
                    {
                        JavaMethod getMethod = new JavaMethod("get" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public").setExceptions("JNCException");
                        for (YangLeaf keyLeaf : listOfKeyLeaf) {
                            getMethod.addParameter("String", keyLeaf.getName() + "Value");
                        }
                        getMethod.addLine("String path = \"" + child.getName() + listOfKeyLeaf.stream().map(k -> "[" + k.getName() + "='\"+" + k.getName() + "Value" + "+\"']").collect(Collectors.joining("")) + "\";");
                        getMethod.addLine("return ("+YangElement.normalize(child.getName())+")searchOne(path);");
                        javaClass.addMethod(getMethod);
                    }
                    {
                        JavaMethod delelteMethod = new JavaMethod("delete" + YangElement.normalize(child.getName()), "void").setModifiers("public").setExceptions("JNCException");
                        for (YangLeaf keyLeaf : listOfKeyLeaf) {
                            delelteMethod.addParameter(AttributesJavaDataType.getJNCDataType(keyLeaf.getDataType()), keyLeaf.getName() + "Value");
                        }
                        delelteMethod.addLine("String path = \"" + child.getName() + listOfKeyLeaf.stream().map(k -> "[" + k.getName() + "='\"+" + k.getName() + "Value" + "+\"']").collect(Collectors.joining("")) + "\";");
                        delelteMethod.addLine("delete(path);");
                        javaClass.addMethod(delelteMethod);
                    }
                    {
                        JavaMethod delelteMethod = new JavaMethod("delete" + YangElement.normalize(child.getName()), "void").setModifiers("public").setExceptions("JNCException");
                        for (YangLeaf keyLeaf : listOfKeyLeaf) {
                            delelteMethod.addParameter("String", keyLeaf.getName() + "Value");
                        }
                        delelteMethod.addLine("String path = \"" + child.getName() + listOfKeyLeaf.stream().map(k -> "[" + k.getName() + "='\"+" + k.getName() + "Value" + "+\"']").collect(Collectors.joining("")) + "\";");
                        delelteMethod.addLine("delete(path);");
                        javaClass.addMethod(delelteMethod);
                    }
                }
            }


            child = child.getNextSibling();
        }

        try {
            javaClass.write(this.getJavaFileInfo().getBaseCodeGenPath() + this.getJavaFileInfo().getPackageFilePath());
        } catch (IOException e) {
            throw new TranslatorException(e);
        }
//        try {
//            generateJava(GENERATE_INTERFACE_WITH_BUILDER, this);
//        } catch (IOException e) {
//            throw new TranslatorException("Failed to generate code for container node " +
//                                                  getName() + " in " +
//                                                  getLineNumber() + " at " +
//                                                  getCharPosition()
//                                                  + " in " + getFileName() + " " + e.getLocalizedMessage());
//        }
    }
}
