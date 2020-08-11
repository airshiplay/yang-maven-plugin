package com.airlenet.yang.compiler.translator.tojava.jnc;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaClass {
    private String name;
    private String packageName;
    private String extend;
    private String description;
    private List<String> imports;
    private List<JavaField> fields;
    private List<JavaMethod> methods;

    public JavaClass(String name, String packageName, String description) {
        this.name = name;
        this.packageName = packageName;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public JavaClass addDependency(String line) {
        if (this.imports == null) {
            this.imports = new ArrayList<>();
        }
        this.imports.add(line);
        return this;
    }

    public JavaClass setExtend(String extend) {
        this.extend = extend;
        return this;
    }

    public JavaClass addDependency(List<String> line) {
        if (this.imports == null) {
            this.imports = new ArrayList<>();
        }
        this.imports.addAll(line);
        return this;
    }

    public void addField(JavaField... fields) {
        if (this.fields == null) {
            this.fields = new ArrayList<>();
        }

        List<JavaField> fieldList = Arrays.asList(fields);
        List<String> importList = fieldList.stream().filter(javaField -> javaField.getImports() != null).map(javaField -> javaField.getImports()).flatMap(imports -> imports.stream()).collect(Collectors.toList());
        addDependency(importList);
        this.fields.addAll(Arrays.asList(fields));
    }

    public void addMethod(JavaMethod... methods) {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
        }

        List<JavaMethod> javaMethodList = Arrays.asList(methods);
        List<String> importList = javaMethodList.stream().filter(javaMethod -> javaMethod.getImports() != null).map(javaMethod -> javaMethod.getImports()).flatMap(imports -> imports.stream()).collect(Collectors.toList());
        addDependency(importList);
        this.methods.addAll(javaMethodList);
    }

    public void write(String path) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n\n");
        if (this.imports != null) {
            Collections.sort(this.imports);
            int size = this.imports.size();
            String preImport = null;
            for (int i = 0; i < size; i++) {
                String im = imports.get(i);
                if(preImport !=null && preImport.equals(im)){
                    continue;
                }
                preImport =im;
                builder.append("import ");
                builder.append(im);
                builder.append(";\n");
                String nextImport = null;
                if (i < size-1) {
                    nextImport = imports.get(i + 1);
                }
                if (nextImport != null && !im.substring(0, im.lastIndexOf(".")).equals(nextImport.substring(0, nextImport.lastIndexOf(".")))) {
                    builder.append("\n");
                }
            }
        }
        builder.append("\n");
        builder.append("/**\n");
        builder.append(" * "+description+"\n");
        builder.append(" */\n");
        builder.append("public class ").append(name)
            ;
        if(extend!=null){
            builder.append(" extends ");
            builder.append(extend);
            builder.append(" ");
        }

        builder     .append(" {\n");

        if (this.fields != null) {
            for (JavaField field : fields) {
                builder.append(field.toJavaCode());
                builder.append("\n");
            }
        }
        builder.append("\n");
        if (this.methods != null) {
            for (JavaMethod method : methods) {
                builder.append(method.toJavaCode());
                builder.append("\n\n");
            }
        }

        builder.append("}");


        FileUtils.write(new File(path + File.separator + name + ".java"), builder.toString());
    }
}
