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

import com.airlenet.yang.compiler.datamodel.ResolvableType;
import com.airlenet.yang.compiler.datamodel.YangReferenceResolver;
import com.airlenet.yang.compiler.datamodel.YangSubModule;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.linker.exceptions.LinkerException;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;
import com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser;

import static com.airlenet.yang.compiler.datamodel.utils.DataModelUtils.validateMultipleDeviationStatement;
import static com.airlenet.yang.compiler.datamodel.utils.GeneratedLanguage.JAVA_GENERATION;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.MODULE_DATA;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.SUB_MODULE_DATA;
import static com.airlenet.yang.compiler.translator.tojava.YangDataModelFactory.getYangSubModuleNode;
import static com.airlenet.yang.compiler.utils.UtilConstants.ONE;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 * submodule-stmt      = optsep submodule-keyword sep identifier-arg-str
 *                             optsep
 *                             "{" stmtsep
 *                                 submodule-header-stmts
 *                                 linkage-stmts
 *                                 meta-stmts
 *                                 revision-stmts
 *                                 body-stmts
 *                             "}" optsep
 *
 * ANTLR grammar rule
 * submodule_stmt : SUBMODULE_KEYWORD identifier LEFT_CURLY_BRACE submodule_body* RIGHT_CURLY_BRACE;
 * submodule_body : submodule_header_statement linkage_stmts meta_stmts revision_stmts body_stmts;
 */

/**
 * Represents listener based call back function corresponding to the "submodule"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class SubModuleListener {

    /**
     * Creates a new sub module listener.
     */
    private SubModuleListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar rule (sub
     * module), perform validations and update the data model tree.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processSubModuleEntry(TreeWalkListener listener,
                                             GeneratedYangParser.SubModuleStatementContext ctx) {

        // Check if stack is empty.
        ListenerValidation.checkStackIsEmpty(listener, ListenerErrorType.INVALID_HOLDER, SUB_MODULE_DATA, ctx.identifier().getText(),
                          ListenerErrorLocation.ENTRY);

        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), SUB_MODULE_DATA, ctx);

        YangSubModule yangSubModule = getYangSubModuleNode(JAVA_GENERATION);
        yangSubModule.setName(identifier);

        yangSubModule.setLineNumber(ctx.getStart().getLine());
        yangSubModule.setCharPosition(ctx.getStart().getCharPositionInLine());
        yangSubModule.setFileName(listener.getFileName());
        if (ctx.submoduleBody().submoduleHeaderStatement().yangVersionStatement() == null) {
            yangSubModule.setVersion(ONE);
        }

        listener.getParsedDataStack().push(yangSubModule);
    }

    /**
     * It is called when parser exits from grammar rule (submodule), it perform
     * validations and update the data model tree.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processSubModuleExit(TreeWalkListener listener,
                                            GeneratedYangParser.SubModuleStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, SUB_MODULE_DATA, ctx.identifier().getText(),
                             ListenerErrorLocation.EXIT);

        Parsable tmpNode = listener.getParsedDataStack().peek();
        if (!(tmpNode instanceof YangSubModule)) {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, SUB_MODULE_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }

        YangSubModule subModule = (YangSubModule) tmpNode;
        if (subModule.getUnresolvedResolutionList(ResolvableType.YANG_COMPILER_ANNOTATION) != null
                && subModule.getUnresolvedResolutionList(ResolvableType.YANG_COMPILER_ANNOTATION).size() != 0
                && subModule.getChild() != null) {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_CHILD, MODULE_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }

        try {
            ((YangReferenceResolver) listener.getParsedDataStack().peek())
                    .resolveSelfFileLinking(ResolvableType.YANG_IF_FEATURE);
            ((YangReferenceResolver) listener.getParsedDataStack().peek())
                    .resolveSelfFileLinking(ResolvableType.YANG_USES);
            ((YangReferenceResolver) listener.getParsedDataStack().peek())
                    .resolveSelfFileLinking(ResolvableType.YANG_DERIVED_DATA_TYPE);
            ((YangReferenceResolver) listener.getParsedDataStack().peek())
                    .resolveSelfFileLinking(ResolvableType.YANG_LEAFREF);
            ((YangReferenceResolver) listener.getParsedDataStack().peek())
                    .resolveSelfFileLinking(ResolvableType.YANG_BASE);
            ((YangReferenceResolver) listener.getParsedDataStack().peek())
                    .resolveSelfFileLinking(ResolvableType.YANG_IDENTITYREF);
        } catch (DataModelException e) {
            LinkerException linkerException = new LinkerException(e.getMessage(), e);
            linkerException.setLine(e.getLineNumber());
            linkerException.setCharPosition(e.getCharPositionInLine());
            linkerException.setFileName(listener.getFileName());
            throw linkerException;
        }

        /*
         * Validate whether all deviation statement xpath is referring to same
         * module
         */
        try {
            validateMultipleDeviationStatement(subModule);
        } catch (DataModelException e) {
            throw new ParserException(e.getMessage());
        }
    }
}
