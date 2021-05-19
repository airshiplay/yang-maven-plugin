package com.tailf.jnc;



public class YangAnyXml extends YangElement {


    /**
     * Constructor that creates a new element tree. An element consists of a
     * name that belongs to a namespace.
     *
     * @param ns   Namespace
     * @param name Name of the element
     */
    public YangAnyXml(String ns, String name) {
        super(ns, name);
    }

    @Override
    public String[] keyNames() {
        return new String[0];
    }

    @Override
    public YangAnyXml cloneShallow() {
        return (YangAnyXml) cloneShallowContent(new YangAnyXml(namespace, name));
    }


    @Override
    public YangAnyXml clone() {
        YangAnyXml copy = new YangAnyXml(namespace, name);
        if (children != null) {
            if (copy.children == null) {
                copy.children = new NodeSet();
            }

            for (int i = 0; i < children.size(); i++) {
                final Element child = children.getElement(i);
                final Element childCopy = (Element) child.clone();
                copy.addChild(childCopy);
            }
        }

        cloneAttrs(copy);
        cloneValue(copy);
        return copy;
    }

//    @Override
//    public Element cloneWithoutChildren() {
//        return new YangAnyXml(namespace, name);
//    }

    @Override
    public String[] childrenNames() {
        return children != null ? children.stream().map(c -> c.name).toArray(String[]::new) : new String[0];
    }
}
