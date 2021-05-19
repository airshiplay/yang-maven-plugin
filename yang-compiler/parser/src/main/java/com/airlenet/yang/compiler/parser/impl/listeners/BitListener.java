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
 * bit-stmt            = bit-keyword sep identifier-arg-str optsep
 *                       (";" /
 *                        "{" stmtsep
 *                            ;; these stmts can appear in any order
 *                            [position-stmt stmtsep]
 *                            [status-stmt stmtsep]
 *                            [description-stmt stmtsep]
 *                            [reference-stmt stmtsep]
 *                          "}"
 *                        "}")
 *
 * ANTLR grammar rule
 * bitStatement : BIT_KEYWORD identifier (STMTEND | LEFT_CURLY_BRACE bitBodyStatement RIGHT_CURLY_BRACE);
 *
 * bitBodyStatement : positionStatement? statusStatement? descriptionStatement? referenceStatement?
 *               | positionStatement? statusStatement? referenceStatement? descriptionStatement?
 *               | positionStatement? descriptionStatement? statusStatement? referenceStatement?
 *               | positionStatement? descriptionStatement? referenceStatement? statusStatement?
 *               | positionStatement? referenceStatement? statusStatement? descriptionStatement?
 *               | positionStatement? referenceStatement? descriptionStatement? statusStatement?
 *               | statusStatement? positionStatement? descriptionStatement? referenceStatement?
 *               | statusStatement? positionStatement? referenceStatement? descriptionStatement?
 *               | statusStatement? descriptionStatement? descriptionStatement? positionStatement?
 *               | statusStatement? descriptionStatement? positionStatement? descriptionStatement?
 *               | statusStatement? referenceStatement? positionStatement? descriptionStatement?
 *               | statusStatement? referenceStatement? descriptionStatement? positionStatement?
 *               | descriptionStatement? positionStatement? statusStatement? referenceStatement?
 *               | descriptionStatement? positionStatement? referenceStatement? statusStatement?
 *               | descriptionStatement? statusStatement? positionStatement? referenceStatement?
 *               | descriptionStatement? statusStatement? referenceStatement? positionStatement?
 *               | descriptionStatement? referenceStatement? positionStatement? statusStatement?
 *               | descriptionStatement? referenceStatement? statusStatement? positionStatement?
 *               | referenceStatement? positionStatement? descriptionStatement? statusStatement?
 *               | referenceStatement? positionStatement? statusStatement? descriptionStatement?
 *               | referenceStatement? statusStatement? descriptionStatement? positionStatement?
 *               | referenceStatement? statusStatement? positionStatement? descriptionStatement?
 *               | referenceStatement? descriptionStatement? positionStatement? statusStatement?
 *               | referenceStatement? descriptionStatement? statusStatement? positionStatement?
 *               ;
 */

import com.airlenet.yang.compiler.datamodel.YangBit;
import com.airlenet.yang.compiler.datamodel.YangBits;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;
import com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser;

import java.util.Map;

import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.BIT_DATA;

/**
 * Represents listener based call back function corresponding to the "bit"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class BitListener {

    /**
     * Creates a new bit listener.
     */
    private BitListener() {
    }

    /**
     * It is called when parser enters grammar rule (bit), it perform
     * validations and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processBitEntry(TreeWalkListener listener,
                                       GeneratedYangParser.BitStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, BIT_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY);

        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), BIT_DATA, ctx);

        YangBit bitNode = new YangBit();
        bitNode.setBitName(identifier);

        bitNode.setLineNumber(ctx.getStart().getLine());
        bitNode.setCharPosition(ctx.getStart().getCharPositionInLine());
        bitNode.setFileName(listener.getFileName());
        listener.getParsedDataStack().push(bitNode);
    }

    /**
     * It is called when parser exits from grammar rule (bit), it perform
     * validations and update the data model tree.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processBitExit(TreeWalkListener listener,
                                      GeneratedYangParser.BitStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, BIT_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT);

        Parsable tmpBitNode = listener.getParsedDataStack().peek();
        if (tmpBitNode instanceof YangBit) {
            listener.getParsedDataStack().pop();

            // Check for stack to be non empty.
            ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, BIT_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT);

            Parsable tmpNode = listener.getParsedDataStack().peek();
            switch (tmpNode.getYangConstructType()) {
                case BITS_DATA: {
                    YangBits yangBits = (YangBits) tmpNode;
                    if (ctx.bitBodyStatement() == null || ctx.bitBodyStatement().positionStatement() == null) {
                        int maxPosition = 0;
                        boolean isPositionPresent = false;

                        for (Map.Entry<Integer, YangBit> element : yangBits.getBitPositionMap().entrySet()) {
                            if (maxPosition <= element.getKey()) {
                                maxPosition = element.getKey();
                                isPositionPresent = true;
                            }
                        }

                        if (isPositionPresent) {
                            maxPosition++;
                        }
                        ((YangBit) tmpBitNode).setPosition(maxPosition);
                    }
                    try {
                        yangBits.addBitInfo((YangBit) tmpBitNode);
                    } catch (DataModelException e) {
                        ParserException parserException = new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(
                                ListenerErrorType.INVALID_CONTENT, BIT_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT, e.getMessage()));
                        parserException.setLine(ctx.getStart().getLine());
                        parserException.setCharPosition(ctx.getStart().getCharPositionInLine());
                        throw parserException;
                    }
                    break;
                }
                default:
                    throw new ParserException(
                            ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, BIT_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT));
            }
        } else {
            throw new ParserException(
                    ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, BIT_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }
    }
}
