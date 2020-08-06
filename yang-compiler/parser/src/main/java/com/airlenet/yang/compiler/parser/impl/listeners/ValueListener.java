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
 * value-stmt = value-keyword sep integer-value stmtend
 *
 * ANTLR grammar rule
 * valueStatement : VALUE_KEYWORD ((MINUS INTEGER) | INTEGER) STMTEND;
 */

import com.airlenet.yang.compiler.datamodel.YangEnum;
import com.airlenet.yang.compiler.datamodel.YangEnumeration;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.VALUE_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.ValueStatementContext;

/**
 * Represents listener based call back function corresponding to the "value"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class ValueListener {

    /**
     * Creates a new value listener.
     */
    private ValueListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar rule
     * (value), perform validations and update the data model tree.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processValueEntry(TreeWalkListener listener, ValueStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, VALUE_DATA, ctx.value().getText(), ListenerErrorLocation.ENTRY);

        // Validate value
        int value = ListenerUtil.getValidIntegerValue(ctx.value().getText(), VALUE_DATA, ctx);

        // Obtain the node of the stack.
        Parsable tmpNode = listener.getParsedDataStack().peek();
        switch (tmpNode.getYangConstructType()) {
            case ENUM_DATA: {
                YangEnum enumNode = (YangEnum) tmpNode;
                if (!isEnumValueValid(listener, ctx, value)) {
                    ParserException parserException = new ParserException("Duplicate Value Entry");
                    parserException.setLine(ctx.getStart().getLine());
                    parserException.setCharPosition(ctx.getStart().getCharPositionInLine());
                    throw parserException;
                }
                enumNode.setValue(value);
                break;
            }
            default:
                throw new ParserException(
                        ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, VALUE_DATA, ctx.value().getText(), ListenerErrorLocation.ENTRY));
        }
    }

    /**
     * Validates ENUM value uniqueness.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     * @param value    enum value
     * @return validation result
     */
    private static boolean isEnumValueValid(TreeWalkListener listener, ValueStatementContext ctx,
                                            int value) {
        Parsable enumNode = listener.getParsedDataStack().pop();

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, VALUE_DATA, ctx.value().getText(), ListenerErrorLocation.ENTRY);

        Parsable tmpNode = listener.getParsedDataStack().peek();
        switch (tmpNode.getYangConstructType()) {
            case ENUMERATION_DATA: {
                YangEnumeration yangEnumeration = (YangEnumeration) tmpNode;
                for (YangEnum curEnum : yangEnumeration.getEnumSet()) {
                    if (value == curEnum.getValue()) {
                        listener.getParsedDataStack().push(enumNode);
                        return false;
                    }
                }
                listener.getParsedDataStack().push(enumNode);
                return true;
            }
            default:
                listener.getParsedDataStack().push(enumNode);
                throw new ParserException(
                        ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, VALUE_DATA, ctx.value().getText(), ListenerErrorLocation.ENTRY));
        }
    }
}
