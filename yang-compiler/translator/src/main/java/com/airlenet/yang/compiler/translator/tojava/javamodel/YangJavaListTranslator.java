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
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaContainer;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaList;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaModule;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaUnion;
import com.airlenet.yang.compiler.datamodel.utils.builtindatatype.YangDataTypes;
import com.airlenet.yang.compiler.translator.exception.TranslatorException;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGenerator;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGeneratorInfo;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoTranslator;
import com.airlenet.yang.compiler.translator.tojava.TempJavaCodeFragmentFiles;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaClass;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaMethod;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.tailf.jnc.ElementLeafListValueIterator;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.YangElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
            if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION) {
                ((YangJavaLeafTranslator) yangLeaf).updateJavaQualifiedInfo();
                ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
            }
        }
        for (YangLeafList yangLeaf : this.getListOfLeafList()) {
            if (yangLeaf.getDataType().getDataType() == YangDataTypes.DERIVED || yangLeaf.getDataType().getDataType() == YangDataTypes.UNION) {
                ((YangJavaLeafListTranslator) yangLeaf).updateJavaQualifiedInfo();
                ((YangJavaTypeTranslator) yangLeaf.getDataType()).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
            }
        }

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
        JavaClass javaClass = new JavaClass(classname, fileInfo.getPackage(), "");
        String absoluteDirPath = getAbsolutePackagePath(fileInfo.getBaseCodeGenPath(),
                fileInfo.getPackageFilePath());
        YangJavaModule yangJavaModule = (YangJavaModule) this.getRoot();
        javaClass.setExtend("com.tailf.jnc.YangElement");
        javaClass.addMethod(new JavaMethod(javaClass.getName(), "").setModifiers("public").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName())
                .addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");"));

        List<YangLeaf> listOfLeaf = this.getListOfLeaf();

        List<YangLeaf> keys = new ArrayList<>();
        for (String key : this.getKeyList()) {
            for (YangLeaf yangLeaf : listOfLeaf) {
                if (key.equals(yangLeaf.getName())) {
                    keys.add(yangLeaf);
                }
            }
        }
///constructor

        JavaMethod constructor = new JavaMethod(javaClass.getName(), "").addDependency("com.tailf.jnc.Leaf");
        constructor.addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");");
        for (YangLeaf yangLeaf : keys) {
            constructor.addParameter(AttributesJavaDataType.getJNCDataType(yangLeaf.getDataType()), YangElement.camelize(yangLeaf.getName() + "Value"));
            constructor.addLine("Leaf " + yangLeaf.getName() + " = new Leaf(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + yangLeaf.getName() + "\");");
            constructor.addLine(yangLeaf.getName() + ".setValue(" + yangLeaf.getName() + "Value);");
            constructor.addLine("insertChild(" + yangLeaf.getName() + ", childrenNames());");
        }
        constructor.setModifiers("public").setExceptions("JNCException").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName());
        javaClass.addMethod(constructor);

        if (!keys.isEmpty()) {

            constructor = new JavaMethod(javaClass.getName(), "").addDependency("com.tailf.jnc.Leaf");
            constructor.addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");");
            for (YangLeaf yangLeaf : keys) {
                constructor.addParameter("String", YangElement.camelize(yangLeaf.getName() + "Value"));
                constructor.addLine("Leaf " + yangLeaf.getName() + " = new Leaf(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + yangLeaf.getName() + "\");");
                constructor.addLine(yangLeaf.getName() + ".setValue(new " + AttributesJavaDataType.getJNCDataType(yangLeaf.getDataType()) + "(" + yangLeaf.getName() + "Value));");
                constructor.addLine("insertChild(" + yangLeaf.getName() + ", childrenNames());");
            }
            constructor.setModifiers("public").setExceptions("JNCException").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName());
            javaClass.addMethod(constructor);

        }


/////keyNames
        JavaMethod keyNames = new JavaMethod("keyNames", "String[]");
        keyNames.setModifiers("public");
        if (keys.isEmpty()) {
            keyNames.addLine("return null;");
        } else {
            keyNames.addLine("return new String[]{");

            for (YangLeaf child : keys) {
                keyNames.addLine("\t\t\"" + child.getName() + "\",");
            }

            keyNames.addLine("};");
        }


        javaClass.addMethod(keyNames);

////childrenNames
        JavaMethod childrenNames = new JavaMethod("childrenNames", "String[]");
        childrenNames.setModifiers("public")
                .addLine("return new String[]{");

        for (YangLeaf child : listOfLeaf) {
            childrenNames.addLine("\t\t\"" + child.getName() + "\",");
        }
        for (YangLeafList child : this.getListOfLeafList()) {
            childrenNames.addLine("\t\t\"" + child.getName() + "\",");
        }
        {
            YangNode child = this.getChild();
            while (child != null) {
                if (child instanceof YangJavaList || child instanceof YangJavaContainer) {
                    childrenNames.addLine("\t\t\"" + child.getName() + "\",");
                }
                child = child.getNextSibling();
            }
        }

        childrenNames.addLine("};");
        javaClass.addMethod(childrenNames);

////clone
        JavaMethod clone = new JavaMethod("clone", classname);
        clone.addDependency("com.tailf.jnc.JNCException");
        clone.setModifiers("public")
                .addLine(classname + " copy;").addLine("try {");
        if (keys.isEmpty()) {
            clone.addLine("\tcopy = new " + classname + "();");
        } else {
            String param = keys.stream().map(yangLeaf -> "get" + YangElement.normalize(yangLeaf.getName()) + "Value().toString()").collect(Collectors.joining(","));
            clone.addLine("\tcopy = new " + classname + "(" + param + ");");
        }
        clone.addLine("} catch (JNCException e) {").addLine("    copy = null;").addLine("}").addLine(" return (" + classname + ")cloneContent(copy);");
        javaClass.addMethod(clone);

////cloneShallow
        JavaMethod cloneShallow = new JavaMethod("cloneShallow", classname);
        cloneShallow.addDependency("com.tailf.jnc.JNCException");
        cloneShallow.setModifiers("public")
                .addLine(classname + " copy;").addLine("try {");
        if (keys.isEmpty()) {
            cloneShallow.addLine("\tcopy = new " + classname + "();");
        } else {
            String param = keys.stream().map(yangLeaf -> "get" + YangElement.normalize(yangLeaf.getName()) + "Value().toString()").collect(Collectors.joining(","));
            cloneShallow.addLine("\tcopy = new " + classname + "(" + param + ");");
        }
        cloneShallow.addLine("} catch (JNCException e) {").addLine("    copy = null;").addLine("}").addLine(" return (" + classname + ")cloneShallowContent(copy);");
        javaClass.addMethod(cloneShallow);

        ////

        for (YangLeaf yangLeaf : listOfLeaf) {

            JavaMethod getMethod = new JavaMethod("get" + YangElement.normalize(yangLeaf.getName()) + "Value", AttributesJavaDataType.getJNCDataType(yangLeaf.getDataType())).setModifiers("public");
            getMethod.setExceptions("JNCException");
            getMethod.addLine("return (" + AttributesJavaDataType.getJNCDataType(yangLeaf.getDataType()) + ")" + "getValue(\"" + yangLeaf.getName() + "\");");
            javaClass.addMethod(getMethod);


            JavaMethod setMethod = new JavaMethod("set" + YangElement.normalize(yangLeaf.getName()) + "Value", "void").setModifiers("public");
            setMethod.setExceptions("JNCException");
            setMethod.addParameter(AttributesJavaDataType.getJNCDataType(yangLeaf.getDataType()), YangElement.camelize(yangLeaf.getName() + "Value"));


            setMethod.addLine("setLeafValue(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE,");
            setMethod.addLine("\t\"" + yangLeaf.getName() + "\",");
            setMethod.addLine("\t" + YangElement.camelize(yangLeaf.getName() + "Value") + ",");
            setMethod.addLine("\tchildrenNames());");
            javaClass.addMethod(setMethod);

            setMethod = new JavaMethod("set" + YangElement.normalize(yangLeaf.getName()) + "Value", "void").setModifiers("public");
            setMethod.setExceptions("JNCException");
            setMethod.addParameter("String", YangElement.camelize(yangLeaf.getName() + "Value"));

            setMethod.addLine("set" + YangElement.normalize(yangLeaf.getName()) + "Value" + "(new " + AttributesJavaDataType.getJNCDataType(yangLeaf.getDataType()) + "(" + YangElement.camelize(yangLeaf.getName() + "Value") + "));");
            javaClass.addMethod(setMethod);

            JavaMethod addMethod = new JavaMethod("set" + YangElement.normalize(yangLeaf.getName()), "void").setModifiers("public");
            addMethod.setExceptions("JNCException");
            addMethod.addLine("setLeafValue(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE,");
            addMethod.addLine("\t\"" + yangLeaf.getName() + "\",");
            addMethod.addLine("\tnull,");
            addMethod.addLine("\tchildrenNames());");

            javaClass.addMethod(addMethod);

            JavaMethod unsetMethod = new JavaMethod("unset" + YangElement.normalize(yangLeaf.getName()) + "Value", "void").setModifiers("public");
            unsetMethod.setExceptions("JNCException");
            unsetMethod.addLine("delete" + "(\"" + yangLeaf.getName() + "\");");
            javaClass.addMethod(unsetMethod);

            Arrays.asList("Create", "Replace", "Merge", "Delete", "Remove").forEach(markType -> {
                JavaMethod markMethod = new JavaMethod("mark" + YangElement.normalize(yangLeaf.getName()) + markType, "void").setModifiers("public");
                markMethod.setExceptions("JNCException");
                markMethod.addLine("markLeaf" + markType + "(\"" + yangLeaf.getName() + "\");");
                javaClass.addMethod(markMethod);

            });

        }
        for (YangLeafList yangLeafList : this.getListOfLeafList()) {

            JavaMethod iteratorMethod = new JavaMethod( YangElement.camelize(yangLeafList.getName()) + "Iterator", "ElementLeafListValueIterator").setModifiers("public");
            iteratorMethod.addDependency(ElementLeafListValueIterator.class.getName());
            iteratorMethod.addLine("return new ElementLeafListValueIterator(children, \"user-name\");");
            javaClass.addMethod(iteratorMethod);

//            JavaMethod getMethod = new JavaMethod("get"+YangElement.normalize(yangLeafList.getName())+"List", AttributesJavaDataType.getJNCDataType(yangLeafList.getDataType())).setModifiers("public");
//            getMethod.setExceptions("JNCException");
//            getMethod.addLine("return ("+AttributesJavaDataType.getJNCDataType(yangLeafList.getDataType())+")"+"getValue(\""+yangLeafList.getName()+"\");");
//            javaClass.addMethod(getMethod);


            JavaMethod setMethod = new JavaMethod("set" + YangElement.normalize(yangLeafList.getName()) + "Value", "void").setModifiers("public");
            setMethod.setExceptions("JNCException");
            setMethod.addParameter(AttributesJavaDataType.getJNCDataType(yangLeafList.getDataType()), YangElement.camelize(yangLeafList.getName() + "Value"));


            setMethod.addLine("setLeafValue(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE,");
            setMethod.addLine("\t\"" + yangLeafList.getName() + "\",");
            setMethod.addLine("\t" + YangElement.camelize(yangLeafList.getName() + "Value") + ",");
            setMethod.addLine("\tchildrenNames());");
            javaClass.addMethod(setMethod);

            setMethod = new JavaMethod("set" + YangElement.normalize(yangLeafList.getName()) + "Value", "void").setModifiers("public");
            setMethod.setExceptions("JNCException");
            setMethod.addParameter("String", YangElement.camelize(yangLeafList.getName() + "Value"));

            setMethod.addLine("set" + YangElement.normalize(yangLeafList.getName()) + "Value" + "(new " + AttributesJavaDataType.getJNCDataType(yangLeafList.getDataType()) + "(" + YangElement.camelize(yangLeafList.getName() + "Value") + "));");
            javaClass.addMethod(setMethod);

            JavaMethod addMethod = new JavaMethod("set" + YangElement.normalize(yangLeafList.getName()), "void").setModifiers("public");
            addMethod.setExceptions("JNCException");
            addMethod.addLine("setLeafValue(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE,");
            addMethod.addLine("\t\"" + yangLeafList.getName() + "\",");
            addMethod.addLine("\tnull,");
            addMethod.addLine("\tchildrenNames());");

            javaClass.addMethod(addMethod);

            /// delete方法
//            JavaMethod unsetMethod = new JavaMethod("unset"+YangElement.normalize(yangLeafList.getName())+"Value", "void").setModifiers("public");
//            unsetMethod.setExceptions("JNCException");
//            unsetMethod.addLine("delete"+"(\""+yangLeafList.getName()+"\");");
//            javaClass.addMethod(unsetMethod);

            // TODO 修改支持Key
            Arrays.asList("Create", "Replace", "Merge", "Delete", "Remove").forEach(markType -> {
                JavaMethod markMethod = new JavaMethod("mark" + YangElement.normalize(yangLeafList.getName()) + markType, "void").setModifiers("public");
                markMethod.setExceptions("JNCException");
                markMethod.addLine("markLeaf" + markType + "(\"" + yangLeafList.getName() + "\");");
                javaClass.addMethod(markMethod);

            });
        }

        {
            YangNode child = this.getChild();
            while (child != null) {
                if (child instanceof YangJavaList) {

                    YangJavaList yangJavaList = (YangJavaList) child;


                    JavaMethod iteratorMethod = new JavaMethod(YangElement.camelize(child.getName()) + "Iterator", "ElementLeafListValueIterator").setModifiers("public");
                    iteratorMethod.addDependency(ElementLeafListValueIterator.class.getName());
                    iteratorMethod.addLine("return new ElementLeafListValueIterator(children, \"user-name\");");
                    javaClass.addMethod(iteratorMethod);

//            JavaMethod getMethod = new JavaMethod("get"+YangElement.normalize(yangLeafList.getName())+"List", AttributesJavaDataType.getJNCDataType(yangLeafList.getDataType())).setModifiers("public");
//            getMethod.setExceptions("JNCException");
//            getMethod.addLine("return ("+AttributesJavaDataType.getJNCDataType(yangLeafList.getDataType())+")"+"getValue(\""+yangLeafList.getName()+"\");");
//            javaClass.addMethod(getMethod);

                    {

                        JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public");
                        addMethod.addDependency(child.getJavaPackage() + "." + YangElement.normalize(child.getName()));
                        addMethod.setExceptions("JNCException");
                        addMethod.addLine(YangElement.normalize(child.getName()) + " " + YangElement.camelize(child.getName()) + " = new " + YangElement.normalize(child.getName()) + "();");
                        addMethod.addLine("insertChild(" + YangElement.camelize(child.getName()) + ", childrenNames());");
                        addMethod.addLine("return " + YangElement.camelize(child.getName()) + ";");

                        javaClass.addMethod(addMethod);
                    }

                    {

                        JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public");
                        addMethod.addParameter(YangElement.normalize(child.getName()), YangElement.camelize(child.getName()));
                        addMethod.addDependency(child.getJavaPackage() + "." + YangElement.normalize(child.getName()));
                        addMethod.setExceptions("JNCException");
                        addMethod.addLine("insertChild(" + YangElement.camelize(child.getName()) + ", childrenNames());");
                        addMethod.addLine("return " + YangElement.camelize(child.getName()) + ";");

                        javaClass.addMethod(addMethod);
                    }
                    if (!yangJavaList.getListOfKeyLeaf().isEmpty()) {


                        JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public");

                        List<String> parameterValue = new ArrayList<>();
                        for (YangLeaf keyNodeLeaf : yangJavaList.getListOfKeyLeaf()) {
                            parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                            addMethod.addParameter(AttributesJavaDataType.getJNCDataType(keyNodeLeaf.getDataType()), YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                        }

                        addMethod.addDependency(child.getJavaPackage() + "." + YangElement.normalize(child.getName()));
                        addMethod.setExceptions("JNCException");
                        addMethod.addLine(YangElement.normalize(child.getName()) + " " + YangElement.camelize(child.getName()) + " = new " + YangElement.normalize(child.getName()) + "(" + parameterValue.stream().collect(Collectors.joining(",")) + ");");
                        addMethod.addLine("return add" + YangElement.normalize(child.getName()) + "(" + YangElement.camelize(child.getName()) + ");");

                        javaClass.addMethod(addMethod);

                        ///////////////////////////
                        addMethod = new JavaMethod("add" + YangElement.normalize(child.getName()), YangElement.normalize(child.getName())).setModifiers("public");

                        parameterValue = new ArrayList<>();
                        for (YangLeaf keyNodeLeaf : yangJavaList.getListOfKeyLeaf()) {
                            parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                            addMethod.addParameter("String", YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                        }

                        addMethod.addDependency(child.getJavaPackage() + "." + YangElement.normalize(child.getName()));
                        addMethod.setExceptions("JNCException");
                        addMethod.addLine(YangElement.normalize(child.getName()) + " " + YangElement.camelize(child.getName()) + " = new " + YangElement.normalize(child.getName()) + "(" + parameterValue.stream().collect(Collectors.joining(",")) + ");");
                        addMethod.addLine("return add" + YangElement.normalize(child.getName()) + "(" + YangElement.camelize(child.getName()) + ");");

                        javaClass.addMethod(addMethod);

                        //   增加delete方法1


                        JavaMethod deleteMethod = new JavaMethod("delete" + YangElement.normalize(child.getName()), "void").setModifiers("public");

                        parameterValue = new ArrayList<>();
                        for (YangLeaf keyNodeLeaf : yangJavaList.getListOfKeyLeaf()) {
                            parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                            deleteMethod.addParameter(AttributesJavaDataType.getJNCDataType(keyNodeLeaf.getDataType()), YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                        }

                        deleteMethod.addDependency(child.getJavaPackage() + "." + YangElement.normalize(child.getName()));
                        deleteMethod.setExceptions("JNCException");

                        deleteMethod.addLine("String path = \"rule" + yangJavaList.getListOfKeyLeaf().stream().map(keyLeaf -> "[" + keyLeaf.getName() + "='\" + " + YangElement.camelize(keyLeaf.getName()) + "Value + \"']").collect(Collectors.joining("")) + "\";");
                        deleteMethod.addLine("delete(path);");

                        javaClass.addMethod(deleteMethod);

                        // 增加delete方法2
                        deleteMethod = new JavaMethod("delete" + YangElement.normalize(child.getName()), "void").setModifiers("public");

                        parameterValue = new ArrayList<>();
                        for (YangLeaf keyNodeLeaf : yangJavaList.getListOfKeyLeaf()) {
                            parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                            deleteMethod.addParameter("String", YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                        }

                        deleteMethod.addDependency(child.getJavaPackage() + "." + YangElement.normalize(child.getName()));
                        deleteMethod.setExceptions("JNCException");

                        deleteMethod.addLine("String path = \"rule" + yangJavaList.getListOfKeyLeaf().stream().map(keyLeaf -> "[" + keyLeaf.getName() + "='\" + " + YangElement.camelize(keyLeaf.getName()) + "Value + \"']").collect(Collectors.joining("")) + "\";");
                        deleteMethod.addLine("delete(path);");

                        javaClass.addMethod(deleteMethod);


                    }


//
//                    final YangNode fchild =child;
//                    Arrays.asList("Create", "Replace", "Merge", "Delete", "Remove").forEach(markType -> {
//                        JavaMethod markMethod = new JavaMethod("mark" + YangElement.normalize(fchild.getName()) + markType, "void").setModifiers("public");
//                        markMethod.setExceptions("JNCException");
//                        markMethod.addLine("markLeaf" + markType + "(\"" + fchild.getName() + "\");");
//                        javaClass.addMethod(markMethod);
//
//                    });


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
