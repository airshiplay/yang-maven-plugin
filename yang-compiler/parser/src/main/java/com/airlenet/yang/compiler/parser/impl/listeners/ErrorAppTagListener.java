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

import com.airlenet.yang.compiler.datamodel.YangAppErrorHolder;
import com.airlenet.yang.compiler.datamodel.YangAppErrorInfo;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.ERROR_APP_TAG_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.ErrorAppTagStatementContext;

/*
 * Reference: RFC 6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC 6020
 *
 *  error-app-tag-stmt  = error-app-tag-keyword sep string stmtend
 *
 * ANTLR grammar rule
 * errorAppTagStatement : ERROR_APP_TAG_KEYWORD string STMTEND;
 */

/**
 * Represents listener based call back function corresponding to the
 * error app tag defined in ANTLR grammar file for corresponding ABNF rule
 * in RFC 6020.
 */
public final class ErrorAppTagListener {

    /**
     * Creates a new error app tag listener.
     */
    private ErrorAppTagListener() {
    }

    /**
     * Performs validations and updates the data model tree. It is called when parser
     * receives an input matching the grammar rule error app tag.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processErrorAppTagMessageEntry(TreeWalkListener listener,
                                                      ErrorAppTagStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, ERROR_APP_TAG_DATA, ctx.string().getText(), ListenerErrorLocation.ENTRY);
        String errorMessage = ListenerUtil.removeQuotesAndHandleConcat(ctx.string().getText());
        // Obtain the node of the stack.
        Parsable tmpNode = listener.getParsedDataStack().peek();
        if (tmpNode instanceof YangAppErrorHolder) {
            YangAppErrorInfo yangAppErrorInfo = ((YangAppErrorHolder) tmpNode).getAppErrorInfo();
            yangAppErrorInfo.setErrorAppTag(errorMessage);
            yangAppErrorInfo.setLineNumber(ctx.getStart().getLine());
            yangAppErrorInfo.setCharPosition(ctx.getStart().getCharPositionInLine());
            yangAppErrorInfo.setFileName(listener.getFileName());
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, ERROR_APP_TAG_DATA,
                                                                    ctx.string().getText(), ListenerErrorLocation.ENTRY));
        }
    }
}