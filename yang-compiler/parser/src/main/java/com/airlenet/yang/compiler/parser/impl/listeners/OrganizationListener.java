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

import com.airlenet.yang.compiler.datamodel.YangModule;
import com.airlenet.yang.compiler.datamodel.YangSubModule;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerErrorLocation;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerErrorMessageConstruction;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerErrorType;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerValidation;

import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.ORGANIZATION_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.OrganizationStatementContext;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 * meta-stmts          = ;; these stmts can appear in any order
 *                       [organization-stmt stmtsep]
 *                       [contact-stmt stmtsep]
 *                       [description-stmt stmtsep]
 *                       [reference-stmt stmtsep]
 * organization-stmt   = organization-keyword sep string
 *                            optsep stmtend
 *
 * ANTLR grammar rule
 * meta_stmts : organization_stmt? contact_stmt? description_stmt? reference_stmt?
 *            | organization_stmt? contact_stmt? reference_stmt? description_stmt?
 *            | organization_stmt? description_stmt? contact_stmt? reference_stmt?
 *            | organization_stmt? description_stmt? reference_stmt? contact_stmt?
 *            | organization_stmt? reference_stmt? contact_stmt? description_stmt?
 *            | organization_stmt? reference_stmt? description_stmt? contact_stmt?
 *            | contact_stmt? organization_stmt? description_stmt? reference_stmt?
 *            | contact_stmt? organization_stmt? reference_stmt? description_stmt?
 *            | contact_stmt? reference_stmt? organization_stmt? description_stmt?
 *            | contact_stmt? reference_stmt? description_stmt? organization_stmt?
 *            | contact_stmt? description_stmt? reference_stmt? organization_stmt?
 *            | contact_stmt? description_stmt? organization_stmt? reference_stmt?
 *            | reference_stmt? contact_stmt? organization_stmt? description_stmt?
 *            | reference_stmt? contact_stmt? description_stmt? organization_stmt?
 *            | reference_stmt? organization_stmt? contact_stmt? description_stmt?
 *            | reference_stmt? organization_stmt? description_stmt? contact_stmt?
 *            | reference_stmt? description_stmt? organization_stmt? contact_stmt?
 *            | reference_stmt? description_stmt? contact_stmt? organization_stmt?
 *            | description_stmt? reference_stmt? contact_stmt? organization_stmt?
 *            | description_stmt? reference_stmt? organization_stmt? contact_stmt?
 *            | description_stmt? contact_stmt? reference_stmt? organization_stmt?
 *            | description_stmt? contact_stmt? organization_stmt? reference_stmt?
 *            | description_stmt? organization_stmt? contact_stmt? reference_stmt?
 *            | description_stmt? organization_stmt? reference_stmt? contact_stmt?
 *            ;
 * organization_stmt : ORGANIZATION_KEYWORD string STMTEND;
 */

/**
 * Represents listener based call back function corresponding to the
 * "organization" rule defined in ANTLR grammar file for corresponding ABNF rule
 * in RFC 6020.
 */
public final class OrganizationListener {

    /**
     * Creates a new organization listener.
     */
    private OrganizationListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar rule
     * (organization), perform validations and update the data model tree.
     *
     * @param listener Listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processOrganizationEntry(TreeWalkListener listener,
                                                OrganizationStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, ORGANIZATION_DATA, ctx.string().getText(),
                             ListenerErrorLocation.ENTRY);

        // Obtain the node of the stack.
        Parsable tmpNode = listener.getParsedDataStack().peek();
        switch (tmpNode.getYangConstructType()) {
            case MODULE_DATA: {
                YangModule module = (YangModule) tmpNode;
                module.setOrganization(ctx.string().getText());
                break;
            }
            case SUB_MODULE_DATA: {
                YangSubModule subModule = (YangSubModule) tmpNode;
                subModule.setOrganization(ctx.string().getText());
                break;
            }
            default:
                throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, ORGANIZATION_DATA,
                                                                        ctx.string().getText(), ListenerErrorLocation.ENTRY));
        }
    }
}
