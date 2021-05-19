package com.airlenet.yang.compiler.translator.tojava.jnc;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JavaField {
    private String[] modifiers;
    private String type;
    private String name;
    private String value;
    private List<String> imports;
    private String javadoc;
    public JavaField(String type,String name, String value, String... modifiers) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.modifiers = modifiers;
    }

    public List<String> getImports() {
        return imports;
    }
    public JavaField setJavadoc(String javadoc) {
        this.javadoc=(javadoc);
        return this;
    }
    public String toJavaCode() {
        StringBuilder builder = new StringBuilder();
        if(this.javadoc!=null){
            builder.append("\t/**").append("\n\t * ").append(Arrays.stream(this.javadoc.split("\n")).map(l->l.trim()).collect(Collectors.joining("\n\t * "))).append("\n\t */");
        }
        builder.append("\n\t");
        if (this.modifiers != null) {
            for (String modifier : modifiers) {
                builder.append(modifier);
                builder.append(" ");
            }
        }

        builder.append(type);
        builder.append(" ");
        builder.append(name);

        builder.append(" = ");
        builder.append(value);
        builder.append(";");

        return builder.toString();
    }

}
