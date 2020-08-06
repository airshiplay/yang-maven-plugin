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

import com.airlenet.yang.compiler.datamodel.YangBase;
import com.airlenet.yang.compiler.datamodel.YangIdentity;
import com.airlenet.yang.compiler.datamodel.YangIdentityRef;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.YangNodeIdentifier;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.linker.impl.YangResolutionInfoImpl;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import static com.airlenet.yang.compiler.datamodel.utils.DataModelUtils.addResolutionInfo;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.BASE_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.BaseStatementContext;

/**
 * base-stmt           = base-keyword sep identifier-ref-arg-str
 * optsep stmtend*
 * identifier-ref-arg  = [prefix ":"] identifier
 */

/**
 * Represents listener based call back function corresponding to the "base"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class BaseListener {

    //Creates a new base listener.
    private BaseListener() {
    }

    /**
     * Performs validation and updates the data model tree when parser receives an
     * input matching the grammar rule (base).
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processBaseEntry(TreeWalkListener listener,
                                        BaseStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, BASE_DATA, ctx.string().getText(), ListenerErrorLocation.ENTRY);

        YangNodeIdentifier nodeIdentifier = ListenerUtil.getValidNodeIdentifier(ctx.string().getText(), BASE_DATA, ctx);

        Parsable tmpData = listener.getParsedDataStack().peek();

        /**
         * For identityref base node identifier is copied in identity listener itself, so no need to process
         * base statement for indentityref
         */
        if (tmpData instanceof YangIdentityRef) {
            return;
        }

        if (!(tmpData instanceof YangIdentity)) {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, BASE_DATA,
                                                                    ctx.string().getText(), ListenerErrorLocation.ENTRY));
        }

        YangBase yangBase = new YangBase();
        yangBase.setBaseIdentifier(nodeIdentifier);
        ((YangIdentity) tmpData).setBaseNode(yangBase);
        yangBase.setParentIdentity(((YangIdentity) tmpData));

        int errorLine = ctx.getStart().getLine();
        int errorPosition = ctx.getStart().getCharPositionInLine();

        yangBase.setLineNumber(errorLine);
        yangBase.setCharPosition(errorPosition);
        yangBase.setFileName(listener.getFileName());

        // Add resolution information to the list
        YangResolutionInfoImpl resolutionInfo =
                new YangResolutionInfoImpl<YangBase>(yangBase, (YangNode) tmpData, errorLine, errorPosition);
        addToResolutionList(resolutionInfo, ctx);
    }

    /**
     * Add to resolution list.
     *
     * @param resolutionInfo resolution information
     * @param ctx            context object of the grammar rule
     */
    private static void addToResolutionList(YangResolutionInfoImpl<YangBase> resolutionInfo,
                                            BaseStatementContext ctx) {

        try {
            addResolutionInfo(resolutionInfo);
        } catch (DataModelException e) {
            throw new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(ListenerErrorType.UNHANDLED_PARSED_DATA,
                                                                            BASE_DATA, ctx.string().getText(),
                                                                            ListenerErrorLocation.EXIT, e.getMessage()));
        }
    }
}
