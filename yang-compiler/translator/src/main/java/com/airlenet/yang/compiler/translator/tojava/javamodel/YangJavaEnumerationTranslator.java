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

import com.airlenet.yang.compiler.datamodel.YangType;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaEnumeration;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaTypeDef;
import com.airlenet.yang.compiler.translator.exception.InvalidNodeForTranslatorException;
import com.airlenet.yang.compiler.translator.exception.TranslatorException;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGenerator;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGeneratorInfo;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoTranslator;
import com.airlenet.yang.compiler.translator.tojava.TempJavaCodeFragmentFiles;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaClass;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaMethod;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.tailf.jnc.YangElement;
import com.tailf.jnc.YangEnumeration;
import com.tailf.jnc.YangException;

import java.io.IOException;
import java.util.stream.Collectors;

import static com.airlenet.yang.compiler.translator.tojava.GeneratedJavaFileType.GENERATE_ENUM_CLASS;
import static com.airlenet.yang.compiler.translator.tojava.YangJavaModelUtils.*;
import static com.airlenet.yang.compiler.translator.tojava.utils.TranslatorErrorType.FAIL_AT_ENTRY;
import static com.airlenet.yang.compiler.translator.tojava.utils.TranslatorErrorType.FAIL_AT_EXIT;
import static com.airlenet.yang.compiler.translator.tojava.utils.TranslatorUtils.getErrorMsg;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.getAbsolutePackagePath;

/**
 * Represents YANG java enumeration information extended to support java code generation.
 */
public class YangJavaEnumerationTranslator
        extends YangJavaEnumeration
        implements JavaCodeGenerator, JavaCodeGeneratorInfo {

    private static final long serialVersionUID = 806201629L;

    /**
     * File handle to maintain temporary java code fragments as per the code
     * snippet types.
     */
    private transient TempJavaCodeFragmentFiles tempFileHandle;

    /**
     * Creates YANG java enumeration object.
     */
    public YangJavaEnumerationTranslator() {
        setJavaFileInfo(new JavaFileInfoTranslator());
        getJavaFileInfo().setGeneratedFileTypes(GENERATE_ENUM_CLASS);
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
                                                  getName());
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
     * enumeration info.
     *
     * @param yangPlugin YANG plugin config
     * @throws TranslatorException translator operations fails
     */
    @Override
    public void generateCodeEntry(YangPluginConfig yangPlugin) throws TranslatorException {
        updateJNCPackageInfo(this,yangPlugin);


//        this.getTypeList().forEach(yangType -> {
//            ((YangJavaTypeTranslator) yangType).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
//        });
//        try {
//            if (getReferredSchema() != null) {
//                throw new InvalidNodeForTranslatorException();
//            }
//            generateCodeOfNode(this, yangPlugin);
//            tempFileHandle.getEnumTempFiles().setEnumClass(true);
//        } catch (IOException e) {
//            throw new TranslatorException(getErrorMsg(FAIL_AT_ENTRY, this,
//                                                      e.getLocalizedMessage()), e);
//        }
    }

    /**
     * Creates a java file using the YANG enumeration info.
     *
     * @throws TranslatorException translator operation fail
     */
    @Override
    public void generateCodeExit() throws TranslatorException {
        if(this.getParent()!=null && this.getParent() instanceof YangJavaTypeDef){
            return;
        }
//isisIPRAType_enum
        String classname= YangElement.normalize(this.getName());
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
//        YangJavaModule yangJavaModule = (YangJavaModule)this.getYangJavaModule();


        javaClass.setExtend(YangEnumeration.class.getName());

        javaClass.addMethod(new JavaMethod(classname,"")
                .setModifiers("public")
                .setExceptions(YangException.class.getName())
                .addParameter("String","value")
                .addLine("super(value,").addLine("\tnew String[] {")
                .addLine(this.getEnumSet().stream().map(yangEnum ->"\t\t\""+ yangEnum.getNamedValue()+"\"").collect(Collectors.joining(",\n\t\t")))
                .addLine("\t}").addLine(");").addLine("check();")
        );

        javaClass.addMethod(new JavaMethod("setValue","void").setModifiers("public").setExceptions(YangException.class.getName())
                .addParameter("String","value")
                .addLine("\tsuper.setValue(value);")
                .addLine("\tcheck();")
        );
        javaClass.addMethod(new JavaMethod("check","void").setModifiers("public").setExceptions(YangException.class.getName())
                .addLine("\tsuper.check();")
        );

        try {
            javaClass.write(absoluteDirPath);
        } catch (IOException e) {
            throw new TranslatorException(e);
        }
//        try {
//            generateJava(GENERATE_ENUM_CLASS, this);
//        } catch (IOException e) {
//            throw new TranslatorException(getErrorMsg(FAIL_AT_EXIT, this,
//                                                      e.getLocalizedMessage()), e);
//        }
    }
}
