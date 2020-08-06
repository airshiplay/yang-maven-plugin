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

import com.airlenet.yang.compiler.datamodel.YangBelongsTo;
import com.airlenet.yang.compiler.datamodel.YangSubModule;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.BELONGS_TO_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.BelongstoStatementContext;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 * submodule-header-stmts =
 *                            ;; these stmts can appear in any order
 *                            [yang-version-stmt stmtsep]
 *                             belongs-to-stmt stmtsep
 *
 * belongs-to-stmt     = belongs-to-keyword sep identifier-arg-str
 *                       optsep
 *                       "{" stmtsep
 *                           prefix-stmt stmtsep
 *                       "}"
 *
 * ANTLR grammar rule
 * submodule_header_statement : yang_version_stmt? belongs_to_stmt
 *                            | belongs_to_stmt yang_version_stmt?
 *                            ;
 * belongs_to_stmt : BELONGS_TO_KEYWORD identifier LEFT_CURLY_BRACE belongs_to_stmt_body RIGHT_CURLY_BRACE;
 * belongs_to_stmt_body : prefix_stmt;
 */

/**
 * Represents listener based call back function corresponding to the
 * "belongs to" rule defined in ANTLR grammar file for corresponding ABNF rule
 * in RFC 6020.
 */
public final class BelongsToListener {

    /**
     * Creates a new belongto listener.
     */
    private BelongsToListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar rule
     * (belongsto), perform validations and update the data model tree.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processBelongsToEntry(TreeWalkListener listener,
                                             BelongstoStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, BELONGS_TO_DATA, ctx.identifier().getText(),
                ListenerErrorLocation.ENTRY);

        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), BELONGS_TO_DATA, ctx);

        YangBelongsTo belongstoNode = new YangBelongsTo();
        belongstoNode.setBelongsToModuleName(identifier);

        // Set the line number and character position in line for the belongs to.
        int errorLine = ctx.getStart().getLine();
        int errorPosition = ctx.getStart().getCharPositionInLine();
        belongstoNode.setLineNumber(errorLine);
        belongstoNode.setCharPosition(errorPosition);
        belongstoNode.setFileName(listener.getFileName());

        // Push belongsto into the stack.
        listener.getParsedDataStack().push(belongstoNode);
    }

    /**
     * It is called when parser exits from grammar rule (belongsto), it perform
     * validations and update the data model tree.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processBelongsToExit(TreeWalkListener listener,
                                            BelongstoStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, BELONGS_TO_DATA, ctx.identifier().getText(),
                ListenerErrorLocation.EXIT);

        Parsable tmpBelongstoNode = listener.getParsedDataStack().peek();
        if (tmpBelongstoNode instanceof YangBelongsTo) {
            listener.getParsedDataStack().pop();

            // Check for stack to be empty.
            ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, BELONGS_TO_DATA,
                    ctx.identifier().getText(), ListenerErrorLocation.EXIT);

            Parsable tmpNode = listener.getParsedDataStack().peek();
            switch (tmpNode.getYangConstructType()) {
                case SUB_MODULE_DATA: {
                    YangSubModule subModule = (YangSubModule) tmpNode;
                    subModule.setBelongsTo((YangBelongsTo) tmpBelongstoNode);
                    subModule.setPrefix(subModule.getBelongsTo().getPrefix());
                    break;
                }
                default:
                    throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, BELONGS_TO_DATA,
                            ctx.identifier().getText(),
                            ListenerErrorLocation.EXIT));
            }
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, BELONGS_TO_DATA,
                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }
    }
}
