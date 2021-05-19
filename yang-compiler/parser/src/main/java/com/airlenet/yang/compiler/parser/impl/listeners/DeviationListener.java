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

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 *
 *  deviation-stmt      = deviation-keyword sep
 *                        deviation-arg-str optsep
 *                        "{" stmtsep
 *                            ;; these stmts can appear in any order
 *                            [description-stmt stmtsep]
 *                            [reference-stmt stmtsep]
 *                            (deviate-not-supported-stmt /
 *                              1*(deviate-add-stmt /
 *                                 deviate-replace-stmt /
 *                                 deviate-delete-stmt))
 *                        "}"
 *
 * ANTLR grammar rule
 *   deviationStatement: DEVIATION_KEYWORD deviation LEFT_CURLY_BRACE (
 *       descriptionStatement | referenceStatement | deviateNotSupportedStatement
 *      | deviateAddStatement | deviateReplaceStatement
 *      | deviateDeleteStatement)* RIGHT_CURLY_BRACE;
 */

import com.airlenet.yang.compiler.datamodel.YangAtomicPath;
import com.airlenet.yang.compiler.datamodel.YangDeviation;
import com.airlenet.yang.compiler.datamodel.YangDeviationHolder;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.linker.impl.YangResolutionInfoImpl;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;
import com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser;

import java.util.List;

import static com.airlenet.yang.compiler.datamodel.YangNodeType.DEVIATION_NODE;
import static com.airlenet.yang.compiler.datamodel.utils.DataModelUtils.addResolutionInfo;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DESCRIPTION_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DEVIATE_ADD;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DEVIATE_DELETE;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DEVIATE_NOT_SUPPORTED;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DEVIATE_REPLACE;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DEVIATION_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.REFERENCE_DATA;

/**
 * Represents listener based call back function corresponding to the "deviation"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class DeviationListener {

    /**
     * Creates a new deviation listener.
     */
    private DeviationListener() {
    }

    /**
     * Performs validation and updates the data model tree. It is called when
     * parser receives an input matching the grammar rule(deviation).
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processDeviationEntry(TreeWalkListener listener,
                                             GeneratedYangParser.DeviationStatementContext ctx) {

        String deviationArg = ListenerUtil.removeQuotesAndHandleConcat(ctx.deviation().getText());

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, DEVIATION_DATA,
                             deviationArg, ListenerErrorLocation.ENTRY);

        // Validates deviation argument string
        List<YangAtomicPath> targetNode = ListenerUtil.getValidAbsoluteSchemaNodeId(deviationArg,
                                                                       DEVIATION_DATA, ctx);

        validateSubStatementsCardinality(ctx);

        // Check for identifier collision
        int line = ctx.getStart().getLine();
        int charPositionInLine = ctx.getStart().getCharPositionInLine();
        ListenerCollisionDetector.detectCollidingChildUtil(listener, line, charPositionInLine,
                                 deviationArg, DEVIATION_DATA);

        Parsable curData = listener.getParsedDataStack().peek();
        if (curData instanceof YangDeviationHolder) {
            YangDeviation deviation = new YangDeviation(DEVIATION_NODE, null);
            deviation.setName(deviationArg);
            deviation.setLineNumber(line);
            deviation.setCharPosition(charPositionInLine);
            deviation.setFileName(listener.getFileName());
            deviation.setTargetNode(targetNode);
            if (!ctx.deviateNotSupportedStatement().isEmpty()) {
                deviation.setDeviateNotSupported(true);
            }
            YangNode curNode = (YangNode) curData;
            try {
                curNode.addChild(deviation);
                ((YangDeviationHolder) curNode).setModuleForDeviation(true);
            } catch (DataModelException e) {
                throw new ParserException(
                        ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(ListenerErrorType.UNHANDLED_PARSED_DATA,
                                                              DEVIATION_DATA,
                                                              deviationArg,
                                                              ListenerErrorLocation.ENTRY, e.getMessage()));
            }
            listener.getParsedDataStack().push(deviation);

            // Adds resolution info to the list
            YangResolutionInfoImpl<YangDeviation> info =
                    new YangResolutionInfoImpl<>(deviation, deviation.getParent(),
                                                 line, charPositionInLine);
            addToResolution(info, ctx);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER,
                                                                    DEVIATION_DATA,
                                                                    deviationArg,
                                                                    ListenerErrorLocation.ENTRY));
        }
    }

    /**
     * Performs validation and updates the data model tree. It is called when
     * parser exits from grammar rule (deviation).
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processDeviationExit(TreeWalkListener listener,
                                            GeneratedYangParser.DeviationStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, DEVIATION_DATA, ctx
                .deviation().getText(), ListenerErrorLocation.EXIT);

        if (listener.getParsedDataStack().peek() instanceof YangDeviation) {
            listener.getParsedDataStack().pop();
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, DEVIATION_DATA,
                                                                    ctx.deviation().getText(), ListenerErrorLocation.EXIT));
        }
    }

    /**
     * Validates the cardinality of deviation sub-statements as per grammar.
     *
     * @param ctx context object of the grammar rule
     */
    private static void validateSubStatementsCardinality(GeneratedYangParser
                                                                 .DeviationStatementContext ctx) {
        ListenerValidation.validateCardinalityMaxOne(ctx.descriptionStatement(),
                                  DESCRIPTION_DATA, DEVIATION_DATA,
                                  ctx.deviation().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.referenceStatement(), REFERENCE_DATA,
                                  DEVIATION_DATA, ctx.deviation().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.deviateNotSupportedStatement(),
                                  DEVIATE_NOT_SUPPORTED,
                                  DEVIATION_DATA, ctx.deviation().getText());
        ListenerValidation.validateCardinalityMutuallyExclusive(ctx.deviateNotSupportedStatement(),
                                             DEVIATE_NOT_SUPPORTED,
                                             ctx.deviateAddStatement(),
                                             DEVIATE_ADD,
                                             DEVIATION_DATA,
                                             ctx.deviation().getText(),
                                             ctx);
        ListenerValidation.validateCardinalityMutuallyExclusive(ctx.deviateNotSupportedStatement(),
                                             DEVIATE_NOT_SUPPORTED,
                                             ctx.deviateReplaceStatement(),
                                             DEVIATE_REPLACE,
                                             DEVIATION_DATA,
                                             ctx.deviation().getText(),
                                             ctx);
        ListenerValidation.validateCardinalityMutuallyExclusive(ctx.deviateNotSupportedStatement(),
                                             DEVIATE_NOT_SUPPORTED,
                                             ctx.deviateDeleteStatement(),
                                             DEVIATE_DELETE,
                                             DEVIATION_DATA,
                                             ctx.deviation().getText(),
                                             ctx);
    }

    /**
     * Add to resolution list.
     *
     * @param info resolution info
     * @param ctx  context object
     */
    private static void addToResolution(YangResolutionInfoImpl<YangDeviation> info,
                                        GeneratedYangParser.DeviationStatementContext ctx) {
        try {
            addResolutionInfo(info);
        } catch (DataModelException e) {
            throw new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(
                    ListenerErrorType.UNHANDLED_PARSED_DATA, DEVIATION_DATA,
                    ctx.deviation().getText(), ListenerErrorLocation.EXIT, e.getMessage()));
        }
    }
}
