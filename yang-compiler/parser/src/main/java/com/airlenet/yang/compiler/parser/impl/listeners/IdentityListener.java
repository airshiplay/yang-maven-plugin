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

import com.airlenet.yang.compiler.datamodel.YangIdentity;
import com.airlenet.yang.compiler.datamodel.YangModule;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.YangSubModule;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import static com.airlenet.yang.compiler.datamodel.utils.GeneratedLanguage.JAVA_GENERATION;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.IDENTITY_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.IdentityStatementContext;
import static com.airlenet.yang.compiler.translator.tojava.YangDataModelFactory.getYangIdentityNode;

/**
 * Reference: RFC6020 and YANG ANTLR Grammar.
 * <p>
 * ABNF grammar as per RFC6020
 * identity-stmt       = identity-keyword sep identifier-arg-str optsep
 * (";" /
 * "{" stmtsep
 * ;; these stmts can appear in any order
 * [base-stmt stmtsep]
 * [status-stmt stmtsep]
 * [description-stmt stmtsep]
 * [reference-stmt stmtsep]
 * "}")
 */

/**
 * Represents listener based call back function corresponding to the "identity"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class IdentityListener {

    //Creates a identity listener.
    private IdentityListener() {
    }

    /**
     * Performs validations and update the data model tree when parser receives an input
     * matching the grammar rule (identity).
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processIdentityEntry(TreeWalkListener listener,
                                            IdentityStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, IDENTITY_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY);

        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), IDENTITY_DATA, ctx);

        YangIdentity identity = getYangIdentityNode(JAVA_GENERATION);
        identity.setName(identifier);

        identity.setLineNumber(ctx.getStart().getLine());
        identity.setCharPosition(ctx.getStart().getCharPositionInLine());
        identity.setFileName(listener.getFileName());
        Parsable curData = listener.getParsedDataStack().peek();
        if (curData instanceof YangModule || curData instanceof YangSubModule) {
            YangNode curNode = (YangNode) curData;
            try {
                curNode.addChild(identity);
            } catch (DataModelException e) {
                throw new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(ListenerErrorType.UNHANDLED_PARSED_DATA, IDENTITY_DATA,
                                                                                ctx.identifier().getText(), ListenerErrorLocation.ENTRY,
                                                                                e.getMessage()));
            }
            // Push identity node to the stack.
            listener.getParsedDataStack().push(identity);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, IDENTITY_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.ENTRY));
        }
    }

    /**
     * Performs validations and update the data model tree when parser exits from grammar
     * rule (identity).
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processIdentityExit(TreeWalkListener listener,
                                           IdentityStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_CURRENT_HOLDER, IDENTITY_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT);

        Parsable parsableType = listener.getParsedDataStack().pop();
        if (!(parsableType instanceof YangIdentity)) {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, IDENTITY_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }
    }
}
