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

import com.airlenet.yang.compiler.datamodel.YangAtomicPath;
import com.airlenet.yang.compiler.datamodel.YangCompilerAnnotation;
import com.airlenet.yang.compiler.datamodel.YangModule;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.YangSubModule;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.linker.impl.YangResolutionInfoImpl;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import java.util.List;

import static com.airlenet.yang.compiler.datamodel.utils.DataModelUtils.addResolutionInfo;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.COMPILER_ANNOTATION_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.CompilerAnnotationStatementContext;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 *   compiler-annotation-stmt = prefix:compiler-annotation-keyword string
 *                          "{"
 *                              [app-data-structure-stmt stmtsep]
 *                              [app-extended-stmt stmtsep]
 *                          "}"
 *
 * ANTLR grammar rule
 *   compilerAnnotationStatement : COMPILER_ANNOTATION string LEFT_CURLY_BRACE
 *        compilerAnnotationBodyStatement RIGHT_CURLY_BRACE;
 *
 *   compilerAnnotationBodyStatement : appDataStructureStatement? appExtendedStatement? ;
 */

/**
 * Represents listener based call back function corresponding to the "compiler-annotation"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class CompilerAnnotationListener {

    /**
     * Creates a new compiler-annotation listener.
     */
    private CompilerAnnotationListener() {
    }

    /**
     * Performs validation and updates the data model tree. It is called when parser receives an
     * input matching the grammar rule(compiler-annotation).
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processCompilerAnnotationEntry(TreeWalkListener listener,
                                                      CompilerAnnotationStatementContext ctx) {
        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, COMPILER_ANNOTATION_DATA, ctx.string().getText(), ListenerErrorLocation.ENTRY);
        String prefix = ListenerUtil.getValidPrefix(ctx.COMPILER_ANNOTATION().getText(), COMPILER_ANNOTATION_DATA, ctx);

        YangCompilerAnnotation compilerAnnotation = new YangCompilerAnnotation();
        compilerAnnotation.setPrefix(prefix);
        compilerAnnotation.setPath(ListenerUtil.removeQuotesAndHandleConcat(ctx.string().getText()));

        compilerAnnotation.setLineNumber(ctx.getStart().getLine());
        compilerAnnotation.setCharPosition(ctx.getStart().getCharPositionInLine());
        compilerAnnotation.setFileName(listener.getFileName());
        // Validate augment argument string
        List<YangAtomicPath> targetNodes = ListenerUtil.getValidAbsoluteSchemaNodeId(ctx.string().getText(),
                                                                        COMPILER_ANNOTATION_DATA, ctx);

        compilerAnnotation.setAtomicPathList(targetNodes);

        int line = ctx.getStart().getLine();
        int charPositionInLine = ctx.getStart().getCharPositionInLine();

        Parsable curData = listener.getParsedDataStack().peek();
        if (!(curData instanceof YangModule || curData instanceof YangSubModule)) {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, COMPILER_ANNOTATION_DATA,
                                                                    ctx.string().getText(), ListenerErrorLocation.ENTRY));
        }

        // Add resolution information to the list
        YangResolutionInfoImpl resolutionInfo = new YangResolutionInfoImpl<>(
                compilerAnnotation, (YangNode) curData, line, charPositionInLine);
        addToResolutionList(resolutionInfo, ctx);

        listener.getParsedDataStack().push(compilerAnnotation);
    }

    /**
     * Performs validation and updates the data model tree. It is called when parser
     * exits from grammar rule (compiler-annotation).
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processCompilerAnnotationExit(TreeWalkListener listener,
                                                     CompilerAnnotationStatementContext ctx) {

        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, COMPILER_ANNOTATION_DATA, ctx.string().getText(), ListenerErrorLocation.EXIT);
        if (!(listener.getParsedDataStack().peek() instanceof YangCompilerAnnotation)) {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, COMPILER_ANNOTATION_DATA,
                    ctx.string().getText(), ListenerErrorLocation.EXIT));
        }
        listener.getParsedDataStack().pop();
    }

    /**
     * Adds to resolution list.
     *
     * @param resolutionInfo resolution information.
     * @param ctx            context object of the grammar rule
     */
    private static void addToResolutionList(YangResolutionInfoImpl<YangCompilerAnnotation> resolutionInfo,
                                            CompilerAnnotationStatementContext ctx) {

        try {
            addResolutionInfo(resolutionInfo);
        } catch (DataModelException e) {
            throw new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(ListenerErrorType.UNHANDLED_PARSED_DATA,
                    COMPILER_ANNOTATION_DATA, ctx.COMPILER_ANNOTATION().getText(), ListenerErrorLocation.ENTRY, e.getMessage()));
        }
    }
}
