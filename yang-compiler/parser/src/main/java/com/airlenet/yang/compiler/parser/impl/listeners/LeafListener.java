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

/**
 * Implements listener based call back function corresponding to the "leaf"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
package com.airlenet.yang.compiler.parser.impl.listeners;

import com.airlenet.yang.compiler.datamodel.YangLeaf;
import com.airlenet.yang.compiler.datamodel.YangLeavesHolder;
import com.airlenet.yang.compiler.datamodel.YangList;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerValidation;

import static com.airlenet.yang.compiler.datamodel.utils.GeneratedLanguage.JAVA_GENERATION;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.CONFIG_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DESCRIPTION_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.LEAF_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.MANDATORY_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.REFERENCE_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.STATUS_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.TYPE_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.UNITS_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.LeafStatementContext;
import static com.airlenet.yang.compiler.translator.tojava.YangDataModelFactory.getYangLeaf;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 *  leaf-stmt           = leaf-keyword sep identifier-arg-str optsep
 *                        "{" stmtsep
 *                            ;; these stmts can appear in any order
 *                            [when-stmt stmtsep]
 *                            *(if-feature-stmt stmtsep)
 *                            type-stmt stmtsep
 *                            [units-stmt stmtsep]
 *                            *(must-stmt stmtsep)
 *                            [default-stmt stmtsep]
 *                            [config-stmt stmtsep]
 *                            [mandatory-stmt stmtsep]
 *                            [status-stmt stmtsep]
 *                            [description-stmt stmtsep]
 *                            [reference-stmt stmtsep]
 *                         "}"
 *
 * ANTLR grammar rule
 *  leafStatement : LEAF_KEYWORD identifier LEFT_CURLY_BRACE (whenStatement | ifFeatureStatement | typeStatement |
 *  unitsStatement | mustStatement | defaultStatement | configStatement | mandatoryStatement | statusStatement  |
 *  descriptionStatement | referenceStatement)* RIGHT_CURLY_BRACE;
 */

/**
 * Represents listener based call back function corresponding to the "leaf" rule
 * defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class LeafListener {

    /**
     * Creates a new leaf listener.
     */
    private LeafListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar rule
     * (leaf), performs validation and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processLeafEntry(TreeWalkListener listener,
                                        LeafStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, LEAF_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY);

        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), LEAF_DATA, ctx);

        // Validate sub statement cardinality.
        validateSubStatementsCardinality(ctx);

        // Check for identifier collision
        int line = ctx.getStart().getLine();
        int charPositionInLine = ctx.getStart().getCharPositionInLine();
        ListenerCollisionDetector.detectCollidingChildUtil(listener, line, charPositionInLine, identifier, LEAF_DATA);

        YangLeaf leaf = getYangLeaf(JAVA_GENERATION);
        leaf.setName(identifier);
        leaf.setLineNumber(line);
        leaf.setCharPosition(charPositionInLine);
        leaf.setFileName(listener.getFileName());

        /*
         * If "config" is not specified, the default is the same as the parent
         * schema node's "config" value.
         */
        if (ctx.configStatement().isEmpty()) {
            boolean parentConfig = ListenerValidation.getParentNodeConfig(listener);
            leaf.setConfig(parentConfig);
        }

        Parsable tmpData = listener.getParsedDataStack().peek();
        YangLeavesHolder leavesHolder;

        if (tmpData instanceof YangLeavesHolder) {
            leavesHolder = (YangLeavesHolder) tmpData;
            leavesHolder.addLeaf(leaf);
            leaf.setContainedIn(leavesHolder);
            if (tmpData instanceof YangList) {
                YangList list = (YangList) tmpData;
                for (String key : list.getKeyList()) {
                    if (key.equals(leaf.getName())) {
                        leaf.setKeyLeaf(true);
                    }
                }
            }
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, LEAF_DATA,
                    ctx.identifier().getText(), ListenerErrorLocation.ENTRY));
        }

        listener.getParsedDataStack().push(leaf);
    }

    /**
     * It is called when parser exits from grammar rule (leaf), performs
     * validation and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processLeafExit(TreeWalkListener listener,
                                       LeafStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, LEAF_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT);

        if (listener.getParsedDataStack().peek() instanceof YangLeaf) {
            YangLeaf leafNode = (YangLeaf) listener.getParsedDataStack().peek();
            try {
                leafNode.validateDataOnExit();
            } catch (DataModelException e) {
                throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_CONTENT, LEAF_DATA,
                        ctx.identifier().getText(), ListenerErrorLocation.EXIT));
            }
            listener.getParsedDataStack().pop();
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, LEAF_DATA,
                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }
    }

    /**
     * Validates the cardinality of leaf sub-statements as per grammar.
     *
     * @param ctx context object of the grammar rule
     */
    private static void validateSubStatementsCardinality(LeafStatementContext ctx) {

        ListenerValidation.validateCardinalityEqualsOne(ctx.typeStatement(), TYPE_DATA, LEAF_DATA, ctx.identifier().getText(), ctx);
        ListenerValidation.validateCardinalityMaxOne(ctx.unitsStatement(), UNITS_DATA, LEAF_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.configStatement(), CONFIG_DATA, LEAF_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.mandatoryStatement(), MANDATORY_DATA, LEAF_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.descriptionStatement(), DESCRIPTION_DATA, LEAF_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.referenceStatement(), REFERENCE_DATA, LEAF_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.statusStatement(), STATUS_DATA, LEAF_DATA, ctx.identifier().getText());
        //TODO when.
    }
}
