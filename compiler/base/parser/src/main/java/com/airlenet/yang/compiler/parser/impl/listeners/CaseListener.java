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
import com.airlenet.yang.compiler.datamodel.YangChoice;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;
import com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser;

import static com.airlenet.yang.compiler.datamodel.utils.GeneratedLanguage.JAVA_GENERATION;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.CASE_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DESCRIPTION_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.REFERENCE_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.STATUS_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.WHEN_DATA;
import static com.airlenet.yang.compiler.translator.tojava.YangDataModelFactory.getYangCaseNode;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 *  case-stmt           = case-keyword sep identifier-arg-str optsep
 *                        (";" /
 *                         "{" stmtsep
 *                             ;; these stmts can appear in any order
 *                             [when-stmt stmtsep]
 *                             *(if-feature-stmt stmtsep)
 *                             [status-stmt stmtsep]
 *                             [description-stmt stmtsep]
 *                             [reference-stmt stmtsep]
 *                             *(data-def-stmt stmtsep)
 *                         "}")
 *
 * ANTLR grammar rule
 * caseStatement : CASE_KEYWORD identifier (STMTEND | LEFT_CURLY_BRACE (whenStatement | ifFeatureStatement
 *               | statusStatement | descriptionStatement | referenceStatement | dataDefStatement)* RIGHT_CURLY_BRACE);
 */

/**
 * Represents listener based call back function corresponding to the "case" rule
 * defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class CaseListener {

    /**
     * Create a new case listener.
     */
    private CaseListener() {
    }

    /**
     * It is called when parser enters grammar rule (case), it perform
     * validations and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processCaseEntry(TreeWalkListener listener,
                                        GeneratedYangParser.CaseStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, CASE_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY);

        // Check validity of identifier and remove double quotes.
        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), CASE_DATA, ctx);

        // Validate sub statement cardinality.
        validateSubStatementsCardinality(ctx);

        Parsable curData = listener.getParsedDataStack().peek();

        // Check for identifier collision
        int line = ctx.getStart().getLine();
        int charPositionInLine = ctx.getStart().getCharPositionInLine();
        ListenerCollisionDetector.detectCollidingChildUtil(listener, line, charPositionInLine, identifier, CASE_DATA);

        if (curData instanceof YangChoice || curData instanceof YangAugment) {
            YangCase caseNode = getYangCaseNode(JAVA_GENERATION);
            caseNode.setName(identifier);
            caseNode.setLineNumber(line);
            caseNode.setCharPosition(charPositionInLine);
            caseNode.setFileName(listener.getFileName());
            YangNode curNode = (YangNode) curData;
            try {
                curNode.addChild(caseNode);
            } catch (DataModelException e) {
                throw new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(ListenerErrorType.UNHANDLED_PARSED_DATA,
                        CASE_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY, e.getMessage()));
            }
            listener.getParsedDataStack().push(caseNode);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, CASE_DATA,
                    ctx.identifier().getText(), ListenerErrorLocation.ENTRY));
        }
    }

    /**
     * It is called when parser exits from grammar rule (case), it perform
     * validations and update the data model tree.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processCaseExit(TreeWalkListener listener,
                                       GeneratedYangParser.CaseStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, CASE_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT);

        if (listener.getParsedDataStack().peek() instanceof YangCase) {
            listener.getParsedDataStack().pop();
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, CASE_DATA,
                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }
    }

    /**
     * Validates the cardinality of case sub-statements as per grammar.
     *
     * @param ctx context object of the grammar rule
     */
    private static void validateSubStatementsCardinality(GeneratedYangParser.CaseStatementContext ctx) {

        ListenerValidation.validateCardinalityMaxOne(ctx.whenStatement(), WHEN_DATA, CASE_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.statusStatement(), STATUS_DATA, CASE_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.descriptionStatement(), DESCRIPTION_DATA, CASE_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.referenceStatement(), REFERENCE_DATA, CASE_DATA, ctx.identifier().getText());
    }
}
