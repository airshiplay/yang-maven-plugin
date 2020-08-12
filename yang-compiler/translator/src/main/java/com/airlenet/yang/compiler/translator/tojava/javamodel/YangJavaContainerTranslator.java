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

import com.airlenet.yang.compiler.datamodel.*;
import com.airlenet.yang.compiler.datamodel.javadatamodel.*;
import com.airlenet.yang.compiler.datamodel.utils.builtindatatype.YangDataTypes;
import com.airlenet.yang.compiler.translator.exception.TranslatorException;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGenerator;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGeneratorInfo;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoTranslator;
import com.airlenet.yang.compiler.translator.tojava.TempJavaCodeFragmentFiles;
import com.airlenet.yang.compiler.translator.tojava.jnc.JNCCodeUtil;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaClass;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaMethod;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.tailf.jnc.YangElement;

import java.io.IOException;
import java.util.List;

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

        List<YangLeaf> children = this.getListOfLeaf();
        for (YangLeaf yangLeaf : children) {
            if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION|| yangLeaf.getDataType().getDataType() == YangDataTypes.ENUMERATION) {
                ((YangJavaLeafTranslator) yangLeaf).updateJavaQualifiedInfo();
                ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
            }
        }
        for (YangLeafList yangLeaf : this.getListOfLeafList()) {
            if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION|| yangLeaf.getDataType().getDataType() == YangDataTypes.ENUMERATION) {
                ((YangJavaLeafListTranslator) yangLeaf).updateJavaQualifiedInfo();
                ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
            }
        }

        YangNode child = getChild();
        while (child != null) {

            if (child instanceof YangJavaChoice) {
                YangNode childChild = child.getChild();
                while (childChild != null) {
                    List<YangLeaf> listOfLeaf = ((YangJavaCase) childChild).getListOfLeaf();
                    for (YangLeaf yangLeaf : listOfLeaf) {
                        ((YangJavaLeafTranslator) yangLeaf).updateJavaQualifiedInfo();
                        ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
                    }
                    childChild = childChild.getNextSibling();
                }
            }
            child = child.getNextSibling();
        }
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
        JavaFileInfoTranslator fileInfo = this.getJavaFileInfo();
        JavaClass javaClass = new JavaClass(classname, this.getJavaFileInfo().getPackage(),
                "Code generated by "+this.getClass().getSimpleName() +
                        "\n * <p>"+
                        "\n * See line "+fileInfo.getLineNumber()+" in" +
                        "\n * "+fileInfo.getYangFileName()+
                        "\n * "+
                        "\n * @author Auto Generated");
        YangJavaModule yangJavaModule = (YangJavaModule) this.getYangJavaModule();


        javaClass.setExtend("com.tailf.jnc.YangElement");
        javaClass.addMethod(new JavaMethod(javaClass.getName(), "").setModifiers("public").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName())
                .addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");")
                .addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName())
                .addLine("setDefaultPrefix();")
                .addLine("setPrefix(" + yangJavaModule.getPrefixClassName() + ".PREFIX);"));

        JNCCodeUtil.keyNamesMethod(javaClass, null);
        JNCCodeUtil.childrenNamesMethod(javaClass, getListOfLeaf(), getListOfLeafList(), getChild());

        JNCCodeUtil.cloneMethod(javaClass, null);

        JNCCodeUtil.cloneShallowMethod(javaClass, null);
        for (YangLeaf yangLeaf : getListOfLeaf()) {

            JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);

        }
        for (YangLeafList yangLeafList : this.getListOfLeafList()) {
            JNCCodeUtil.yangLeafListMethod(javaClass, yangJavaModule, yangLeafList);
        }
        YangNode child = getChild();
        while (child != null) {
            if (child instanceof YangJavaUnion||child instanceof YangJavaUses ||child instanceof YangJavaGrouping ||child instanceof YangJavaEnumeration ||child instanceof YangTypeDef) {

            } else if (child instanceof YangJavaList) {
//                JNCCodeUtil.yangNodeMethond(javaClass, child);
                JNCCodeUtil.yangJavaListMethod(javaClass, (YangJavaList) child);
            } else if (child instanceof YangJavaContainer) {
                JNCCodeUtil.yangNodeMethond(javaClass, child,true);
                JNCCodeUtil.yangJavaContainerMethod(javaClass, (YangJavaContainer) child);
            } else if (child instanceof YangJavaChoice) {
                YangNode childChild = child.getChild();
                while (childChild != null) {
                    List<YangLeaf> listOfLeaf = ((YangJavaCase) childChild).getListOfLeaf();
                    for (YangLeaf yangLeaf : listOfLeaf) {
                        JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);
                    }
                    childChild = childChild.getNextSibling();
                }
            } else {
                JNCCodeUtil.yangNodeMethond(javaClass, child,true);
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
