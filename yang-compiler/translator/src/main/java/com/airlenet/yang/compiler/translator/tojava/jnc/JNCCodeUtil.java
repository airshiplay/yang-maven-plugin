package com.airlenet.yang.compiler.translator.tojava.jnc;

import com.airlenet.yang.compiler.datamodel.*;
import com.airlenet.yang.compiler.datamodel.javadatamodel.*;
import com.airlenet.yang.compiler.translator.tojava.javamodel.AttributesJavaDataType;
import com.tailf.jnc.ElementChildrenIterator;
import com.tailf.jnc.ElementLeafListValueIterator;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.YangBits;
import com.tailf.jnc.YangElement;

import java.util.*;
import java.util.stream.Collectors;

public class JNCCodeUtil {

    public static void keyNamesMethod(JavaClass javaClass, List<YangLeaf> keyListOfLeaf) {
        JavaMethod keyNames = new JavaMethod("keyNames", "String[]");
        keyNames.setModifiers("public");
        if (keyListOfLeaf == null || keyListOfLeaf.isEmpty()) {
            keyNames.addLine("return null;");
        } else {
            keyNames.addLine("return new String[]{");

            for (YangLeaf child : keyListOfLeaf) {
                keyNames.addLine("\t\t\"" + child.getName() + "\",");
            }

            keyNames.addLine("};");
        }


        javaClass.addMethod(keyNames);
    }

    public static void childrenNamesMethod(JavaClass javaClass, List<YangLeaf> listOfLeaf, List<YangLeafList> listOfLeafList, YangNode child) {
        JavaMethod childrenNames = new JavaMethod("childrenNames", "String[]");
        childrenNames.setModifiers("public")
                .addLine("return new String[]{");

        for (YangLeaf yangLeaf : listOfLeaf) {
            childrenNames.addLine("\t\t\"" + yangLeaf.getName() + "\",");
        }

        for (YangLeafList yangLeafList : listOfLeafList) {
            childrenNames.addLine("\t\t\"" + yangLeafList.getName() + "\",");
        }

        while (child != null) {
            if (child instanceof YangJavaUses || child instanceof YangJavaGrouping || child instanceof YangJavaEnumeration || child instanceof YangJavaUnion || child instanceof YangTypeDef) {

            } else if (child instanceof YangJavaChoice) {
                YangNode subchild = ((YangJavaChoice) (child)).getChild();
                while (subchild != null) {
                    childrenNames.addLine("\t\t\"" + subchild.getName() + "\",");
                    subchild = subchild.getNextSibling();
                }
            } else {
                childrenNames.addLine("\t\t\"" + child.getName() + "\",");
            }

            child = child.getNextSibling();
        }
        childrenNames.addLine("};");
        javaClass.addMethod(childrenNames);


    }

    public static void cloneMethod(JavaClass javaClass, List<YangLeaf> keys) {
        JavaMethod clone = new JavaMethod("clone", javaClass.getName());
        clone.addDependency("com.tailf.jnc.JNCException");
        clone.setModifiers("public")
                .addLine(javaClass.getName() + " copy;");
        if (keys == null || keys.isEmpty()) {
            clone.addLine("\tcopy = new " + javaClass.getName() + "();");
        } else {
            String param = keys.stream().map(yangLeaf -> "get" + YangElement.normalize(yangLeaf.getName()) + "Value().toString()").collect(Collectors.joining(","));
            clone.addLine("try {").addLine("\tcopy = new " + javaClass.getName() + "(" + param + ");").addLine("} catch (JNCException e) {").addLine("    copy = null;").addLine("}");
        }
        clone.addLine(" return (" + javaClass.getName() + ")cloneContent(copy);");
        javaClass.addMethod(clone);
    }

    public static void cloneShallowMethod(JavaClass javaClass, List<YangLeaf> yangLeafKeyList) {
        JavaMethod cloneShallow = new JavaMethod("cloneShallow", javaClass.getName());
        cloneShallow.addDependency("com.tailf.jnc.JNCException");
        cloneShallow.setModifiers("public")
                .addLine(javaClass.getName() + " copy;");

        if (yangLeafKeyList == null || yangLeafKeyList.isEmpty()) {
            cloneShallow.addLine("\tcopy = new " + javaClass.getName() + "();");
        } else {
            String param = yangLeafKeyList.stream().map(yangLeaf -> "get" + YangElement.normalize(yangLeaf.getName()) + "Value().toString()").collect(Collectors.joining(","));
            cloneShallow.addLine("try {").addLine("\tcopy = new " + javaClass.getName() + "(" + param + ");").addLine("} catch (JNCException e) {").addLine("    copy = null;").addLine("}");
        }
        cloneShallow.addLine(" return (" + javaClass.getName() + ")cloneShallowContent(copy);");
        javaClass.addMethod(cloneShallow);
    }

    public static void yangLeafMethod(JavaClass javaClass, YangJavaModule yangJavaModule, YangLeaf yangLeaf) {
        YangLeaf dataTypeYangLeaf = yangLeaf;
//        while (dataTypeYangLeaf.getReferredSchema()!=null){
//            dataTypeYangLeaf =dataTypeYangLeaf.getReferredSchema();
//        }
        String leafDateTypeClassName = AttributesJavaDataType.getJNCDataType(dataTypeYangLeaf.getDataType());
        JavaMethod getMethod = new JavaMethod("get" + YangElement.normalize(yangLeaf.getName()) + "Value", leafDateTypeClassName).setModifiers("public");
        getMethod.setExceptions("JNCException");
        getMethod.setJavadoc(yangLeaf.getDescription());
        if (yangLeaf.getDefaultValueInString() == null) {
            getMethod.addLine("return (" + leafDateTypeClassName + ")" + "getValue(\"" + yangLeaf.getName() + "\");");
        } else {
            getMethod.addLine(leafDateTypeClassName + " " + YangElement.camelize(yangLeaf.getName()) + " = (" + leafDateTypeClassName + ")" + "getValue(\"" + yangLeaf.getName() + "\");");
            getMethod.addLine("if (" + YangElement.camelize(yangLeaf.getName()) + " == null) {");
            getMethod.addLine("\t" + YangElement.camelize(yangLeaf.getName()) + "= new " + leafDateTypeClassName + "(\"" + yangLeaf.getDefaultValueInString() + "\");");
            getMethod.addLine("}");
            getMethod.addLine("return " + YangElement.camelize(yangLeaf.getName()) + ";");
        }
        javaClass.addMethod(getMethod);
        yangLeaf.getDefaultValueInString();

        JavaMethod setMethod = new JavaMethod("set" + YangElement.normalize(yangLeaf.getName()) + "Value", "void").setModifiers("public");
        setMethod.setExceptions("JNCException");
        setMethod.addParameter(leafDateTypeClassName, YangElement.camelize(yangLeaf.getName() + "Value"));


        setMethod.addLine("setLeafValue(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE,");
        setMethod.addLine("\t\"" + yangLeaf.getName() + "\",");
        setMethod.addLine("\t" + YangElement.camelize(yangLeaf.getName() + "Value") + ",");
        setMethod.addLine("\tchildrenNames());");
        javaClass.addMethod(setMethod);

        if (!"String".equals(leafDateTypeClassName)) {

            setMethod = new JavaMethod("set" + YangElement.normalize(yangLeaf.getName()) + "Value", "void").setModifiers("public");
            setMethod.setExceptions("JNCException");
            setMethod.addParameter("String", YangElement.camelize(yangLeaf.getName() + "Value"));
            if (com.tailf.jnc.YangEmpty.class.getName().equals(leafDateTypeClassName)) {
                setMethod.addLine("set" + YangElement.normalize(yangLeaf.getName()) + "Value" + "(new " + leafDateTypeClassName + "());");
            } else if (leafDateTypeClassName.equals("byte[]")) {
                setMethod.addLine("set" + YangElement.normalize(yangLeaf.getName()) + "Value" + "(" + YangElement.camelize(yangLeaf.getName() + "Value") + ".getBytes());");
            } else if (leafDateTypeClassName.equals(YangBits.class.getName())) {
                Set<Map.Entry<Integer, YangBit>> entries = ((com.airlenet.yang.compiler.datamodel.YangBits) yangLeaf.getDataType().getDataTypeExtendedInfo()).getBitPositionMap().entrySet();
                setMethod.addLine("set" + YangElement.normalize(yangLeaf.getName()) + "Value" + "(new com.tailf.jnc.YangBits(" + YangElement.camelize(yangLeaf.getName() + "Value") + ",");
                setMethod.addLine("\tnew java.math.BigInteger(\"" + entries.stream().map(entry -> entry.getKey().intValue()).collect(Collectors.summarizingInt(position -> 1 << position)).getSum() + "\"),");
                setMethod.addLine("\tnew String[] {" + entries.stream().map(entry -> "\"" + entry.getValue().getBitName() + "\"").collect(Collectors.joining(",")) + " },");
                setMethod.addLine("\tnew int[] {" + entries.stream().map(entry -> entry.getKey().intValue() + "").collect(Collectors.joining(",")) + " }");
//                    setMethod.addLine("    new String[] {\"iso8473\", \"ipv4\", \"ipv6\", },");
//                    setMethod.addLine("    new int[] {0, 1, 2, }");
                setMethod.addLine("));");
            } else {
                setMethod.addLine("set" + YangElement.normalize(yangLeaf.getName()) + "Value" + "(new " + leafDateTypeClassName + "(" + YangElement.camelize(yangLeaf.getName() + "Value") + "));");
            }
            javaClass.addMethod(setMethod);
        }


        JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(yangLeaf.getName()), "void").setModifiers("public");
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

    public static void yangLeafListMethod(JavaClass javaClass, YangJavaModule yangJavaModule, YangLeafList yangLeafList) {
        {
            JavaMethod iteratorMethod = new JavaMethod(YangElement.camelize(yangLeafList.getName()) + "Iterator", "ElementLeafListValueIterator").setModifiers("public");
            iteratorMethod.addDependency(ElementLeafListValueIterator.class.getName());
            iteratorMethod.addLine("return new ElementLeafListValueIterator(children, \"user-name\");");
            javaClass.addMethod(iteratorMethod);
        }

        /**
         *     public List<com.tailf.jnc.YangUnion> getGroupList() {
         *         List<com.tailf.jnc.YangUnion> list = new ArrayList<>();
         *         ElementLeafListValueIterator iterator = groupIterator();
         *         if(iterator==null){
         *             return null;
         *         }
         *         while (iterator.hasNext()){
         *             com.tailf.jnc.YangUnion next =(com.tailf.jnc.YangUnion) iterator.next();
         *             list.add(next);
         *         }
         *         return list;
         *     }
         */
        YangLeafList dataTypeYangLeaf = yangLeafList;
//        while (dataTypeYangLeaf.getReferredSchema()!=null){
//            dataTypeYangLeaf =dataTypeYangLeaf.getReferredSchema();
//        }
        String leafDateTypeClassName = AttributesJavaDataType.getJNCDataType(dataTypeYangLeaf.getDataType());
        {


            JavaMethod getMethod = new JavaMethod("get" + YangElement.normalize(yangLeafList.getName()) + "List", "List<" + leafDateTypeClassName + ">").setModifiers("public");
            getMethod.addDependency(List.class.getName());
            getMethod.addDependency(ArrayList.class.getName());
            getMethod.setJavadoc(yangLeafList.getDescription());
            getMethod.addLine("List<" + leafDateTypeClassName + "> list = new ArrayList<>();");
            getMethod.addLine("ElementLeafListValueIterator iterator = " + YangElement.camelize(yangLeafList.getName()) + "Iterator();");
            getMethod.addLine("if(iterator==null){");
            getMethod.addLine("    return null;");
            getMethod.addLine("}");
            getMethod.addLine("while (iterator.hasNext()){");
            getMethod.addLine("    " + leafDateTypeClassName + " next =(" + leafDateTypeClassName + ") iterator.next();");
            getMethod.addLine("    list.add(next);");
            getMethod.addLine("}");
            getMethod.addLine("return list;");
            javaClass.addMethod(getMethod);

        }


        JavaMethod setMethod = new JavaMethod("set" + YangElement.normalize(yangLeafList.getName()) + "Value", "void").setModifiers("public");
        setMethod.setExceptions("JNCException");
        setMethod.addParameter(leafDateTypeClassName, YangElement.camelize(yangLeafList.getName() + "Value"));


        setMethod.addLine("setLeafValue(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE,");
        setMethod.addLine("\t\"" + yangLeafList.getName() + "\",");
        setMethod.addLine("\t" + YangElement.camelize(yangLeafList.getName() + "Value") + ",");
        setMethod.addLine("\tchildrenNames());");
        javaClass.addMethod(setMethod);


        if (!"String".equals(leafDateTypeClassName)) {
            setMethod = new JavaMethod("set" + YangElement.normalize(yangLeafList.getName()) + "Value", "void").setModifiers("public");
            setMethod.setExceptions("JNCException");
            setMethod.addParameter("String", YangElement.camelize(yangLeafList.getName() + "Value"));
            if (com.tailf.jnc.YangEmpty.class.getName().equals(leafDateTypeClassName)) {
                setMethod.addLine("set" + YangElement.normalize(yangLeafList.getName()) + "Value" + "(new " + leafDateTypeClassName + "());");
            } else {
                setMethod.addLine("set" + YangElement.normalize(yangLeafList.getName()) + "Value" + "(new " + leafDateTypeClassName + "(" + YangElement.camelize(yangLeafList.getName() + "Value") + "));");
            }

            javaClass.addMethod(setMethod);

        }

        /**
         *     public void deleteGroup(YangUnion groupValue) throws JNCException {
         *         String path = "group[groupValue]";
         *         delete(path);
         *     }
         */
        {


            JavaMethod deleteMethod = new JavaMethod("delete" + YangElement.normalize(yangLeafList.getName()), "void").setModifiers("public");
            deleteMethod.addParameter(leafDateTypeClassName, YangElement.camelize(yangLeafList.getName()) + "Value");
            deleteMethod.setExceptions("JNCException");
            deleteMethod.addLine("String path = \"" + yangLeafList.getName() + "['\"+" + YangElement.camelize(yangLeafList.getName()) + "Value" + "+\"']\";");
            deleteMethod.addLine("delete(path);");
            javaClass.addMethod(deleteMethod);


            if (!"String".equals(leafDateTypeClassName)) {
                deleteMethod = new JavaMethod("delete" + YangElement.normalize(yangLeafList.getName()), "void").setModifiers("public");
                deleteMethod.addParameter("String", YangElement.camelize(yangLeafList.getName()) + "Value");
                deleteMethod.setExceptions("JNCException");
                deleteMethod.addLine("String path = \"" + yangLeafList.getName() + "['\"+" + YangElement.camelize(yangLeafList.getName()) + "Value" + "+\"']\";");
                deleteMethod.addLine("delete(path);");
                javaClass.addMethod(deleteMethod);
            }


        }

        /**
         *     public void addGroup() throws JNCException {
         *         setLeafListValue(NacmPrefix.NAMESPACE,
         *             "group",
         *             null,
         *             childrenNames());
         *     }
         */
        {
            JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(yangLeafList.getName()), "void").setModifiers("public");

            addMethod.setExceptions("JNCException");
            addMethod.addLine("setLeafListValue(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE,");
            addMethod.addLine("    \"" + yangLeafList.getName() + "\",");
            addMethod.addLine("    null,");
            addMethod.addLine("    childrenNames());");
            javaClass.addMethod(addMethod);

        }

        /**
         *     public void markGroupReplace(YangUnion groupValue) throws JNCException {
         *         markLeafReplace("group['" + groupValue + "']");
         *     }
         */
        Arrays.asList("Create", "Replace", "Merge", "Delete", "Remove").forEach(markType -> {
            JavaMethod markMethod = new JavaMethod("mark" + YangElement.normalize(yangLeafList.getName()) + markType, "void").setModifiers("public");
            markMethod.addParameter(leafDateTypeClassName, YangElement.camelize(yangLeafList.getName()) + "Value");
            markMethod.setExceptions("JNCException");
            markMethod.addLine("markLeaf" + markType + "(\"" + yangLeafList.getName() + "['\" + " + YangElement.camelize(yangLeafList.getName()) + "Value" + " + \"']" + "\");");
            javaClass.addMethod(markMethod);


            if (!"String".equals(leafDateTypeClassName)) {
                markMethod = new JavaMethod("mark" + YangElement.normalize(yangLeafList.getName()) + markType, "void").setModifiers("public");
                markMethod.addParameter("String", YangElement.camelize(yangLeafList.getName()) + "Value");
                markMethod.setExceptions("JNCException");
                markMethod.addLine("markLeaf" + markType + "(\"" + yangLeafList.getName() + "['\" + " + YangElement.camelize(yangLeafList.getName()) + "Value" + " + \"']" + "\");");
                javaClass.addMethod(markMethod);
            }


        });
    }

    public static void yangNodeMethond(JavaClass javaClass, YangNode yangNode, boolean container) {
        {
            JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(yangNode.getName()), yangNode.getJavaPackage() + "." + YangElement.normalize(yangNode.getName())).setModifiers("public");
            addMethod.setExceptions("JNCException");

            addMethod.addLine(yangNode.getJavaPackage() + "." + YangElement.normalize(yangNode.getName()) + " " + YangElement.camelize(yangNode.getName()) + "= new " + yangNode.getJavaPackage() + "." + YangElement.normalize(yangNode.getName()) + "();");
            if (container) {
                addMethod.addLine("this." + YangElement.camelize(yangNode.getName()) + " = " + YangElement.camelize(yangNode.getName()) + ";");
            }
            addMethod.addLine("insertChild(" + YangElement.camelize(yangNode.getName()) + ", childrenNames());");
            addMethod.addLine("return " + YangElement.camelize(yangNode.getName()) + ";");
            javaClass.addMethod(addMethod);

        }
        {
            JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(yangNode.getName()), yangNode.getJavaPackage() + "." + YangElement.normalize(yangNode.getName())).setModifiers("public");
            addMethod.setExceptions("JNCException");
//            addMethod.addDependency();

            addMethod.addDependency(JNCException.class.getName());
            addMethod.addParameter(yangNode.getJavaPackage() + "." + YangElement.normalize(yangNode.getName()), YangElement.camelize(yangNode.getName()));
            if (container) {
                addMethod.addLine("this." + YangElement.camelize(yangNode.getName()) + " = " + YangElement.camelize(yangNode.getName()) + ";");
            }
            addMethod.addLine("insertChild(" + YangElement.camelize(yangNode.getName()) + ", childrenNames());");
            addMethod.addLine("return " + YangElement.camelize(yangNode.getName()) + ";");
            javaClass.addMethod(addMethod);
        }
    }

    public static void yangJavaListMethod(JavaClass javaClass, YangJavaList yangJavaList) {


        JavaMethod iteratorMethod = new JavaMethod(YangElement.camelize(yangJavaList.getName()) + "Iterator", "ElementChildrenIterator").setModifiers("public");
        iteratorMethod.addDependency(ElementLeafListValueIterator.class.getName());
        iteratorMethod.addLine("return new ElementChildrenIterator(children, \"" + yangJavaList.getName() + "\");");
        javaClass.addMethod(iteratorMethod);
        String fullClassName = yangJavaList.getJavaPackage() + "." + YangElement.normalize(yangJavaList.getName());
        {
//getList
            /**
             *         List<Rule> list = new ArrayList<>();
             *         ElementChildrenIterator iterator = ruleIterator();
             *         if(iterator==null){
             *             return null;
             *         }
             *         while (iterator.hasNext()){
             *             Rule next =(Rule) iterator.next();
             *             list.add(next);
             *         }
             *         return list;
             */

            JavaMethod getMethod = new JavaMethod("get" + YangElement.normalize(yangJavaList.getName()) + "List", "List<" + fullClassName + ">").setModifiers("public");
//            getMethod.addDependency();

            getMethod.setJavadoc(yangJavaList.getDescription());
            getMethod.addDependency(List.class.getName());
            getMethod.addDependency(ArrayList.class.getName());
            getMethod.addDependency(ElementChildrenIterator.class.getName());
            getMethod.setExceptions("JNCException");
            getMethod.addLine("List<" + fullClassName + "> list = new ArrayList<>();");
            getMethod.addLine("ElementChildrenIterator iterator = " + YangElement.camelize(yangJavaList.getName()) + "Iterator();");
            getMethod.addLine("if(iterator==null){");
            getMethod.addLine("    return null;");
            getMethod.addLine("}");
            getMethod.addLine("while (iterator.hasNext()){");
            getMethod.addLine("    " + fullClassName + " next =(" + fullClassName + ") iterator.next();");
            getMethod.addLine("    list.add(next);");
            getMethod.addLine("}");
            getMethod.addLine("return list;");

            javaClass.addMethod(getMethod);
        }

        {

            JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(yangJavaList.getName()), fullClassName).setModifiers("public");
//            addMethod.addDependency(yangJavaList.getJavaPackage() + "." + YangElement.normalize(yangJavaList.getName()));
            addMethod.setExceptions("JNCException");
            addMethod.addLine(fullClassName + " " + YangElement.camelize(yangJavaList.getName()) + " = new " + fullClassName + "();");
            addMethod.addLine("insertChild(" + YangElement.camelize(yangJavaList.getName()) + ", childrenNames());");
            addMethod.addLine("return " + YangElement.camelize(yangJavaList.getName()) + ";");

            javaClass.addMethod(addMethod);
        }

        {

            JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(yangJavaList.getName()), fullClassName).setModifiers("public");
            addMethod.addParameter(fullClassName, YangElement.camelize(yangJavaList.getName()));
//            addMethod.addDependency(yangJavaList.getJavaPackage() + "." + YangElement.normalize(yangJavaList.getName()));
            addMethod.setExceptions("JNCException");
            addMethod.addLine("insertChild(" + YangElement.camelize(yangJavaList.getName()) + ", childrenNames());");
            addMethod.addLine("return " + YangElement.camelize(yangJavaList.getName()) + ";");

            javaClass.addMethod(addMethod);
        }
        List<YangLeaf> listOfKeyLeaf = yangJavaList.getListOfKeyLeaf();
        if (!listOfKeyLeaf.isEmpty()) {

            /////add  TODO 待修改，类型转换失败
//            {
//                JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(yangJavaList.getName()), YangElement.normalize(yangJavaList.getName())).setModifiers("public");
//
//                List<String> parameterValue = new ArrayList<>();
//                for (YangLeaf keyNodeLeaf : listOfKeyLeaf) {
//                    parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");
//
//                    YangLeaf dataTypeYangLeaf=keyNodeLeaf;
////                    while (dataTypeYangLeaf.getReferredSchema()!=null){
////                        dataTypeYangLeaf =dataTypeYangLeaf.getReferredSchema();
////                    }
//                    String jncDataType = AttributesJavaDataType.getJNCDataType(dataTypeYangLeaf.getDataType());
//
//
//                    addMethod.addParameter(jncDataType, YangElement.camelize(keyNodeLeaf.getName()) + "Value");
//                }
//
//                addMethod.addDependency(yangJavaList.getJavaPackage() + "." + YangElement.normalize(yangJavaList.getName()));
//                addMethod.setExceptions("JNCException");
//                addMethod.addLine(YangElement.normalize(yangJavaList.getName()) + " " + YangElement.camelize(yangJavaList.getName()) + " = new " + YangElement.normalize(yangJavaList.getName()) + "(" + parameterValue.stream().collect(Collectors.joining(",")) + ");");
//                addMethod.addLine("return add" + YangElement.normalize(yangJavaList.getName()) + "(" + YangElement.camelize(yangJavaList.getName()) + ");");
//
//                javaClass.addMethod(addMethod);
//            }
            ///////////////////////////add2
            {
                JavaMethod addMethod = new JavaMethod("add" + YangElement.normalize(yangJavaList.getName()), fullClassName).setModifiers("public");

                List<String> parameterValue = new ArrayList<>();
                for (YangLeaf keyNodeLeaf : listOfKeyLeaf) {
                    parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                    addMethod.addParameter("String", YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                }

//                addMethod.addDependency(yangJavaList.getJavaPackage() + "." + YangElement.normalize(yangJavaList.getName()));
                addMethod.setExceptions("JNCException");
                addMethod.addLine(fullClassName + " " + YangElement.camelize(yangJavaList.getName()) + " = new " + fullClassName + "(" + parameterValue.stream().collect(Collectors.joining(",")) + ");");
                addMethod.addLine("return add" + YangElement.normalize(yangJavaList.getName()) + "(" + YangElement.camelize(yangJavaList.getName()) + ");");

                javaClass.addMethod(addMethod);
            }

            /////get1
            {
                JavaMethod getMethod = new JavaMethod("get" + YangElement.normalize(yangJavaList.getName()), fullClassName).setModifiers("public");

                List<String> parameterValue = new ArrayList<>();
                for (YangLeaf keyNodeLeaf : listOfKeyLeaf) {
                    parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");

                    YangLeaf dataTypeYangLeaf = keyNodeLeaf;
//                    while (dataTypeYangLeaf.getReferredSchema()!=null){
//                        dataTypeYangLeaf =dataTypeYangLeaf.getReferredSchema();
//                    }
                    String jncDataType = AttributesJavaDataType.getJNCDataType(dataTypeYangLeaf.getDataType());
                    getMethod.addParameter(jncDataType, YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                }

//                getMethod.addDependency(yangJavaList.getJavaPackage() + "." + YangElement.normalize(yangJavaList.getName()));
                getMethod.setExceptions("JNCException");

                getMethod.addLine("String path = \"" + yangJavaList.getName() + "" + listOfKeyLeaf.stream().map(keyLeaf -> "[" + keyLeaf.getName() + "'\" + " + YangElement.camelize(keyLeaf.getName()) + "Value" + " + \"']").collect(Collectors.joining("")) + "\";");
                getMethod.addLine("return (" + fullClassName + ")searchOne(path);");

                javaClass.addMethod(getMethod);
            }
            /////get2
            {
                JavaMethod getMethod = new JavaMethod("get" + YangElement.normalize(yangJavaList.getName()), fullClassName).setModifiers("public");

                List<String> parameterValue = new ArrayList<>();
                for (YangLeaf keyNodeLeaf : listOfKeyLeaf) {
                    parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                    getMethod.addParameter("String", YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                }

//                getMethod.addDependency(yangJavaList.getJavaPackage() + "." + YangElement.normalize(yangJavaList.getName()));
                getMethod.setExceptions("JNCException");
                getMethod.addLine("String path = \"" + yangJavaList.getName() + "" + listOfKeyLeaf.stream().map(keyLeaf -> "[" + keyLeaf.getName() + "'\" + " + YangElement.camelize(keyLeaf.getName()) + "Value" + " + \"']").collect(Collectors.joining("")) + "\";");
                getMethod.addLine("return (" + fullClassName + ")searchOne(path);");
                javaClass.addMethod(getMethod);
            }
            //   增加delete方法1
            {
                JavaMethod deleteMethod = new JavaMethod("delete" + YangElement.normalize(yangJavaList.getName()), "void").setModifiers("public");

                List<String> parameterValue = new ArrayList<>();
                for (YangLeaf keyNodeLeaf : listOfKeyLeaf) {
                    parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");

                    YangLeaf dataTypeYangLeaf = keyNodeLeaf;
//                    while (dataTypeYangLeaf.getReferredSchema()!=null){
//                        dataTypeYangLeaf =dataTypeYangLeaf.getReferredSchema();
//                    }
                    String jncDataType = AttributesJavaDataType.getJNCDataType(dataTypeYangLeaf.getDataType());
                    deleteMethod.addParameter(jncDataType, YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                }

//                deleteMethod.addDependency(yangJavaList.getJavaPackage() + "." + YangElement.normalize(yangJavaList.getName()));
                deleteMethod.setExceptions("JNCException");

                deleteMethod.addLine("String path = \"rule" + listOfKeyLeaf.stream().map(keyLeaf -> "[" + keyLeaf.getName() + "='\" + " + YangElement.camelize(keyLeaf.getName()) + "Value + \"']").collect(Collectors.joining("")) + "\";");
                deleteMethod.addLine("delete(path);");

                javaClass.addMethod(deleteMethod);
            }
            // 增加delete方法2
            {


                JavaMethod deleteMethod = new JavaMethod("delete" + YangElement.normalize(yangJavaList.getName()), "void").setModifiers("public");

                List<String> parameterValue = new ArrayList<>();
                for (YangLeaf keyNodeLeaf : listOfKeyLeaf) {
                    parameterValue.add(YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                    deleteMethod.addParameter("String", YangElement.camelize(keyNodeLeaf.getName()) + "Value");
                }

//                deleteMethod.addDependency(yangJavaList.getJavaPackage() + "." + YangElement.normalize(yangJavaList.getName()));
                deleteMethod.setExceptions("JNCException");

                deleteMethod.addLine("String path = \"rule" + listOfKeyLeaf.stream().map(keyLeaf -> "[" + keyLeaf.getName() + "='\" + " + YangElement.camelize(keyLeaf.getName()) + "Value + \"']").collect(Collectors.joining("")) + "\";");
                deleteMethod.addLine("delete(path);");

                javaClass.addMethod(deleteMethod);
            }

        }
    }

    public static void yangJavaContainerMethod(JavaClass javaClass, YangJavaContainer yangJavaContainer) {

        javaClass.addField(new JavaField(yangJavaContainer.getJavaPackage() + "." + YangElement.normalize(yangJavaContainer.getName()), YangElement.camelize(yangJavaContainer.getName()), "null", "public"));

        {
            JavaMethod getMethod = new JavaMethod("get" + YangElement.normalize(yangJavaContainer.getName()), yangJavaContainer.getJavaPackage() + "." + YangElement.normalize(yangJavaContainer.getName())).setModifiers("public");
            getMethod.addLine("return " + YangElement.camelize(yangJavaContainer.getName()) + ";");
            javaClass.addMethod(getMethod);

        }

        /**
         *         this.groups = null;
         *         String path = "groups";
         *         return delete(path);
         */
        {
            JavaMethod delelteMethod = new JavaMethod("delete" + YangElement.normalize(yangJavaContainer.getName()), "void").setModifiers("public").setExceptions("JNCException");
            delelteMethod.addLine("this." + YangElement.camelize(yangJavaContainer.getName()) + " = null;");
            delelteMethod.addLine("String path = \"" + yangJavaContainer.getName() + "\";");
            delelteMethod.addLine("delete(path);");
            javaClass.addMethod(delelteMethod);
        }
    }
}
