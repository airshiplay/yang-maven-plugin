/*
 * Copyright 2017-present Open Networking Foundation
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

import com.airlenet.yang.compiler.datamodel.YangAnydata;
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
import com.airlenet.yang.compiler.datamodel.YangVersionHolder;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;
import com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.AnydataStatementContext;

import static com.airlenet.yang.compiler.datamodel.utils.GeneratedLanguage.JAVA_GENERATION;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.ANYDATA_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.CONFIG_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DESCRIPTION_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.MANDATORY_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.REFERENCE_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.STATUS_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.WHEN_DATA;
import static com.airlenet.yang.compiler.translator.tojava.YangDataModelFactory.getYangAnydataNode;
import static com.airlenet.yang.compiler.utils.UtilConstants.ONE_DOT_ONE;

/*
 * Reference: RFC7950 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC7950
 * anydata-stmt        = anydata-keyword sep identifier-arg-str optsep
 *                       (";" /
 *                        "{" stmtsep
 *                            ;; these stmts can appear in any order
 *                            [when-stmt]
 *                            *if-feature-stmt
 *                            *must-stmt
 *                            [config-stmt]
 *                            [mandatory-stmt]
 *                            [status-stmt]
 *                            [description-stmt]
 *                            [reference-stmt]
 *                         "}") stmtsep
 *
 * ANTLR grammar rule
 *  anydataStatement : ANYDATA_KEYWORD identifier (STMTEND |
 *             LEFT_CURLY_BRACE stmtSep (whenStatement | ifFeatureStatement
 *             | mustStatement | configStatement | mandatoryStatement
 *             | statusStatement | descriptionStatement | referenceStatement)*
 *             RIGHT_CURLY_BRACE) stmtSep;
 */

/**
 * Represents listener based call back function corresponding to the "anydata"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 7950.
 */
public final class AnydataListener {

    /**
     * Creates a new anydata listener.
     */
    private AnydataListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar rule
     * (anydata), performs validation and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processAnydataEntry(TreeWalkListener listener,
                                           AnydataStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, ANYDATA_DATA, ctx.identifier()
                .getText(), ListenerErrorLocation.ENTRY);

        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), ANYDATA_DATA, ctx);

        // Validate sub statement cardinality.
        validateSubStatementsCardinality(ctx);

        // Check for identifier collision
        int line = ctx.getStart().getLine();
        int charPositionInLine = ctx.getStart().getCharPositionInLine();
        ListenerCollisionDetector.detectCollidingChildUtil(listener, line, charPositionInLine, identifier, ANYDATA_DATA);

        YangAnydata anyData = getYangAnydataNode(JAVA_GENERATION);
        anyData.setName(identifier);

        anyData.setLineNumber(line);
        anyData.setCharPosition(charPositionInLine);
        anyData.setFileName(listener.getFileName());
        /*
         * If "config" is not specified, the default is the same as the parent
         * schema node's "config" value.
         */
        if (ctx.configStatement().isEmpty()) {
            boolean parentConfig = ListenerValidation.getParentNodeConfig(listener);
            anyData.setConfig(parentConfig);
        }

        Parsable curData = listener.getParsedDataStack().peek();
        // Module/Submodule yang version check to make sure anydata supported
        // version
        if (!((YangVersionHolder) listener.getParsedDataStack().get(0))
                .getVersion().equals(ONE_DOT_ONE)) {
            throw new ParserException("YANG file error : anydata with name " + ctx.identifier()
                    .getText() + " at line number " + line + " in " + listener
                    .getFileName() + " is feature of YANG version 1.1");
        }
        if (curData instanceof YangModule || curData instanceof YangSubModule
                || curData instanceof YangContainer || curData instanceof YangList
                || curData instanceof YangCase || curData instanceof YangNotification
                || curData instanceof YangInput || curData instanceof YangOutput
                || curData instanceof YangAugment || curData instanceof YangGrouping) {
            YangNode curNode = (YangNode) curData;
            try {
                curNode.addChild(anyData);
            } catch (DataModelException e) {
                throw new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(
                        ListenerErrorType.UNHANDLED_PARSED_DATA, ANYDATA_DATA,
                        ctx.identifier().getText(), ListenerErrorLocation.ENTRY, e.getMessage()));
            }
            listener.getParsedDataStack().push(anyData);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, ANYDATA_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.ENTRY));
        }
    }

    /**
     * It is called when parser exits from grammar rule (anydata), it perform
     * validations and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processAnydataExit(TreeWalkListener listener,
                                          AnydataStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, ANYDATA_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT);

        if (listener.getParsedDataStack().peek() instanceof YangAnydata) {
            listener.getParsedDataStack().pop();
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, ANYDATA_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }
    }

    /**
     * Validates the cardinality of anydata sub-statements as per grammar.
     *
     * @param ctx context object of the grammar rule
     */
    private static void validateSubStatementsCardinality(AnydataStatementContext ctx) {

        String text = ctx.identifier().getText();
        ListenerValidation.validateCardinalityMaxOne(ctx.whenStatement(), WHEN_DATA, ANYDATA_DATA, text);
        ListenerValidation.validateCardinalityMaxOne(ctx.configStatement(), CONFIG_DATA, ANYDATA_DATA, text);
        ListenerValidation.validateCardinalityMaxOne(ctx.mandatoryStatement(), MANDATORY_DATA, ANYDATA_DATA, text);
        ListenerValidation.validateCardinalityMaxOne(ctx.statusStatement(), STATUS_DATA, ANYDATA_DATA, text);
        ListenerValidation.validateCardinalityMaxOne(ctx.descriptionStatement(), DESCRIPTION_DATA, ANYDATA_DATA, text);
        ListenerValidation.validateCardinalityMaxOne(ctx.referenceStatement(), REFERENCE_DATA, ANYDATA_DATA, text);
    }
}
