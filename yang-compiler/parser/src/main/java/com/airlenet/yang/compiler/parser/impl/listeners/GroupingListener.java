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

package com.airlenet.yang.compiler.parser.impl.listeners;

import com.airlenet.yang.compiler.datamodel.YangContainer;
import com.airlenet.yang.compiler.datamodel.YangGrouping;
import com.airlenet.yang.compiler.datamodel.YangInput;
import com.airlenet.yang.compiler.datamodel.YangList;
import com.airlenet.yang.compiler.datamodel.YangModule;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.YangNotification;
import com.airlenet.yang.compiler.datamodel.YangOutput;
import com.airlenet.yang.compiler.datamodel.YangRpc;
import com.airlenet.yang.compiler.datamodel.YangSubModule;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import static com.airlenet.yang.compiler.datamodel.utils.GeneratedLanguage.JAVA_GENERATION;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DESCRIPTION_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.GROUPING_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.REFERENCE_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.STATUS_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.GroupingStatementContext;
import static com.airlenet.yang.compiler.translator.tojava.YangDataModelFactory.getYangGroupingNode;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 * grouping-stmt       = grouping-keyword sep identifier-arg-str optsep
 *                      (";" /
 *                       "{" stmtsep
 *                          ;; these stmts can appear in any order
 *                          [status-stmt stmtsep]
 *                           [description-stmt stmtsep]
 *                           [reference-stmt stmtsep]
 *                           *((typedef-stmt /
 *                              grouping-stmt) stmtsep)
 *                           *(data-def-stmt stmtsep)
 *                       "}")
 *
 * ANTLR grammar rule
 * groupingStatement : GROUPING_KEYWORD identifier (STMTEND | LEFT_CURLY_BRACE
 *       (statusStatement | descriptionStatement | referenceStatement | typedefStatement | groupingStatement
 *       | dataDefStatement)* RIGHT_CURLY_BRACE);
 */

/**
 * Represents listener based call back function corresponding to the "grouping"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class GroupingListener {

    /**
     * Creates a new grouping listener.
     */
    private GroupingListener() {
    }

    /**
     * It is called when parser enters grammar rule (grouping), it perform
     * validations and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processGroupingEntry(TreeWalkListener listener,
                                            GroupingStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, GROUPING_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY);

        // Check validity of identifier and remove double quotes.
        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), GROUPING_DATA, ctx);

        // Validate sub statement cardinality.
        validateSubStatementsCardinality(ctx);

        // Increase the grouping count by one.
        listener.increaseGroupingDepth();
        Parsable curData = listener.getParsedDataStack().peek();

        // Check for identifier collision
        int line = ctx.getStart().getLine();
        int charPositionInLine = ctx.getStart().getCharPositionInLine();
        ListenerCollisionDetector.detectCollidingChildUtil(listener, line, charPositionInLine, identifier, GROUPING_DATA);

        if (curData instanceof YangModule || curData instanceof YangSubModule
                || curData instanceof YangContainer || curData instanceof YangNotification
                || curData instanceof YangList || curData instanceof YangGrouping
                || curData instanceof YangRpc || curData instanceof YangInput
                || curData instanceof YangOutput) {

            YangGrouping groupingNode = getYangGroupingNode(JAVA_GENERATION);
            groupingNode.setName(identifier);
            groupingNode.setGroupingDepth(listener.getGroupingDepth());

            groupingNode.setLineNumber(line);
            groupingNode.setCharPosition(charPositionInLine);
            groupingNode.setFileName(listener.getFileName());
            YangNode curNode = (YangNode) curData;
            try {
                curNode.addChild(groupingNode);
            } catch (DataModelException e) {
                throw new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(ListenerErrorType.UNHANDLED_PARSED_DATA,
                        GROUPING_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY, e.getMessage()));
            }
            listener.getParsedDataStack().push(groupingNode);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER,
                    GROUPING_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY));
        }
    }

    /**
     * It is called when parser exits from grammar rule (grouping), it perform
     * validations and update the data model tree.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processGroupingExit(TreeWalkListener listener,
                                           GroupingStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, GROUPING_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT);

        // Decrease the grouping count by one.
        listener.decreaseGroupingDepth();
        if (listener.getParsedDataStack().peek() instanceof YangGrouping) {
            listener.getParsedDataStack().pop();
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, GROUPING_DATA,
                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }
    }

    /**
     * Validates the cardinality of case sub-statements as per grammar.
     *
     * @param ctx context object of the grammar rule
     */
    private static void validateSubStatementsCardinality(GroupingStatementContext ctx) {

        ListenerValidation.validateCardinalityMaxOne(ctx.statusStatement(), STATUS_DATA, GROUPING_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.descriptionStatement(), DESCRIPTION_DATA, GROUPING_DATA,
                                  ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.referenceStatement(), REFERENCE_DATA, GROUPING_DATA, ctx.identifier().getText());
    }
}
