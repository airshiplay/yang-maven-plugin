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
 *  feature-stmt        = feature-keyword sep identifier-arg-str optsep
 *                        (";" /
 *                         "{" stmtsep
 *                             ;; these stmts can appear in any order
 *                             *(if-feature-stmt stmtsep)
 *                             [status-stmt stmtsep]
 *                             [description-stmt stmtsep]
 *                             [reference-stmt stmtsep]
 *                         "}")
 *
 *
 *
 * ANTLR grammar rule
 * featureStatement : FEATURE_KEYWORD string (STMTEND | LEFT_CURLY_BRACE featureBody RIGHT_CURLY_BRACE);
 */

import com.airlenet.yang.compiler.datamodel.YangFeature;
import com.airlenet.yang.compiler.datamodel.YangFeatureHolder;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.FEATURE_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.FeatureStatementContext;

/**
 * Represents listener based call back function corresponding to the "feature"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class FeatureListener {

    /**
     * Creates a new feature listener.
     */
    private FeatureListener() {
    }

    /**
     * Performs validation and updates the data model tree.It is called when parser receives
     * an input matching the grammar rule (feature).
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processFeatureEntry(TreeWalkListener listener,
                                           FeatureStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, FEATURE_DATA, ctx.string().getText(), ListenerErrorLocation.ENTRY);

        String identifier = ListenerUtil.getValidIdentifier(ctx.string().getText(), FEATURE_DATA, ctx);

        // Obtain the node of the stack.
        Parsable tmpNode = listener.getParsedDataStack().peek();
        if (tmpNode instanceof YangFeatureHolder) {
            YangFeatureHolder featureHolder = (YangFeatureHolder) tmpNode;

            YangFeature feature = new YangFeature();
            feature.setName(identifier);

            feature.setLineNumber(ctx.getStart().getLine());
            feature.setCharPosition(ctx.getStart().getCharPositionInLine());
            feature.setFileName(listener.getFileName());
            featureHolder.addFeatureList(feature);
            listener.getParsedDataStack().push(feature);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, FEATURE_DATA,
                    ctx.string().getText(), ListenerErrorLocation.ENTRY));
        }
    }

    /**
     * Perform validations and updates the data model tree.It is called when parser exits from
     * grammar rule(feature).
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processFeatureExit(TreeWalkListener listener,
                                          FeatureStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, FEATURE_DATA, ctx.string().getText(), ListenerErrorLocation.EXIT);

        if (listener.getParsedDataStack().peek() instanceof YangFeature) {
            listener.getParsedDataStack().pop();
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, FEATURE_DATA,
                    ctx.string().getText(), ListenerErrorLocation.EXIT));
        }
    }
}
