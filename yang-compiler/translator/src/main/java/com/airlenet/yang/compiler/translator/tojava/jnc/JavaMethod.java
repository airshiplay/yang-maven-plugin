package com.airlenet.yang.compiler.translator.tojava.jnc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JavaMethod {
    private String name;
    private String returnType;
    private String[] exceptions;
    private List<String> imports;
    private List<String> parameters;
    private String[] modifiers;
    private List<String> javadocs;
    private List<String> lines;


    public JavaMethod(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
    }


    public JavaMethod setExceptions(String... exceptions) {
        this.exceptions = exceptions;
        return this;
    }


    public JavaMethod setModifiers(String... modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public JavaMethod setJavadocs(List<String> javadoc) {
        this.javadocs = javadoc;
        return this;
    }

    public JavaMethod addJavadoc(String javadoc) {
        if (this.javadocs == null) {
            this.javadocs = new ArrayList<>();
        }
        this.javadocs.add(javadoc);
        return this;
    }

    public JavaMethod setLines(List<String> lines) {
        this.lines = lines;
        return this;
    }

    public JavaMethod addParameter(String type, String name) {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.parameters.add(type + " " + name);
        return this;
    }

    public JavaMethod addLine(String line) {
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        this.lines.add(line);
        return this;
    }

    public JavaMethod addDependency(String line) {
        if (this.imports == null) {
            this.imports = new ArrayList<>();
        }
        this.imports.add(line);
        return this;
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
        builder.append(returnType);
        builder.append(" ");
        builder.append(name);

        builder.append("(");
        if (this.parameters != null) {
            builder.append(this.parameters.stream().collect(Collectors.joining(",")));
        }
        builder.append(") ");
        if (this.exceptions != null) {
            builder.append("throws");
            builder.append(" ");
            builder.append(Arrays.asList(exceptions).stream().collect(Collectors.joining(",")));

        }
        builder.append(" {\n");
        if (this.lines != null) {
            for (String line : lines) {
                builder.append("\t").append("\t");
                builder.append(line);
                builder.append("\n");
            }
        }
        builder.append("\t}");
        return builder.toString();
    }

}
