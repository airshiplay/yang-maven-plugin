package com.airlenet.yang.compiler.translator.tojava.jnc;


import java.util.List;

public class JavaField {
    private String[] modifiers;
    private String name;
    private String value;
    private List<String> imports;
    public JavaField(String name, String value, String... modifiers) {
        this.name = name;
        this.value = value;
        this.modifiers = modifiers;
    }

    public List<String> getImports() {
        return imports;
    }

    public String toJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("\t");
        if (this.modifiers != null) {
            for (String modifier : modifiers) {
                builder.append(modifier);
                builder.append(" ");
            }
        }

        builder.append(name);

        builder.append(" = \"");
        builder.append(value);
        builder.append("\";");

        return builder.toString();
    }

}
