/* 
 * @(#)SavedQuery.java        1.0
 *
 * This file has been auto-generated by JNC, the
 * Java output format plug-in of pyang.
 * Origin: module "tailf-webui", revision: "2013-03-07".
 */

package com.airlenet.yang.model.tailfWebui.webui.dataStores.userProfile;

import com.airlenet.yang.model.tailfWebui.WebuiPrefix;
import com.tailf.jnc.Element;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.Leaf;
import com.tailf.jnc.YangElement;
import com.tailf.jnc.YangString;

/**
 * This class represents an element from 
 * the namespace http://tail-f.com/ns/webui
 * generated to "src/main/java/com.airlenet.yang.model/tailfWebui/webui/dataStores/userProfile/saved-query"
 * <p>
 * See line 169 in
 * src/main/yang/module/tailf/tailf-webui.yang
 *
 * @version 1.0
 * @author Auto Generated
 */
public class SavedQuery extends YangElement {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for an empty SavedQuery object.
     */
    public SavedQuery() {
        super(WebuiPrefix.NAMESPACE, "saved-query");
    }

    /**
     * Constructor for an initialized SavedQuery object,
     * 
     * @param listPathValue Key argument of child.
     * @param nameValue Key argument of child.
     */
    public SavedQuery(YangString listPathValue, YangString nameValue)
            throws JNCException {
        super(WebuiPrefix.NAMESPACE, "saved-query");
        Leaf listPath = new Leaf(WebuiPrefix.NAMESPACE, "list-path");
        listPath.setValue(listPathValue);
        insertChild(listPath, childrenNames());
        Leaf name = new Leaf(WebuiPrefix.NAMESPACE, "name");
        name.setValue(nameValue);
        insertChild(name, childrenNames());
    }

    /**
     * Constructor for an initialized SavedQuery object,
     * with String keys.
     * @param listPathValue Key argument of child.
     * @param nameValue Key argument of child.
     */
    public SavedQuery(String listPathValue, String nameValue)
            throws JNCException {
        super(WebuiPrefix.NAMESPACE, "saved-query");
        Leaf listPath = new Leaf(WebuiPrefix.NAMESPACE, "list-path");
        listPath.setValue(new com.tailf.jnc.YangString(listPathValue));
        insertChild(listPath, childrenNames());
        Leaf name = new Leaf(WebuiPrefix.NAMESPACE, "name");
        name.setValue(new com.tailf.jnc.YangString(nameValue));
        insertChild(name, childrenNames());
    }

    /**
     * Clones this object, returning an exact copy.
     * @return A clone of the object.
     */
    public SavedQuery clone() {
        SavedQuery copy;
        try {
            copy = new SavedQuery(getListPathValue().toString(), getNameValue().toString());
        } catch (JNCException e) {
            copy = null;
        }
        return (SavedQuery)cloneContent(copy);
    }

    /**
     * Clones this object, returning a shallow copy.
     * @return A clone of the object. Children are not included.
     */
    public SavedQuery cloneShallow() {
        SavedQuery copy;
        try {
            copy = new SavedQuery(getListPathValue().toString(), getNameValue().toString());
        } catch (JNCException e) {
            copy = null;
        }
        return (SavedQuery)cloneShallowContent(copy);
    }

    /**
     * @return An array with the identifiers of any key children
     */
    public String[] keyNames() {
        return new String[] {
            "list-path",
            "name",
        };
    }

    /**
     * @return An array with the identifiers of any children, in order.
     */
    public String[] childrenNames() {
        return new String[] {
            "list-path",
            "name",
            "serialized-query",
        };
    }

    /* Access methods for leaf child: "list-path". */

    /**
     * Gets the value for child leaf "list-path".
     * @return The value of the leaf.
     */
    public com.tailf.jnc.YangString getListPathValue() throws JNCException {
        return (com.tailf.jnc.YangString)getValue("list-path");
    }

    /**
     * Sets the value for child leaf "list-path",
     * using instance of generated typedef class.
     * @param listPathValue The value to set.
     * @param listPathValue used during instantiation.
     */
    public void setListPathValue(com.tailf.jnc.YangString listPathValue)
            throws JNCException {
        setLeafValue(WebuiPrefix.NAMESPACE,
            "list-path",
            listPathValue,
            childrenNames());
    }

    /**
     * Sets the value for child leaf "list-path",
     * using a String value.
     * @param listPathValue used during instantiation.
     */
    public void setListPathValue(String listPathValue) throws JNCException {
        setListPathValue(new com.tailf.jnc.YangString(listPathValue));
    }

    /**
     * This method is used for creating a subtree filter.
     * The added "list-path" leaf will not have a value.
     */
    public void addListPath() throws JNCException {
        setLeafValue(WebuiPrefix.NAMESPACE,
            "list-path",
            null,
            childrenNames());
    }

    /* Access methods for leaf child: "name". */

    /**
     * Gets the value for child leaf "name".
     * @return The value of the leaf.
     */
    public com.tailf.jnc.YangString getNameValue() throws JNCException {
        return (com.tailf.jnc.YangString)getValue("name");
    }

    /**
     * Sets the value for child leaf "name",
     * using instance of generated typedef class.
     * @param nameValue The value to set.
     * @param nameValue used during instantiation.
     */
    public void setNameValue(com.tailf.jnc.YangString nameValue)
            throws JNCException {
        setLeafValue(WebuiPrefix.NAMESPACE,
            "name",
            nameValue,
            childrenNames());
    }

    /**
     * Sets the value for child leaf "name",
     * using a String value.
     * @param nameValue used during instantiation.
     */
    public void setNameValue(String nameValue) throws JNCException {
        setNameValue(new com.tailf.jnc.YangString(nameValue));
    }

    /**
     * This method is used for creating a subtree filter.
     * The added "name" leaf will not have a value.
     */
    public void addName() throws JNCException {
        setLeafValue(WebuiPrefix.NAMESPACE,
            "name",
            null,
            childrenNames());
    }

    /* Access methods for optional leaf child: "serialized-query". */

    /**
     * Gets the value for child leaf "serialized-query".
     * @return The value of the leaf.
     */
    public com.tailf.jnc.YangString getSerializedQueryValue()
            throws JNCException {
        return (com.tailf.jnc.YangString)getValue("serialized-query");
    }

    /**
     * Sets the value for child leaf "serialized-query",
     * using instance of generated typedef class.
     * @param serializedQueryValue The value to set.
     * @param serializedQueryValue used during instantiation.
     */
    public void setSerializedQueryValue(com.tailf.jnc.YangString serializedQueryValue)
            throws JNCException {
        setLeafValue(WebuiPrefix.NAMESPACE,
            "serialized-query",
            serializedQueryValue,
            childrenNames());
    }

    /**
     * Sets the value for child leaf "serialized-query",
     * using a String value.
     * @param serializedQueryValue used during instantiation.
     */
    public void setSerializedQueryValue(String serializedQueryValue)
            throws JNCException {
        setSerializedQueryValue(new com.tailf.jnc.YangString(serializedQueryValue));
    }

    /**
     * Unsets the value for child leaf "serialized-query".
     */
    public void unsetSerializedQueryValue() throws JNCException {
        delete("serialized-query");
    }

    /**
     * This method is used for creating a subtree filter.
     * The added "serialized-query" leaf will not have a value.
     */
    public void addSerializedQuery() throws JNCException {
        setLeafValue(WebuiPrefix.NAMESPACE,
            "serialized-query",
            null,
            childrenNames());
    }

    /**
     * Marks the leaf "serialized-query" with operation "replace".
     */
    public void markSerializedQueryReplace() throws JNCException {
        markLeafReplace("serialized-query");
    }

    /**
     * Marks the leaf "serialized-query" with operation "merge".
     */
    public void markSerializedQueryMerge() throws JNCException {
        markLeafMerge("serialized-query");
    }

    /**
     * Marks the leaf "serialized-query" with operation "create".
     */
    public void markSerializedQueryCreate() throws JNCException {
        markLeafCreate("serialized-query");
    }

    /**
     * Marks the leaf "serialized-query" with operation "delete".
     */
    public void markSerializedQueryDelete() throws JNCException {
        markLeafDelete("serialized-query");
    }

    /**
     * Marks the leaf "serialized-query" with operation "remove".
     */
    public void markSerializedQueryRemove() throws JNCException {
        markLeafRemove("serialized-query");
    }

    /**
     * Support method for addChild.
     * Adds a child to this object.
     * 
     * @param child The child to add
     */
    public void addChild(Element child) {
        super.addChild(child);
    }

}