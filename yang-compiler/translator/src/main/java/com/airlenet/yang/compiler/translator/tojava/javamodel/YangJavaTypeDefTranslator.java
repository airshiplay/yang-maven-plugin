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

import com.airlenet.yang.compiler.datamodel.YangBit;
import com.airlenet.yang.compiler.datamodel.YangDerivedInfo;
import com.airlenet.yang.compiler.datamodel.YangType;
import com.airlenet.yang.compiler.datamodel.javadatamodel.*;
import com.airlenet.yang.compiler.translator.exception.InvalidNodeForTranslatorException;
import com.airlenet.yang.compiler.translator.exception.TranslatorException;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGenerator;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGeneratorInfo;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoTranslator;
import com.airlenet.yang.compiler.translator.tojava.TempJavaCodeFragmentFiles;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaClass;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaMethod;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.tailf.jnc.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.airlenet.yang.compiler.datamodel.utils.builtindatatype.YangDataTypes.*;
import static com.airlenet.yang.compiler.translator.tojava.GeneratedJavaFileType.GENERATE_TYPEDEF_CLASS;
import static com.airlenet.yang.compiler.translator.tojava.YangJavaModelUtils.*;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.getAbsolutePackagePath;

/**
 * Represents type define information extended to support java code generation.
 */
public class YangJavaTypeDefTranslator
        extends YangJavaTypeDef
        implements JavaCodeGeneratorInfo, JavaCodeGenerator {

    private static final long serialVersionUID = 806201620L;

    /**
     * File handle to maintain temporary java code fragments as per the code
     * snippet types.
     */
    private transient TempJavaCodeFragmentFiles tempFileHandle;

    /**
     * Creates a YANG java typedef object.
     */
    public YangJavaTypeDefTranslator() {
        super();
        setJavaFileInfo(new JavaFileInfoTranslator());
        getJavaFileInfo().setGeneratedFileTypes(GENERATE_TYPEDEF_CLASS);
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
     * typedef info.
     *
     * @param yangPlugin YANG plugin config
     * @throws TranslatorException when fails to translate
     */
    @Override
    public void generateCodeEntry(YangPluginConfig yangPlugin) throws TranslatorException {
        if (getReferredSchema() != null) {
            throw new InvalidNodeForTranslatorException();
        }
        // TODO update the below exception in all related places, remove file
        // name and other information.
        YangType typeInTypeDef = this.getTypeDefBaseType();
        InvalidNodeForTranslatorException exception = new InvalidNodeForTranslatorException();
        exception.setFileName(this.getFileName());
        exception.setCharPosition(this.getCharPosition());
        exception.setLine(this.getLineNumber());
        if (typeInTypeDef.getDataType() == DERIVED) {
            YangDerivedInfo derivedInfo = (YangDerivedInfo) typeInTypeDef.getDataTypeExtendedInfo();
            if (derivedInfo.getEffectiveBuiltInType() == LEAFREF) {
                throw exception;
            }
        } else if (typeInTypeDef.getDataType() == LEAFREF) {
            throw exception;
        }
        updateJNCPackageInfo(this,yangPlugin);

        if(typeInTypeDef.getDataType() == DERIVED){

            this.getTypeList().forEach(yangType -> {
                ((YangJavaTypeTranslator)yangType).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
            });
        }
        if(typeInTypeDef.getDataType() == BITS){

            this.getTypeList().forEach(yangType -> {
                ((YangJavaTypeTranslator)yangType).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
            });
        }
        ((YangJavaTypeTranslator)typeInTypeDef).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());

//        try {
//            generateCodeOfNode(this, yangPlugin);
//        } catch (IOException e) {
//            throw new TranslatorException(
//                    "Failed to prepare generate code entry for typedef node " + getName()
//                            + " in " + getLineNumber() +
//                            " at " + getCharPosition() +
//                            " in " + getFileName(), e);
//        }
    }

    /**
     * Create a java file using the YANG typedef info.
     *
     * @throws TranslatorException when fails to translate
     */
    @Override
    public void generateCodeExit() throws TranslatorException {
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

        YangType typeInTypeDef = this.getTypeDefBaseType();


        if(typeInTypeDef.getDataType() == ENUMERATION){
            javaClass.setExtend(YangEnumeration.class.getName());

            javaClass.addMethod(new JavaMethod(classname,"")
                    .setModifiers("public")
                    .setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super(value,").addLine("\tnew String[] {")
                    .addLine(((YangJavaEnumeration)this.getTypeList().get(0).getDataTypeExtendedInfo()).getEnumSet().stream().map(yangEnum ->"\t\t\""+ yangEnum.getNamedValue()+"\"").collect(Collectors.joining(",\n\t\t")))
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
        }else if(typeInTypeDef.getDataType() == UINT8||typeInTypeDef.getDataType() == UINT16||typeInTypeDef.getDataType() == UINT32 ||typeInTypeDef.getDataType() == UINT64
                ||typeInTypeDef.getDataType() == INT16||typeInTypeDef.getDataType() == INT32||typeInTypeDef.getDataType() == INT64){
            String extend=null;
            String type =null;
            switch (typeInTypeDef.getDataType()){
                case UINT8:
                    extend= YangUInt8.class.getName();
                    type ="short";
                    break;
                case INT16:
                    extend= YangInt16.class.getName();
                    type ="short";
                    break;
                case UINT16:
                    extend= YangUInt16.class.getName();
                    type ="int";
                    break;
                case INT32:
                    extend= YangInt32.class.getName();
                    type ="int";
                    break;
                case UINT32:
                    extend= YangUInt32.class.getName();
                    type ="long";
                    break;
                case INT64:
                    extend= YangInt64.class.getName();
                    type ="long";
                    break;
                case UINT64:
                    extend= YangUInt64.class.getName();
                    type =BigInteger.class.getName();
                    break;
            }
            javaClass.setExtend(extend);
            javaClass.addMethod(new JavaMethod(classname,"")
                    .setModifiers("public")
                    .setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super(value);")
                    .addLine("check();")
            );
            javaClass.addMethod(new JavaMethod(classname,"")
                    .setModifiers("public")
                    .setExceptions(YangException.class.getName())
                    .addParameter(type,"value")
                    .addLine("super(value);")
                    .addLine("check();")
            );

            javaClass.addMethod(new JavaMethod("setValue","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super.setValue(value);")
                    .addLine("check();")
            );
            javaClass.addMethod(new JavaMethod("setValue","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addParameter(type,"value")
                    .addLine("super.setValue(value);")
                    .addLine("check();")
            );
            javaClass.addMethod(new JavaMethod("check","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addLine("super.check();")
            );
        }else if(typeInTypeDef.getDataType() == UNION){
            javaClass.setExtend(YangUnion.class.getName());

            javaClass.addMethod(new JavaMethod(classname,"")
                    .setModifiers("public")
                    .setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super(value,").addLine("\tnew String[] {")

                    .addLine(
                            ((YangJavaUnion)((YangJavaType)this.getTypeList().get(0)).getDataTypeExtendedInfo()).getTypeList().stream()
                                    .map( yangType -> "\""+((YangJavaType)yangType).getJavaQualifiedInfo().getPkgInfo()+"."+ ((YangJavaType)yangType).getJavaQualifiedInfo().getClassInfo()+"\"").collect(Collectors.joining(","))
                    )
                    .addLine("}").addLine(");").addLine("check();")
            );

            javaClass.addMethod(new JavaMethod("setValue","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super.setValue(value);")
                    .addLine("check();")
            );
            javaClass.addMethod(new JavaMethod("check","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addLine("super.check();")
            );
        }else if(typeInTypeDef.getDataType() == STRING ||typeInTypeDef.getDataType()==BINARY){

            String extend=null;
            switch (typeInTypeDef.getDataType()){
                case STRING:
                    extend= YangString.class.getName();
                    break;
                case BINARY:
                    extend=YangBinary.class.getName();
                    break;
            }
            javaClass.setExtend(extend);

            javaClass.addMethod(new JavaMethod(classname,"")
                    .setModifiers("public")
                    .setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super(value);").addLine("check();")
            );

            javaClass.addMethod(new JavaMethod("setValue","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super.setValue(value);")
                    .addLine("check();")
            );
            javaClass.addMethod(new JavaMethod("check","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addLine("super.check();")
            );
        }else if(typeInTypeDef.getDataType() == DERIVED){
            JavaQualifiedTypeInfo javaQualifiedInfo = ((YangJavaTypeTranslator) this.getTypeList().get(0)).getJavaQualifiedInfo();
            javaClass.setExtend(javaQualifiedInfo.getPkgInfo()+"."+javaQualifiedInfo.getClassInfo());
            javaClass.addMethod(new JavaMethod(classname,"")
                    .setModifiers("public")
                    .setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super(value);").addLine("check();")
            );

            javaClass.addMethod(new JavaMethod("setValue","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super.setValue(value);")
                    .addLine("check();")
            );
            javaClass.addMethod(new JavaMethod("check","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addLine("super.check();")
            );
        }else if(typeInTypeDef.getDataType() == BITS){
            javaClass.setExtend(YangBits.class.getName());
            Set<Map.Entry<Integer, YangBit>> entries = ((com.airlenet.yang.compiler.datamodel.YangBits) typeInTypeDef.getDataTypeExtendedInfo()).getBitPositionMap().entrySet();





            javaClass.addMethod(new JavaMethod(classname,"")
                    .setModifiers("public")
                    .setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super(value,")
                    .addLine("\tnew java.math.BigInteger(\""+ entries.stream().map(entry->entry.getKey().intValue()).collect(Collectors.summarizingInt( position-> 1<<position)).getSum()+"\"),")
                    .addLine("\tnew String[] {"+entries.stream().map(entry->"\""+entry.getValue().getBitName()+"\"").collect(Collectors.joining(","))+" },")
                    .addLine("\tnew int[] {"+entries.stream().map(entry->entry.getKey().intValue()+"").collect(Collectors.joining(","))+" }")
                    .addLine(");")
                    .addLine("check();")
            );

            javaClass.addMethod(new JavaMethod("setValue","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addParameter("String","value")
                    .addLine("super.setValue(value);")
                    .addLine("check();")
            );
            javaClass.addMethod(new JavaMethod("setValue","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addParameter(BigInteger.class.getName(),"value")
                    .addLine("super.setValue(value);")
                    .addLine("check();")
            );
            javaClass.addMethod(new JavaMethod("check","void").setModifiers("public").setExceptions(YangException.class.getName())
                    .addLine("super.check();")
            );
        }

        try {
            javaClass.write(absoluteDirPath);
        } catch (IOException e) {
            throw new TranslatorException(e);
        }
//        try {
//            generateJava(GENERATE_TYPEDEF_CLASS, this);
//        } catch (IOException e) {
//            throw new TranslatorException(
//                    "Failed to prepare generate code for typedef node " + getName()
//                            + " in " + getLineNumber() +
//                            " at " + getCharPosition() +
//                            " in " + getFileName(), e);
//        }
    }
}
