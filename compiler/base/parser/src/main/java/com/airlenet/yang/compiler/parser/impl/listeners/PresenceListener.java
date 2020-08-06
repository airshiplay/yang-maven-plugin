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
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerErrorLocation;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerErrorMessageConstruction;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerErrorType;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerValidation;

import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.CONTAINER_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.PRESENCE_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.PresenceStatementContext;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 * presence-stmt       = presence-keyword sep string stmtend
 *
 * ANTLR grammar rule
 * presenceStatement : PRESENCE_KEYWORD string STMTEND;
 */

/**
 * Represents listener based call back function corresponding to the "presence"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class PresenceListener {

    /**
     * Creates a new presence listener.
     */
    private PresenceListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar
     * rule (presence), performs validation and updates the data model
     * tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processPresenceEntry(TreeWalkListener listener,
                                            PresenceStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, PRESENCE_DATA, ctx.string().getText(), ListenerErrorLocation.ENTRY);

        Parsable tmpData = listener.getParsedDataStack().peek();
        if (tmpData.getYangConstructType() == CONTAINER_DATA) {
            YangContainer container = (YangContainer) tmpData;
            container.setPresence(ctx.string().getText());
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, PRESENCE_DATA,
                    ctx.string().getText(), ListenerErrorLocation.ENTRY));
        }
    }
}