/* 
 * @(#)Groups.java        1.0
 *
 * This file has been auto-generated by JNC, the
 * Java output format plug-in of pyang.
 * Origin: module "ietf-netconf-acm", revision: "2012-02-22".
 */

package com.airlenet.yang.model.ietfNetconfAcm.nacm;

import com.airlenet.yang.model.ietfNetconfAcm.NacmPrefix;
import com.airlenet.yang.model.ietfNetconfAcm.nacm.groups.Group;
import com.tailf.jnc.Element;
import com.tailf.jnc.ElementChildrenIterator;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.YangElement;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an element from 
 * the namespace urn:ietf:params:xml:ns:yang:ietf-netconf-acm
 * generated to "src/main/java/com.airlenet.yang.model/ietfNetconfAcm/nacm/groups"
 * <p>
 * See line 282 in
 * src/main/yang/module/ietf/ietf-netconf-acm.yang
 *
 * @version 1.0
 * @author Auto Generated
 */
public class Groups extends YangElement {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for an empty Groups object.
     */
    public Groups() {
        super(NacmPrefix.NAMESPACE, "groups");
    }

    /**
     * Clones this object, returning an exact copy.
     * @return A clone of the object.
     */
    public Groups clone() {
        return (Groups)cloneContent(new Groups());
    }

    /**
     * Clones this object, returning a shallow copy.
     * @return A clone of the object. Children are not included.
     */
    public Groups cloneShallow() {
        return (Groups)cloneShallowContent(new Groups());
    }

    /**
     * @return An array with the identifiers of any key children
     */
    public String[] keyNames() {
        return null;
    }

    /**
     * @return An array with the identifiers of any children, in order.
     */
    public String[] childrenNames() {
        return new String[] {
            "group",
        };
    }

    /* Access methods for list child: "group". */

    /**
     * Gets list entry "group", with specified keys.
     * @param nameValue Key argument of child.
     */
    public Group getGroup(com.airlenet.yang.model.ietfNetconfAcm.GroupNameType nameValue)
            throws JNCException {
        String path = "group[name='" + nameValue + "']";
        return (Group)searchOne(path);
    }

    /**
     * Gets list entry "group", with specified keys.
     * The keys are specified as strings.
     * @param nameValue Key argument of child.
     */
    public Group getGroup(String nameValue) throws JNCException {
        String path = "group[name='" + nameValue + "']";
        return (Group)searchOne(path);
    }

    /**
     * Iterator method for the list "group".
     * @return An iterator for the list.
     */
    public ElementChildrenIterator groupIterator() {
        return new ElementChildrenIterator(children, "group");
    }

    /**
     * List method for the list "group".
     * @return An List for the list.
     */
    public List<Group> getGroupList() {
        List<Group> list = new ArrayList<>();
        ElementChildrenIterator iterator = groupIterator();
        if(iterator==null){
            return null;
        }
        while (iterator.hasNext()){
            Group next =(Group) iterator.next();
            list.add(next);
        }
        return list;
    }

    /**
     * Adds list entry "group", using an existing object.
     * @param group The object to add.
     * @return The added child.
     */
    public Group addGroup(Group group) throws JNCException {
        insertChild(group, childrenNames());
        return group;
    }

    /**
     * Adds list entry "group", with specified keys.
     * @param nameValue Key argument of child.
     * @return The added child.
     */
    public Group addGroup(com.airlenet.yang.model.ietfNetconfAcm.GroupNameType nameValue)
            throws JNCException {
        Group group = new Group(nameValue);
        return addGroup(group);
    }

    /**
     * Adds list entry "group", with specified keys.
     * The keys are specified as strings.
     * @param nameValue Key argument of child.
     * @return The added child.
     */
    public Group addGroup(String nameValue) throws JNCException {
        Group group = new Group(nameValue);
        return addGroup(group);
    }

    /**
     * Adds list entry "group".
     * This method is used for creating subtree filters.
     * @return The added child.
     */
    public Group addGroup() throws JNCException {
        Group group = new Group();
        insertChild(group, childrenNames());
        return group;
    }

    /**
     * Deletes list entry "group", with specified keys.
     * @param nameValue Key argument of child.
     */
    public void deleteGroup(com.airlenet.yang.model.ietfNetconfAcm.GroupNameType nameValue)
            throws JNCException {
        String path = "group[name='" + nameValue + "']";
        delete(path);
    }

    /**
     * Deletes list entry "group", with specified keys.
     * The keys are specified as strings.
     * @param nameValue Key argument of child.
     */
    public void deleteGroup(String nameValue) throws JNCException {
        String path = "group[name='" + nameValue + "']";
        delete(path);
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