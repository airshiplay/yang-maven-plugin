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
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoContainer;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoTranslator;
import com.airlenet.yang.compiler.translator.tojava.JavaQualifiedTypeInfoTranslator;
import com.airlenet.yang.compiler.translator.tojava.TempJavaCodeFragmentFiles;
import com.airlenet.yang.compiler.translator.tojava.TempJavaCodeFragmentFilesContainer;
import com.airlenet.yang.compiler.translator.tojava.TempJavaServiceFragmentFiles;
import com.airlenet.yang.compiler.translator.tojava.jnc.JNCCodeUtil;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaClass;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaMethod;
import com.airlenet.yang.compiler.translator.tojava.utils.JavaExtendsListHolder;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.tailf.jnc.YangElement;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.airlenet.yang.compiler.translator.tojava.GeneratedJavaFileType.GENERATE_INTERFACE_WITH_BUILDER;
import static com.airlenet.yang.compiler.translator.tojava.YangJavaModelUtils.generateCodeOfAugmentableNode;
import static com.airlenet.yang.compiler.translator.tojava.YangJavaModelUtils.updateJNCPackageInfo;
import static com.airlenet.yang.compiler.translator.tojava.utils.JavaIdentifierSyntax.getEnumJavaAttribute;
import static com.airlenet.yang.compiler.translator.tojava.utils.TranslatorErrorType.FAIL_AT_ENTRY;
import static com.airlenet.yang.compiler.translator.tojava.utils.TranslatorErrorType.FAIL_AT_EXIT;
import static com.airlenet.yang.compiler.translator.tojava.utils.TranslatorErrorType.INVALID_NODE;
import static com.airlenet.yang.compiler.translator.tojava.utils.TranslatorUtils.getErrorMsg;
import static com.airlenet.yang.compiler.utils.UtilConstants.EVENT_LISTENER_STRING;
import static com.airlenet.yang.compiler.utils.UtilConstants.EVENT_STRING;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.getCapitalCase;

/**
 * Represents notification information extended to support java code generation.
 */
public class YangJavaNotificationTranslator
        extends YangJavaNotification
        implements JavaCodeGenerator, JavaCodeGeneratorInfo {

    private static final long serialVersionUID = 806201624L;

    /**
     * File handle to maintain temporary java code fragments as per the code
     * snippet types.
     */
    private transient TempJavaCodeFragmentFiles tempFileHandle;

    /**
     * Creates an instance of java Notification.
     */
    public YangJavaNotificationTranslator() {
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
            throw new TranslatorException(getErrorMsg(INVALID_NODE, this));
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
     * notification info.
     *
     * @param yangPlugin YANG plugin config
     * @throws TranslatorException translator operation fail
     */
    @Override
    public void generateCodeEntry(YangPluginConfig yangPlugin)
            throws TranslatorException {

        // Obtain the notification name as per enum in notification.
        String enumName = getEnumJavaAttribute(getName().toUpperCase());
        ((RpcNotificationContainer) getParent())
                .addToNotificationEnumMap(enumName, this);

        /*
         * As part of the notification support the following files needs to be
         * generated.
         * 1) Subject of the notification(event), this is simple interface with
         * builder class.
         * 2) Event class extending "AbstractEvent" and defining event type
         * enum.
         * 3) Event listener interface extending "EventListener".
         *
         * The manager class needs to extend the ListenerRegistry.
         */

        // Generate subject of the notification(event), this is simple interface
        // with builder class.
        updateJNCPackageInfo(this, yangPlugin);


        List<YangLeaf> children = this.getListOfLeaf();
        for (YangLeaf yangLeaf : children) {
            if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION || yangLeaf.getDataType().getDataType() == YangDataTypes.ENUMERATION) {
                ((YangJavaLeafTranslator) yangLeaf).updateJavaQualifiedInfo();
                ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
            }
        }
        for (YangLeafList yangLeaf : this.getListOfLeafList()) {
            if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION || yangLeaf.getDataType().getDataType() == YangDataTypes.ENUMERATION) {
                ((YangJavaLeafListTranslator) yangLeaf).updateJavaQualifiedInfo();
                ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
            }
        }

        for (YangNode yangAugment : getAugmentedInfoList()) {
            List<YangLeaf> augmentListOfLeaf = ((YangJavaAugmentTranslator) yangAugment).getListOfLeaf();

            for (YangLeaf yangLeaf : augmentListOfLeaf) {
                if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION || yangLeaf.getDataType().getDataType() == YangDataTypes.ENUMERATION) {
                    ((YangJavaLeafTranslator) yangLeaf).updateJavaQualifiedInfo();
                    ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
                }
            }
            YangNode augmentedNode = yangAugment.getChild();
            while (augmentedNode != null) {

                if (!(augmentedNode instanceof YangJavaUsesTranslator)) {
                    ((JavaCodeGenerator) augmentedNode).generatePackageInfo(yangPlugin);
                }

                if (augmentedNode instanceof YangJavaListTranslator) {
                    List<YangLeaf> listOfKeyLeaf = ((YangJavaListTranslator) augmentedNode).getListOfKeyLeaf();
                    for (YangLeaf yangLeaf : listOfKeyLeaf) {
                        if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION || yangLeaf.getDataType().getDataType() == YangDataTypes.ENUMERATION) {
                            ((YangJavaLeafTranslator) yangLeaf).updateJavaQualifiedInfo();
                            ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
                        }
                    }
                }

                augmentedNode = augmentedNode.getNextSibling();

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

                    List<YangLeafList> listOfLeafList = ((YangJavaCase) childChild).getListOfLeafList();
                    for (YangLeafList yangLeafList : listOfLeafList) {
                        ((YangJavaLeafListTranslator) yangLeafList).updateJavaQualifiedInfo();
                        ((YangJavaTypeTranslator) yangLeafList.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
                    }
                    childChild = childChild.getNextSibling();
                }
            }
            child = child.getNextSibling();
        }

        List<YangAugment> yangAugmentList = getAugmentedInfoList();

        for (YangAugment yangAugment : yangAugmentList) {
            YangNode augmentedNode = yangAugment.getChild();
            if (augmentedNode != null) {
                if (augmentedNode instanceof YangJavaUsesTranslator) {
                    augmentedNode = augmentedNode.getNextSibling();
                }

                ((JavaCodeGenerator) augmentedNode).generatePackageInfo(yangPlugin);
            }
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

    /*Adds current notification info to the extends list so its parents service*/
    private void addNotificationToExtendsList() {
        YangNode parent = getParent();
        TempJavaServiceFragmentFiles tempFiles =
                ((TempJavaCodeFragmentFilesContainer) parent).getTempJavaCodeFragmentFiles()
                        .getServiceTempFiles();
        JavaExtendsListHolder holder = tempFiles.getJavaExtendsListHolder();
        JavaQualifiedTypeInfoTranslator event =
                new JavaQualifiedTypeInfoTranslator();

        String parentInfo = getCapitalCase(((JavaFileInfoContainer) parent)
                .getJavaFileInfo()
                .getJavaName());
        event.setClassInfo(parentInfo + EVENT_STRING);
        event.setPkgInfo(getJavaFileInfo().getPackage());
        holder.addToExtendsList(event, parent, tempFiles);

        JavaQualifiedTypeInfoTranslator eventListener =
                new JavaQualifiedTypeInfoTranslator();

        eventListener.setClassInfo(parentInfo + EVENT_LISTENER_STRING);
        eventListener.setPkgInfo(getJavaFileInfo().getPackage());
        holder.addToExtendsList(eventListener, parent, tempFiles);
    }

    /**
     * Creates a java file using the YANG notification info.
     */
    @Override
    public void generateCodeExit()
            throws TranslatorException {

        String classname = YangElement.normalizeClass(this.getName());
        JavaFileInfoTranslator fileInfo = this.getJavaFileInfo();
        JavaClass javaClass = new JavaClass(classname, this.getJavaFileInfo().getPackage(),
                "Code generated by " + this.getClass().getSimpleName() +
                        "\n * <p>" +
                        "\n * See line " + fileInfo.getLineNumber() + " in" +
                        "\n * " + fileInfo.getYangFileName().replace("\\", "/") +
                        "\n * " +
                        "\n * @author Auto Generated");
        YangJavaModule yangJavaModule = (YangJavaModule) this.getYangJavaModule();


        javaClass.setExtend("com.tailf.jnc.YangElement");
        JavaMethod emptyConstructorMethod = new JavaMethod(javaClass.getName(), "").setModifiers("public").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName())
                .addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");")
                .addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName());
        YangJavaModule parentYangJavaModule = (YangJavaModule) getParent().getYangJavaModule();
        if (parentYangJavaModule == null || getParent() instanceof YangJavaModule || !parentYangJavaModule.getPrefixClassName().equals(yangJavaModule.getPrefixClassName())) {
            emptyConstructorMethod.addLine("setDefaultPrefix();");
        }

        javaClass.addMethod(emptyConstructorMethod);


        JNCCodeUtil.keyNamesMethod(javaClass, null);
        JNCCodeUtil.childrenNamesMethod(javaClass, getListOfLeaf(), getListOfLeafList(), getChild(), getAugmentedInfoList());

        JNCCodeUtil.cloneMethod(javaClass, null);

        JNCCodeUtil.cloneShallowMethod(javaClass, null);
        for (YangLeaf yangLeaf : getListOfLeaf()) {

            JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);

        }
        List<YangLeaf> augmentLeafList = getAugmentedInfoList().stream().flatMap(a -> a.getListOfLeaf().stream()).collect(Collectors.toList());
        for (YangLeaf yangLeaf : augmentLeafList) {

            JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);

        }
        for (YangLeafList yangLeafList : this.getListOfLeafList()) {
            JNCCodeUtil.yangLeafListMethod(javaClass, yangJavaModule, yangLeafList);
        }
        YangNode child = getChild();
        while (child != null) {
            if (child instanceof YangJavaUnion || child instanceof YangJavaUses || child instanceof YangJavaGrouping
                    || child instanceof YangJavaEnumeration || child instanceof YangTypeDef
                    || child instanceof YangJavaAction || child instanceof YangJavaTailfAction) {

            } else if (child instanceof YangJavaList) {
//                JNCCodeUtil.yangNodeMethond(javaClass, child);
                JNCCodeUtil.yangJavaListMethod(javaClass, (YangJavaList) child);
            } else if (child instanceof YangJavaContainer) {
                JNCCodeUtil.yangNodeMethond(javaClass, child, true);
                JNCCodeUtil.yangJavaContainerMethod(javaClass, (YangJavaContainer) child);
            } else if (child instanceof YangJavaChoice) {
                YangNode childChild = child.getChild();
                while (childChild != null) {
                    YangNode childChildChild = childChild.getChild();
                    if (childChildChild != null) {
                        if (childChildChild instanceof YangJavaList) {
                            JNCCodeUtil.yangJavaListMethod(javaClass, (YangJavaList) childChildChild);
                        } else if (childChildChild instanceof YangJavaContainer) {
                            JNCCodeUtil.yangNodeMethond(javaClass, childChildChild, true);
                            JNCCodeUtil.yangJavaContainerMethod(javaClass, (YangJavaContainer) childChildChild);
                        } else if (childChildChild instanceof YangJavaUnion || childChildChild instanceof YangJavaUses || childChildChild instanceof YangJavaGrouping
                                || childChildChild instanceof YangJavaEnumeration || childChildChild instanceof YangTypeDef
                                || childChildChild instanceof YangJavaAction || childChildChild instanceof YangJavaTailfAction) {

                        } else {
                            System.out.println(childChildChild.getClass());
                        }
                    }
                    List<YangLeaf> listOfLeaf = ((YangJavaCase) childChild).getListOfLeaf();
                    for (YangLeaf yangLeaf : listOfLeaf) {
                        JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);
                    }
                    List<YangLeafList> listOfLeafList = ((YangJavaCase) childChild).getListOfLeafList();
                    for (YangLeafList yangLeafList : listOfLeafList) {
                        JNCCodeUtil.yangLeafListMethod(javaClass, yangJavaModule, yangLeafList);
                    }
                    childChild = childChild.getNextSibling();
                }
            } else {
                JNCCodeUtil.yangNodeMethond(javaClass, child, true);
            }
            child = child.getNextSibling();
        }
        List<YangAugment> yangAugmentList = getAugmentedInfoList();

        for (YangAugment yangAugment : yangAugmentList) {
            YangNode augmentedNode = yangAugment.getChild();
            if (augmentedNode == null) {
                continue;
            }

            do {
                if (augmentedNode instanceof YangJavaUsesTranslator || augmentedNode instanceof YangJavaUnionTranslator) {
                    continue;
                }
                JavaFileInfoTranslator augmentFileInfoTranslator = ((JavaFileInfoContainer) augmentedNode).getJavaFileInfo();


//                String filedName = YangElement.camelize(augmentedNode.getName());
                String augmentClassname = YangElement.normalizeClass(augmentedNode.getName());

                javaClass.addDependency(augmentFileInfoTranslator.getPackage() + "." + augmentClassname);

                if (augmentedNode instanceof YangJavaListTranslator) {
                    JNCCodeUtil.yangJavaListMethod(javaClass, (YangJavaListTranslator) augmentedNode);
                } else if (augmentedNode instanceof YangJavaContainerTranslator) {
                    JNCCodeUtil.yangJavaContainerMethod(javaClass, (YangJavaContainerTranslator) augmentedNode);
                    JNCCodeUtil.yangNodeMethond(javaClass, (YangJavaContainerTranslator) augmentedNode, true);
//                    javaClass.addField(new JavaField(augmentClassname, filedName, "null", "public").setJavadoc("See line " + augmentedNode.getLineNumber() + " in\n" +
//                            "" + ((JavaFileInfoContainer) augmentedNode).getJavaFileInfo().getYangFileName()));
//                    javaClass.addMethod(new JavaMethod("get" + augmentClassname, augmentClassname).setModifiers("public").addLine("return this." + filedName + ";"));
//                    javaClass.addMethod(new JavaMethod("add" + augmentClassname, augmentClassname).setModifiers("public").addParameter(augmentClassname, filedName).setExceptions(JNCException.class.getName())
//                            .addLine(" this." + filedName + " = " + filedName + ";").addLine("this.insertChild(" + filedName + ", this.childrenNames());").addLine("return this." + filedName + ";"));
//                    javaClass.addMethod(new JavaMethod("add" + augmentClassname, augmentClassname).setModifiers("public").setExceptions(JNCException.class.getName()).addLine(augmentClassname + " " + filedName + " = new " + augmentClassname + "();")
//                            .addLine(" this." + filedName + " = " + filedName + ";").addLine("this.insertChild(" + filedName + ", this.childrenNames());").addLine("return this." + filedName + ";"));
//
//                    javaClass.addMethod(new JavaMethod("delete" + augmentClassname, "void").setModifiers("public").setExceptions(JNCException.class.getName())
//                            .addLine("this." + filedName + " = null;").addLine("String path=\"" + augmentedNode.getName() + "\";").addLine("this.delete(path);"));
                } else {
                    System.out.println(augmentedNode.getClass());
                }


            } while ((augmentedNode = augmentedNode.getNextSibling()) != null);

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
