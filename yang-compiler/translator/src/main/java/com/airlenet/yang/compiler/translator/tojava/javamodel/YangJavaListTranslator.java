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
import com.airlenet.yang.compiler.translator.tojava.*;
import com.airlenet.yang.compiler.translator.tojava.jnc.JNCCodeUtil;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaClass;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaField;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaMethod;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.YangElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.airlenet.yang.compiler.translator.tojava.GeneratedJavaFileType.GENERATE_INTERFACE_WITH_BUILDER;
import static com.airlenet.yang.compiler.translator.tojava.YangJavaModelUtils.*;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.getAbsolutePackagePath;

/**
 * Represents YANG list information extended to support java code generation.
 */
public class YangJavaListTranslator
        extends YangJavaList
        implements JavaCodeGeneratorInfo, JavaCodeGenerator {

    private static final long serialVersionUID = 806201626L;

    /**
     * File handle to maintain temporary java code fragments as per the code
     * snippet types.
     */
    private transient TempJavaCodeFragmentFiles tempFileHandle;


    /**
     * Creates YANG java list object.
     */
    public YangJavaListTranslator() {
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

    @Override
    public void generatePackageInfo(YangPluginConfig yangPlugin) {
        if (this.getParent() != null && ((JavaCodeGeneratorInfo) this.getParent()).getJavaFileInfo().getPackage() == null) {
            ((JavaCodeGenerator) this.getParent()).generatePackageInfo(yangPlugin);
        }
        updateJNCPackageInfo(this, yangPlugin);
    }

    /**
     * Prepare the information for java code generation corresponding to YANG
     * list info.
     *
     * @param yangPlugin YANG plugin config
     * @throws TranslatorException translator operation fail
     */
    @Override
    public void generateCodeEntry(YangPluginConfig yangPlugin) throws TranslatorException {
        updateJNCPackageInfo(this, yangPlugin);


        List<YangLeaf> children = this.getListOfLeaf();
        for (YangLeaf yangLeaf : children) {
            if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION || yangLeaf.getDataType().getDataType() == YangDataTypes.ENUMERATION) {
                ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
                ((YangJavaLeafTranslator) yangLeaf).updateJavaQualifiedInfo();
            }
        }
        for (YangLeafList yangLeaf : this.getListOfLeafList()) {
            if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION || yangLeaf.getDataType().getDataType() == YangDataTypes.ENUMERATION) {
                ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
                ((YangJavaLeafListTranslator) yangLeaf).updateJavaQualifiedInfo();
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

                if (augmentedNode != null && !(augmentedNode instanceof YangJavaUsesTranslator)) {
                    ((JavaCodeGenerator) augmentedNode).generatePackageInfo(yangPlugin);
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
                    childChild = childChild.getNextSibling();
                }
            }
            child = child.getNextSibling();
        }
        List<YangAugment> yangAugmentList = getAugmentedInfoList();

//        while (child!=null){
//            if (child.getDataType().getDataType() == YangDataTypes.DERIVED || child.getDataType().getDataType() == YangDataTypes.UNION) {
//                ((YangJavaLeafListTranslator) child).updateJavaQualifiedInfo();
//                ((YangJavaTypeTranslator) child.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
//            }
//        }
//        try {
//            generateCodeAndUpdateInParent(this, yangPlugin, true);
//        } catch (IOException e) {
//            throw new TranslatorException(
//                    "Failed to prepare generate code entry for list node " +
//                            getName() + " in " +
//                            getLineNumber() + " at " +
//                            getCharPosition() +
//                            " in " + getFileName(), e);
//        }
    }

    /**
     * Creates a java file using the YANG list info.
     *
     * @throws TranslatorException translator operation fail
     */
    @Override
    public void generateCodeExit() throws TranslatorException {

        String classname = YangElement.normalize(this.getName());
        JavaFileInfoTranslator fileInfo = this.getJavaFileInfo();
        JavaClass javaClass = new JavaClass(classname, fileInfo.getPackage(),
                "Code generated by " + this.getClass().getSimpleName() +
                        "\n * <p>" +
                        "\n * See line " + fileInfo.getLineNumber() + " in" +
                        "\n * " + fileInfo.getYangFileName() +
                        "\n * " +
                        "\n * @author Auto Generated");
        String absoluteDirPath = getAbsolutePackagePath(fileInfo.getBaseCodeGenPath(),
                fileInfo.getPackageFilePath());
        YangJavaModule yangJavaModule = (YangJavaModule) this.getYangJavaModule();
        javaClass.setExtend("com.tailf.jnc.YangElement");


        //// empty constructor
        JavaMethod emptyConstructorMethod = new JavaMethod(javaClass.getName(), "").setModifiers("public").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName())
                .addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");");
        YangJavaModule parentYangJavaModule = (YangJavaModule) getParent().getYangJavaModule();
        if (parentYangJavaModule == null || getParent() instanceof YangJavaModule || !parentYangJavaModule.getPrefixClassName().equals(yangJavaModule.getPrefixClassName())) {
            emptyConstructorMethod.addLine("setDefaultPrefix();");
        }

        javaClass.addMethod(emptyConstructorMethod);

        List<YangLeaf> listOfLeaf = this.getListOfLeaf();

        List<YangLeaf> keys = getListOfKeyLeaf();

///constructor
        if (!keys.isEmpty()) {

            JavaMethod constructor = new JavaMethod(javaClass.getName(), "").addDependency("com.tailf.jnc.Leaf");
            constructor.addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");");
            for (YangLeaf yangLeaf : keys) {
                YangLeaf dataTypeYangLeaf = yangLeaf;
//            while (dataTypeYangLeaf.getReferredSchema()!=null){
//                dataTypeYangLeaf =dataTypeYangLeaf.getReferredSchema();
//            }
                String jncDataType = AttributesJavaDataType.getJNCDataType(dataTypeYangLeaf.getDataType());
                constructor.addParameter(jncDataType, YangElement.camelize(yangLeaf.getName() + "Value"));
                if (jncDataType.equals("com.tailf.jnc.YangIdentityref")) {
                    YangJavaModule javaModule = (YangJavaModule) ((YangIdentityRef) yangLeaf.getDataType().getDataTypeExtendedInfo()).getReferredIdentity().getYangJavaModule();
                    constructor.addLine("setPrefix(new com.tailf.jnc.Prefix(" + javaModule.getJavaPackage() + "." + javaModule.getPrefixClassName() + ".PREFIX,");
                    constructor.addLine("\t\t" + javaModule.getJavaPackage() + "." + javaModule.getPrefixClassName() + ".NAMESPACE));");
                    emptyConstructorMethod.addLine("setPrefix(new com.tailf.jnc.Prefix(" + javaModule.getJavaPackage() + "." + javaModule.getPrefixClassName() + ".PREFIX,");
                    emptyConstructorMethod.addLine("\t\t" + javaModule.getJavaPackage() + "." + javaModule.getPrefixClassName() + ".NAMESPACE));");
                }
                constructor.addLine("Leaf " + YangElement.camelize(yangLeaf.getName()) + " = new Leaf(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + yangLeaf.getName() + "\");");
                constructor.addLine(YangElement.camelize(yangLeaf.getName()) + ".setValue(" + YangElement.camelize(yangLeaf.getName() + "Value") + ");");


                constructor.addLine("insertChild(" + YangElement.camelize(yangLeaf.getName()) + ", childrenNames());");
            }
            constructor.setModifiers("public").setExceptions("JNCException").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName());
            javaClass.addMethod(constructor);


            constructor = new JavaMethod(javaClass.getName(), "").addDependency("com.tailf.jnc.Leaf");
            constructor.addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");");
            for (YangLeaf yangLeaf : keys) {
                YangLeaf dataTypeYangLeaf = yangLeaf;
                constructor.addParameter("String", YangElement.camelize(yangLeaf.getName() + "Value"));
                String jncDataType = AttributesJavaDataType.getJNCDataType(dataTypeYangLeaf.getDataType());
                if (jncDataType.equals("com.tailf.jnc.YangIdentityref")) {
                    YangJavaModule javaModule = (YangJavaModule) ((YangIdentityRef) yangLeaf.getDataType().getDataTypeExtendedInfo()).getReferredIdentity().getYangJavaModule();
                    constructor.addLine("setPrefix(new com.tailf.jnc.Prefix(" + javaModule.getJavaPackage() + "." + javaModule.getPrefixClassName() + ".PREFIX,");
                    constructor.addLine("\t\t" + javaModule.getJavaPackage() + "." + javaModule.getPrefixClassName() + ".NAMESPACE));");
                }
                constructor.addLine("Leaf " + YangElement.camelize(yangLeaf.getName()) + " = new Leaf(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + yangLeaf.getName() + "\");");

//                while (dataTypeYangLeaf.getReferredSchema()!=null){
//                    dataTypeYangLeaf =dataTypeYangLeaf.getReferredSchema();
//                }
                constructor.addLine(YangElement.camelize(yangLeaf.getName()) + ".setValue(new " + AttributesJavaDataType.getJNCDataType(dataTypeYangLeaf.getDataType()) + "(" + YangElement.camelize(yangLeaf.getName() + "Value") + "));");
                constructor.addLine("insertChild(" + YangElement.camelize(yangLeaf.getName()) + ", childrenNames());");
            }
            constructor.setModifiers("public").setExceptions("JNCException").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName());
            javaClass.addMethod(constructor);

        }


/////keyNames
        JNCCodeUtil.keyNamesMethod(javaClass, getListOfKeyLeaf());

////childrenNames
        JNCCodeUtil.childrenNamesMethod(javaClass, getListOfLeaf(), getListOfLeafList(), getChild(), getAugmentedInfoList());

////clone
        JNCCodeUtil.cloneMethod(javaClass, keys);

////cloneShallow
        JNCCodeUtil.cloneShallowMethod(javaClass, keys);

        ////

        for (YangLeaf yangLeaf : listOfLeaf) {
            JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);
        }
        List<YangLeaf> augmentLeafList = getAugmentedInfoList().stream().flatMap(a -> a.getListOfLeaf().stream()).collect(Collectors.toList());
        for (YangLeaf yangLeaf : augmentLeafList) {

            JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);

        }
        for (YangLeafList yangLeafList : this.getListOfLeafList()) {
            JNCCodeUtil.yangLeafListMethod(javaClass, yangJavaModule, yangLeafList);
        }

        {
            YangNode child = this.getChild();
            while (child != null) {
                if (child instanceof YangJavaUnion || child instanceof YangJavaUses || child instanceof YangJavaGrouping || child instanceof YangJavaEnumeration
                        || child instanceof YangJavaAction || child instanceof YangJavaTailfAction) {

                } else if (child instanceof YangJavaList) {
//                JNCCodeUtil.yangNodeMethond(javaClass, child);
                    JNCCodeUtil.yangJavaListMethod(javaClass, (YangJavaList) child);
                } else if (child instanceof YangJavaContainer) {
                    JNCCodeUtil.yangNodeMethond(javaClass, child, false);
                    JNCCodeUtil.yangJavaContainerMethod(javaClass, (YangJavaContainer) child);
                } else if (child instanceof YangJavaChoice) {
                    YangNode childChild = child.getChild();
                    while (childChild != null) {
                        List<YangLeaf> listOfLeafChild = ((YangJavaCase) childChild).getListOfLeaf();
                        for (YangLeaf yangLeaf : listOfLeafChild) {
                            JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);
                        }
                        childChild = childChild.getNextSibling();
                    }
                } else if (child instanceof YangJavaAnydata) {
                    JNCCodeUtil.yangJavaAnydataMethod(javaClass, yangJavaModule, (YangJavaAnydata) child);
                } else {
                    JNCCodeUtil.yangNodeMethond(javaClass, child, false);
                }
                child = child.getNextSibling();
            }
        }
        for (YangAugment yangAugment : getAugmentedInfoList()) {
            YangNode augmentedNode = yangAugment.getChild();
            if (augmentedNode == null) {
                continue;
            }

            do {
                if(augmentedNode instanceof YangJavaUsesTranslator ||augmentedNode instanceof YangJavaUnionTranslator){
                    continue;
                }
                JavaFileInfoTranslator augmentFileInfoTranslator = ((JavaFileInfoContainer) augmentedNode).getJavaFileInfo();


                String filedName = YangElement.camelize(augmentedNode.getName());
                String augmentClassname = YangElement.normalize(augmentedNode.getName());

                javaClass.addDependency(augmentFileInfoTranslator.getPackage() + "." + augmentClassname);

                javaClass.addField(new JavaField(augmentClassname, filedName, "null", "public").setJavadoc("See line " + augmentedNode.getLineNumber() + " in\n" +
                        "" + ((JavaFileInfoContainer) augmentedNode).getJavaFileInfo().getYangFileName()));
                javaClass.addMethod(new JavaMethod("get" + augmentClassname, augmentClassname).setModifiers("public").addLine("return this." + filedName + ";"));
                javaClass.addMethod(new JavaMethod("add" + augmentClassname, augmentClassname).setModifiers("public").addParameter(augmentClassname, filedName).setExceptions(JNCException.class.getName())
                        .addLine(" this." + filedName + " = " + filedName + ";").addLine("this.insertChild(" + filedName + ", this.childrenNames());").addLine("return this." + filedName + ";"));
                javaClass.addMethod(new JavaMethod("add" + augmentClassname, augmentClassname).setModifiers("public").setExceptions(JNCException.class.getName()).addLine(augmentClassname + " " + filedName + " = new " + augmentClassname + "();")
                        .addLine(" this." + filedName + " = " + filedName + ";").addLine("this.insertChild(" + filedName + ", this.childrenNames());").addLine("return this." + filedName + ";"));

                javaClass.addMethod(new JavaMethod("delete" + augmentClassname, "void").setModifiers("public").setExceptions(JNCException.class.getName())
                        .addLine("this." + filedName + " = null;").addLine("String path=\"" + augmentedNode.getName() + "\";").addLine("this.delete(path);"));
            } while ((augmentedNode = augmentedNode.getNextSibling()) != null);

        }
//        public com.tailf.jnc.YangUInt32 getIdValue() throws JNCException {
//            return (com.tailf.jnc.YangUInt32)getValue("id");
//        }

//        public Employee cloneShallow() {
//            Employee copy;
//            try {
//                copy = new Employee(getIdValue().toString());
//            } catch (JNCException e) {
//                copy = null;
//            }
//            return (Employee)cloneShallowContent(copy);
//        }
        try {
            javaClass.write(absoluteDirPath);
        } catch (IOException e) {
            throw new TranslatorException(e);
        }

//        try {
//            generateJava(GENERATE_INTERFACE_WITH_BUILDER, this);
//        } catch (IOException e) {
//            throw new TranslatorException("Failed to generate code for list node " +
//                                                  getName() + " in " +
//                                                  getLineNumber() + " at " +
//                                                  getCharPosition() +
//                                                  " in " + getFileName(), e);
//        }
    }
}
