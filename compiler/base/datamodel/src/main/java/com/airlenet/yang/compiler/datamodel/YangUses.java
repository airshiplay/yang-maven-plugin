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
package com.airlenet.yang.compiler.datamodel;

import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.exceptions.ErrorMessages;
import com.airlenet.yang.compiler.datamodel.utils.DataModelUtils;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.datamodel.utils.ResolvableStatus;
import com.airlenet.yang.compiler.datamodel.utils.YangConstructType;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;

/*-
 * Reference RFC 6020.
 *
 * The "uses" statement is used to reference a "grouping" definition. It takes
 * one argument, which is the name of the grouping.
 *
 * The effect of a "uses" reference to a grouping is that the nodes defined by
 * the grouping are copied into the current schema tree, and then updated
 * according to the "refine" and "augment" statements.
 *
 * The identifiers defined in the grouping are not bound to a namespace until
 * the contents of the grouping are added to the schema tree via a "uses"
 * statement that does not appear inside a "grouping" statement, at which point
 * they are bound to the namespace of the current module.
 *
 * The uses's sub-statements
 *
 *                +--------------+---------+-------------+------------------+
 *                | substatement | section | cardinality |data model mapping|
 *                +--------------+---------+-------------+------------------+
 *                | augment      | 7.15    | 0..1        | -child nodes     |
 *                | description  | 7.19.3  | 0..1        | -string          |
 *                | if-feature   | 7.18.2  | 0..n        | -YangIfFeature   |
 *                | refine       | 7.12.2  | 0..1        | -TODO            |
 *                | reference    | 7.19.4  | 0..1        | -string          |
 *                | status       | 7.19.2  | 0..1        | -YangStatus      |
 *                | when         | 7.19.5  | 0..1        | -YangWhen        |
 *                +--------------+---------+-------------+------------------+
 */

/**
 * Represents data model node to maintain information defined in YANG uses.
 */
public abstract class YangUses
        extends YangNode
        implements YangCommonInfo, Parsable, Resolvable, CollisionDetector,
        YangWhenHolder, YangIfFeatureHolder, YangTranslatorOperatorNode,
        LeafRefInvalidHolder, DefaultDenyWriteExtension,
        DefaultDenyAllExtension {

    private static final long serialVersionUID = 806201617L;

    /**
     * YANG node identifier.
     */
    private YangNodeIdentifier nodeIdentifier;

    /**
     * Referred group.
     */
    private YangGrouping refGroup;

    /**
     * Description of YANG uses.
     */
    private String description;

    /**
     * YANG reference.
     */
    private String reference;

    /**
     * Status of YANG uses.
     */
    private YangStatusType status;

    /**
     * When data of the node.
     */
    private YangWhen when;

    /**
     * List of if-feature.
     */
    private List<YangIfFeature> ifFeatureList;

    /**
     * Status of resolution. If completely resolved enum value is "RESOLVED",
     * if not enum value is "UNRESOLVED", in case reference of grouping/typedef
     * is added to uses/type but it's not resolved value of enum should be
     * "INTRA_FILE_RESOLVED".
     */
    private ResolvableStatus resolvableStatus;

    /**
     * Effective list of leaf lists of grouping that needs to replicated at YANG uses.
     */
    private List<YangEntityToResolveInfoImpl> entityToResolveInfoList;

    /**
     * Current grouping depth for uses.
     */
    private int currentGroupingDepth;

    /**
     * References the extension default-deny-write.
     */
    private boolean defaultDenyWrite;

    /**
     * References the extension default-deny-all.
     */
    private boolean defaultDenyAll;

    /**
     * Status of cloning of YANG uses.
     */
    private boolean isCloned;

    /**
     * Creates an YANG uses node.
     */
    public YangUses() {
        super(YangNodeType.USES_NODE, null);
        nodeIdentifier = new YangNodeIdentifier();
        resolvableStatus = ResolvableStatus.UNRESOLVED;
        ifFeatureList = new LinkedList<>();
        entityToResolveInfoList = new LinkedList<>();
    }

    @Override
    public void addToChildSchemaMap(YangSchemaNodeIdentifier id,
                                    YangSchemaNodeContextInfo context)
            throws DataModelException {
        // Do nothing.
    }

    @Override
    public void incrementMandatoryChildCount() {
        // Do nothing.
        // TODO
    }

    @Override
    public void addToDefaultChildMap(YangSchemaNodeIdentifier id,
                                     YangSchemaNode yangSchemaNode) {
        // Do nothing.
        // TODO
    }

    @Override
    public YangSchemaNodeType getYangSchemaNodeType() {
        return YangSchemaNodeType.YANG_NON_DATA_NODE;
    }

    /**
     * Adds an entity to resolve in list.
     *
     * @param entityToResolve entity to resolved
     * @throws DataModelException a violation of data model rules
     */
    public void addEntityToResolve(
            List<YangEntityToResolveInfoImpl> entityToResolve)
            throws DataModelException {
        if (entityToResolveInfoList == null) {
            entityToResolveInfoList = new
                    LinkedList<>();
        }
        entityToResolveInfoList.addAll(entityToResolve);
    }

    /**
     * Returns the referred group.
     *
     * @return the referred group
     */
    public YangGrouping getRefGroup() {
        return refGroup;
    }

    /**
     * Sets the referred group.
     *
     * @param refGroup the referred group
     */
    public void setRefGroup(YangGrouping refGroup) {
        this.refGroup = refGroup;
    }

    /**
     * Returns the when.
     *
     * @return the when
     */
    @Override
    public YangWhen getWhen() {
        return when;
    }

    /**
     * Sets the when.
     *
     * @param when the when to set
     */
    @Override
    public void setWhen(YangWhen when) {
        this.when = when;
    }

    /**
     * Returns the description.
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description set the description
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the textual reference.
     *
     * @return the reference
     */
    @Override
    public String getReference() {
        return reference;
    }

    /**
     * Sets the textual reference.
     *
     * @param reference the reference to set
     */
    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    @Override
    public YangStatusType getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status to set
     */
    @Override
    public void setStatus(YangStatusType status) {
        this.status = status;
    }

    /**
     * Returns the type of the data.
     *
     * @return returns USES_DATA
     */
    @Override
    public YangConstructType getYangConstructType() {
        return YangConstructType.USES_DATA;
    }

    /**
     * Validates the data on entering the corresponding parse tree node.
     *
     * @throws DataModelException a violation of data model rules
     */
    @Override
    public void validateDataOnEntry()
            throws DataModelException {
        // TODO auto-generated method stub, to be implemented by parser
    }

    /**
     * Validates the data on exiting the corresponding parse tree node.
     *
     * @throws DataModelException a violation of data model rules
     */
    @Override
    public void validateDataOnExit()
            throws DataModelException {
        // TODO auto-generated method stub, to be implemented by parser
    }

    /**
     * Returns node identifier.
     *
     * @return node identifier
     */
    public YangNodeIdentifier getNodeIdentifier() {
        return nodeIdentifier;
    }

    /**
     * Sets node identifier.
     *
     * @param nodeIdentifier the node identifier
     */
    public void setNodeIdentifier(YangNodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    /**
     * Returns prefix associated with uses.
     *
     * @return prefix associated with uses
     */
    public String getPrefix() {
        return nodeIdentifier.getPrefix();
    }

    /**
     * Get prefix associated with uses.
     *
     * @param prefix prefix associated with uses
     */
    public void setPrefix(String prefix) {
        nodeIdentifier.setPrefix(prefix);
    }

    @Override
    public Object resolve()
            throws DataModelException {

        YangGrouping referredGrouping = getRefGroup();

        if (referredGrouping == null) {
            throw new DataModelException("YANG uses linker error, cannot resolve" +
                                                 " uses " + getName() + " in " +
                                                 getLineNumber() + " at " +
                                                 getCharPosition() + " in " +
                                                 getFileName() + "\"");
        } else {
            /*
             * if referredGrouping has uses which is not resolved then set the status
             * as Intra file resolved and return
             */
            if (checkIsUnresolvedRecursiveUsesInGrouping(referredGrouping)) {
                return null;
            }
        }

        YangNode usesParentNode = DataModelUtils.getParentNodeInGenCode(this);
        if (!(usesParentNode instanceof YangLeavesHolder)
                || !(usesParentNode instanceof CollisionDetector)) {
            throw new DataModelException(
                    "YANG uses holder construct is wrong " + getName() + " in " +
                            getLineNumber() + " at " + getCharPosition() +
                            " in " + getFileName() + "\"");
        }

        YangLeavesHolder usesParent = (YangLeavesHolder) usesParentNode;
        if (referredGrouping.getListOfLeaf() != null) {
            for (YangLeaf leaf : referredGrouping.getListOfLeaf()) {
                YangLeaf clonedLeaf;
                try {
                    ((CollisionDetector) usesParent)
                            .detectCollidingChild(leaf.getName(), YangConstructType.LEAF_DATA);
                    clonedLeaf = leaf.clone();
                    clonedLeaf.setReferredLeaf(leaf);
                    DataModelUtils.addUnresolvedType(this, clonedLeaf, (YangNode) usesParent);
                } catch (CloneNotSupportedException | DataModelException e) {
                    throw new DataModelException(e.getMessage());
                }

                clonedLeaf.setContainedIn(usesParent);
                usesParent.addLeaf(clonedLeaf);
                if (usesParent instanceof YangList) {
                    Set<String> keys = ((YangList) usesParent).getKeyLeaf();
                    if (keys.contains(clonedLeaf.getName())) {
                        clonedLeaf.setKeyLeaf(true);
                    }
                }
            }
        }
        if (referredGrouping.getListOfLeafList() != null) {
            for (YangLeafList leafList : referredGrouping.getListOfLeafList()) {
                YangLeafList clonedLeafList;
                try {
                    ((CollisionDetector) usesParent)
                            .detectCollidingChild(leafList.getName(), YangConstructType.LEAF_LIST_DATA);
                    clonedLeafList = leafList.clone();
                    clonedLeafList.setReferredSchemaLeafList(leafList);
                    DataModelUtils.addUnresolvedType(this, clonedLeafList,
                                      (YangNode) usesParent);
                } catch (CloneNotSupportedException | DataModelException e) {
                    throw new DataModelException(e.getMessage());
                }
                clonedLeafList.setContainedIn(usesParent);
                usesParent.addLeafList(clonedLeafList);
            }
        }

        try {
            cloneGroupingTree(referredGrouping, usesParentNode, this, false);
        } catch (DataModelException e) {
            throw new DataModelException(e.getMessage());
        }
        DataModelUtils.updateClonedLeavesUnionEnumRef(usesParent);
        return unmodifiableList(entityToResolveInfoList);
    }

    /**
     * Checks if referred grouping has uses which is not resolved then it set the
     * status of current uses as intra file resolved and returns true.
     *
     * @param referredGrouping referred grouping node of uses
     * @return true if referred grouping has unresolved uses
     */
    private boolean checkIsUnresolvedRecursiveUsesInGrouping(YangGrouping referredGrouping) {

        /*
         * Search the grouping node's children for presence of uses node.
         */
        TraversalType curTraversal = TraversalType.ROOT;
        YangNode curNode = referredGrouping.getChild();
        while (curNode != null) {
            if (curNode == referredGrouping || (curNode instanceof YangUses &&
                    curNode.getName().equals(referredGrouping.getName()))) {
                // if we have traversed all the child nodes, then exit from loop
                return false;
            }

            // if child nodes has uses, then add it to resolution stack
            if (curNode instanceof YangUses) {
                if (((YangUses) curNode).getResolvableStatus() != ResolvableStatus.RESOLVED) {
                    setResolvableStatus(ResolvableStatus.INTRA_FILE_RESOLVED);
                    return true;
                }
            }

            // Traversing all the child nodes of grouping
            if (curTraversal != TraversalType.PARENT && curNode.getChild() != null) {
                curTraversal = TraversalType.CHILD;
                curNode = curNode.getChild();
            } else if (curNode.getNextSibling() != null) {
                curTraversal = TraversalType.SIBLING;
                curNode = curNode.getNextSibling();
            } else {
                curTraversal = TraversalType.PARENT;
                curNode = curNode.getParent();
            }
        }
        return false;
    }

    @Override
    public ResolvableStatus getResolvableStatus() {
        return resolvableStatus;
    }

    @Override
    public void setResolvableStatus(ResolvableStatus resolvableStatus) {
        this.resolvableStatus = resolvableStatus;
    }

    @Override
    public void detectCollidingChild(String identifierName, YangConstructType dataType)
            throws DataModelException {
        DataModelUtils.detectCollidingChildUtil(identifierName, dataType, this);
    }

    @Override
    public void detectSelfCollision(String identifierName, YangConstructType dataType)
            throws DataModelException {

        if (getName().equals(identifierName)) {
            throw new DataModelException(
                    ErrorMessages.getErrorMsgCollision(ErrorMessages.COLLISION_DETECTION, getName(),
                                         getLineNumber(), getCharPosition(),
                                         ErrorMessages.USES, getFileName()));
        }
    }

    @Override
    public List<YangIfFeature> getIfFeatureList() {
        return unmodifiableList(ifFeatureList);
    }

    @Override
    public void addIfFeatureList(YangIfFeature ifFeature) {
        if (ifFeatureList == null) {
            ifFeatureList = new LinkedList<>();
        }
        ifFeatureList.add(ifFeature);
    }

    @Override
    public void setIfFeatureList(List<YangIfFeature> ifFeatureList) {
        this.ifFeatureList = ifFeatureList;
    }

    /**
     * Sets the current grouping depth.
     *
     * @param currentGroupingDepth current grouping depth
     */
    public void setCurrentGroupingDepth(int currentGroupingDepth) {
        this.currentGroupingDepth = currentGroupingDepth;
    }

    /**
     * Returns the current grouping depth.
     *
     * @return current grouping depth
     */
    public int getCurrentGroupingDepth() {
        return currentGroupingDepth;
    }

    @Override
    public String getName() {
        return nodeIdentifier.getName();
    }

    @Override
    public void setName(String name) {
        nodeIdentifier.setName(name);
    }

    @Override
    public YangNode clone(YangUses node, boolean isDeviation, boolean isAnyData) throws
            CloneNotSupportedException {
        YangNode clnNode = (YangNode) super.clone();
        clnNode.setParent(null);
        clnNode.setChild(null);
        clnNode.setNextSibling(null);
        clnNode.setPreviousSibling(null);
        return clnNode;
    }

    @Override
    public boolean getDefaultDenyWrite() {
        return defaultDenyWrite;
    }

    @Override
    public void setDefaultDenyWrite(boolean defaultDenyWrite) {
        this.defaultDenyWrite = defaultDenyWrite;
    }

    @Override
    public boolean getDefaultDenyAll() {
        return defaultDenyAll;
    }

    @Override
    public void setDefaultDenyAll(boolean defaultDenyAll) {
        this.defaultDenyAll = defaultDenyAll;
    }

    /**
     * Returns true if YANG uses is cloned; false otherwise.
     *
     * @return true if cloned; false otherwise
     */
    public boolean isCloned() {
        return isCloned;
    }

    /**
     * Sets the YANG uses cloned status.
     *
     * @param cloned cloned status
     */
    public void setCloned(boolean cloned) {
        isCloned = cloned;
    }
}
