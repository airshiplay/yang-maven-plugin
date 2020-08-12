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
import com.airlenet.yang.compiler.datamodel.YangLeafList;
import com.airlenet.yang.compiler.datamodel.YangNode;
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
                "Code generated by "+this.getClass().getSimpleName() +
                        "\n * <p>"+
                        "\n * See line "+fileInfo.getLineNumber()+" in" +
                        "\n * "+fileInfo.getYangFileName()+
                        "\n * "+
                        "\n * @author Auto Generated");
        String absoluteDirPath = getAbsolutePackagePath(fileInfo.getBaseCodeGenPath(),
                fileInfo.getPackageFilePath());
        YangJavaModule yangJavaModule = (YangJavaModule) this.getYangJavaModule();
        javaClass.setExtend("com.tailf.jnc.YangElement");


        //// empty constructor
        javaClass.addMethod(new JavaMethod(javaClass.getName(), "").setModifiers("public").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName())
                .addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");"));

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
                constructor.addLine("Leaf " + YangElement.camelize(yangLeaf.getName()) + " = new Leaf(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + yangLeaf.getName() + "\");");
                constructor.addLine(YangElement.camelize(yangLeaf.getName()) + ".setValue(" + YangElement.camelize(yangLeaf.getName()+ "Value") +");");
                constructor.addLine("insertChild(" + YangElement.camelize(yangLeaf.getName()) + ", childrenNames());");
            }
            constructor.setModifiers("public").setExceptions("JNCException").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName());
            javaClass.addMethod(constructor);


            constructor = new JavaMethod(javaClass.getName(), "").addDependency("com.tailf.jnc.Leaf");
            constructor.addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");");
            for (YangLeaf yangLeaf : keys) {
                constructor.addParameter("String", YangElement.camelize(yangLeaf.getName() + "Value"));
                constructor.addLine("Leaf " + YangElement.camelize(yangLeaf.getName()) + " = new Leaf(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + yangLeaf.getName() + "\");");
                YangLeaf dataTypeYangLeaf = yangLeaf;
//                while (dataTypeYangLeaf.getReferredSchema()!=null){
//                    dataTypeYangLeaf =dataTypeYangLeaf.getReferredSchema();
//                }
                constructor.addLine(YangElement.camelize(yangLeaf.getName()) + ".setValue(new " + AttributesJavaDataType.getJNCDataType(dataTypeYangLeaf.getDataType()) + "(" + YangElement.camelize(yangLeaf.getName()+"Value") + "));");
                constructor.addLine("insertChild(" + YangElement.camelize(yangLeaf.getName()) + ", childrenNames());");
            }
            constructor.setModifiers("public").setExceptions("JNCException").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName());
            javaClass.addMethod(constructor);

        }


/////keyNames
        JNCCodeUtil.keyNamesMethod(javaClass, getListOfKeyLeaf());

////childrenNames
        JNCCodeUtil.childrenNamesMethod(javaClass, getListOfLeaf(), getListOfLeafList(), getChild());

////clone
        JNCCodeUtil.cloneMethod(javaClass, keys);

////cloneShallow
        JNCCodeUtil.cloneShallowMethod(javaClass, keys);

        ////

        for (YangLeaf yangLeaf : listOfLeaf) {
            JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);
        }
        for (YangLeafList yangLeafList : this.getListOfLeafList()) {
            JNCCodeUtil.yangLeafListMethod(javaClass, yangJavaModule, yangLeafList);
        }

        {
            YangNode child = this.getChild();
            while (child != null) {
                if (child instanceof YangJavaUnion || child instanceof YangJavaUses || child instanceof YangJavaGrouping || child instanceof YangJavaEnumeration) {

                } else if (child instanceof YangJavaList) {
//                JNCCodeUtil.yangNodeMethond(javaClass, child);
                    JNCCodeUtil.yangJavaListMethod(javaClass, (YangJavaList) child);
                } else if (child instanceof YangJavaContainer) {
                    JNCCodeUtil.yangNodeMethond(javaClass, child,false);
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
                } else {
                    JNCCodeUtil.yangNodeMethond(javaClass, child,false);
                }
                child = child.getNextSibling();
            }
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
