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

import com.airlenet.yang.compiler.datamodel.YangAugment;
import com.airlenet.yang.compiler.datamodel.YangCase;
import com.airlenet.yang.compiler.datamodel.YangContainer;
import com.airlenet.yang.compiler.datamodel.YangGrouping;
import com.airlenet.yang.compiler.datamodel.YangInput;
import com.airlenet.yang.compiler.datamodel.YangList;
import com.airlenet.yang.compiler.datamodel.YangModule;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.YangNotification;
import com.airlenet.yang.compiler.datamodel.YangOutput;
import com.airlenet.yang.compiler.datamodel.YangSubModule;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import static com.airlenet.yang.compiler.datamodel.utils.GeneratedLanguage.JAVA_GENERATION;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.CONFIG_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.CONTAINER_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DESCRIPTION_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.PRESENCE_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.REFERENCE_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.STATUS_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.ContainerStatementContext;
import static com.airlenet.yang.compiler.translator.tojava.YangDataModelFactory.getYangContainerNode;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 *  container-stmt      = container-keyword sep identifier-arg-str optsep
 *                        (";" /
 *                         "{" stmtsep
 *                             ;; these stmts can appear in any order
 *                             [when-stmt stmtsep]
 *                             *(if-feature-stmt stmtsep)
 *                             *(must-stmt stmtsep)
 *                             [presence-stmt stmtsep]
 *                             [config-stmt stmtsep]
 *                             [status-stmt stmtsep]
 *                             [description-stmt stmtsep]
 *                             [reference-stmt stmtsep]
 *                             *((typedef-stmt /
 *                                grouping-stmt) stmtsep)
 *                             *(data-def-stmt stmtsep)
 *                         "}")
 *
 * ANTLR grammar rule
 *  containerStatement : CONTAINER_KEYWORD identifier
 *                   (STMTEND | LEFT_CURLY_BRACE (whenStatement | ifFeatureStatement | mustStatement |
 *                   presenceStatement | configStatement | statusStatement | descriptionStatement |
 *                   referenceStatement | typedefStatement | groupingStatement
 *                    | dataDefStatement)* RIGHT_CURLY_BRACE);
 */

/**
 * Represents listener based call back function corresponding to the "container"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class ContainerListener {

    /**
     * Creates a new container listener.
     */
    private ContainerListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar rule
     * (container), performs validation and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processContainerEntry(TreeWalkListener listener,
                                             ContainerStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, CONTAINER_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY);

        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), CONTAINER_DATA, ctx);

        // Validate sub statement cardinality.
        validateSubStatementsCardinality(ctx);

        // Check for identifier collision
        int line = ctx.getStart().getLine();
        int charPositionInLine = ctx.getStart().getCharPositionInLine();
        ListenerCollisionDetector.detectCollidingChildUtil(listener, line, charPositionInLine, identifier, CONTAINER_DATA);

        YangContainer container = getYangContainerNode(JAVA_GENERATION);
        container.setName(identifier);

        container.setLineNumber(line);
        container.setCharPosition(charPositionInLine);
        container.setFileName(listener.getFileName());
        /*
         * If "config" is not specified, the default is the same as the parent
         * schema node's "config" value.
         */
        if (ctx.configStatement().isEmpty()) {
            boolean parentConfig = ListenerValidation.getParentNodeConfig(listener);
            container.setConfig(parentConfig);
        }

        Parsable curData = listener.getParsedDataStack().peek();
        if (curData instanceof YangModule || curData instanceof YangSubModule
                || curData instanceof YangContainer || curData instanceof YangList
                || curData instanceof YangCase || curData instanceof YangNotification
                || curData instanceof YangInput || curData instanceof YangOutput
                || curData instanceof YangAugment || curData instanceof YangGrouping) {
            YangNode curNode = (YangNode) curData;
            try {
                curNode.addChild(container);
            } catch (DataModelException e) {
                throw new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(
                        ListenerErrorType.UNHANDLED_PARSED_DATA, CONTAINER_DATA,
                        ctx.identifier().getText(), ListenerErrorLocation.ENTRY, e.getMessage()));
            }
            listener.getParsedDataStack().push(container);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, CONTAINER_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.ENTRY));
        }
    }

    /**
     * It is called when parser exits from grammar rule (container), it perform
     * validations and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processContainerExit(TreeWalkListener listener,
                                            ContainerStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, CONTAINER_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT);

        if (listener.getParsedDataStack().peek() instanceof YangContainer) {
            YangContainer yangContainer = (YangContainer) listener.getParsedDataStack().peek();
            try {
                yangContainer.validateDataOnExit();
            } catch (DataModelException e) {
                ParserException parserException = new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(
                        ListenerErrorType.UNHANDLED_PARSED_DATA, CONTAINER_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT, e.getMessage()));
                parserException.setLine(ctx.getStart().getLine());
                parserException.setCharPosition(ctx.getStart().getCharPositionInLine());
                throw parserException;
            }
            listener.getParsedDataStack().pop();
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, CONTAINER_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }
    }

    /**
     * Validates the cardinality of container sub-statements as per grammar.
     *
     * @param ctx context object of the grammar rule
     */
    private static void validateSubStatementsCardinality(ContainerStatementContext ctx) {

        ListenerValidation.validateCardinalityMaxOne(ctx.presenceStatement(), PRESENCE_DATA, CONTAINER_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.configStatement(), CONFIG_DATA, CONTAINER_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.descriptionStatement(), DESCRIPTION_DATA, CONTAINER_DATA,
                                  ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.referenceStatement(), REFERENCE_DATA, CONTAINER_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.statusStatement(), STATUS_DATA, CONTAINER_DATA, ctx.identifier().getText());
        // TODO validate 'when' cardinality
    }
}
