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

import com.airlenet.yang.compiler.datamodel.DefaultDenyWriteExtension;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerErrorLocation;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerErrorMessageConstruction;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerErrorType;
import com.airlenet.yang.compiler.parser.impl.parserutils.ListenerValidation;

import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.DEFAULT_DENY_WRITE_DATA;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.DefaultDenyWriteStatementContext;

public final class DefaultDenyWriteExtRefListener {

    private DefaultDenyWriteExtRefListener() {
    }

    public static void processDefaultDenyWriteStructureEntry(
            TreeWalkListener listener, DefaultDenyWriteStatementContext ctx) {
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, DEFAULT_DENY_WRITE_DATA,
                             "", ListenerErrorLocation.ENTRY);

        Parsable tmpData = listener.getParsedDataStack().peek();
        if (tmpData instanceof DefaultDenyWriteExtension) {
            DefaultDenyWriteExtension holder = (DefaultDenyWriteExtension) tmpData;
            holder.setDefaultDenyWrite(true);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(
                    ListenerErrorType.INVALID_HOLDER, DEFAULT_DENY_WRITE_DATA, "", ListenerErrorLocation.ENTRY));
        }
    }
}
