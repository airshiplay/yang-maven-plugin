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

import com.airlenet.yang.compiler.datamodel.YangAppExtended;
import com.airlenet.yang.compiler.datamodel.YangCompilerAnnotation;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;
import com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser;

import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.APP_EXTENDED_NAME_DATA;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 *   app-extended-stmt = prefix:app-extended-name-keyword string ";"
 *
 * ANTLR grammar rule
 * appExtendedStatement : APP_EXTENDED extendedName STMTEND;
 */

/**
 * Represents listener based call back function corresponding to the "app-extended-name"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class AppExtendedNameListener {

    /**
     * Creates a new app-extended-name listener.
     */
    private AppExtendedNameListener() {
    }

    /**
     * Performs validation and updates the data model tree. It is called when parser receives an
     * input matching the grammar rule(app-extended-name).
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processAppExtendedNameEntry(TreeWalkListener listener,
                                                   GeneratedYangParser.AppExtendedStatementContext ctx) {

        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, APP_EXTENDED_NAME_DATA, ctx.extendedName().getText(), ListenerErrorLocation.ENTRY);

        String prefix = ListenerUtil.getValidPrefix(ctx.APP_EXTENDED().getText(), APP_EXTENDED_NAME_DATA, ctx);
        YangAppExtended extendedName = new YangAppExtended();
        extendedName.setPrefix(prefix);
        extendedName.setYangAppExtendedName(ListenerUtil.removeQuotesAndHandleConcat(ctx.extendedName().getText()));

        extendedName.setLineNumber(ctx.getStart().getLine());
        extendedName.setCharPosition(ctx.getStart().getCharPositionInLine());
        extendedName.setFileName(listener.getFileName());
        Parsable curData = listener.getParsedDataStack().peek();
        if (curData instanceof YangCompilerAnnotation) {
            YangCompilerAnnotation compilerAnnotation = ((YangCompilerAnnotation) curData);
            compilerAnnotation.setYangAppExtendedName(extendedName);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, APP_EXTENDED_NAME_DATA,
                    ctx.extendedName().getText(), ListenerErrorLocation.ENTRY));
        }
    }
}
