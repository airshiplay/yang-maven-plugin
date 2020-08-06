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

import org.junit.Test;
import com.airlenet.yang.compiler.datamodel.YangModule;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.YangNodeType;
import com.airlenet.yang.compiler.datamodel.YangRpc;
import com.airlenet.yang.compiler.datamodel.YangStatusType;
import com.airlenet.yang.compiler.datamodel.YangTypeDef;
import com.airlenet.yang.compiler.datamodel.utils.builtindatatype.YangDataTypes;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.YangUtilsParserManager;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test cases for testing Rpc listener functionality.
 */
public class RpcListenerTest {

    private final YangUtilsParserManager manager = new YangUtilsParserManager();

    /**
     * Checks valid rpc statements.
     */
    @Test
    public void processValidRpcStatement() throws IOException, ParserException {

        YangNode node = manager.getDataModel("src/test/resources/ValidRpcStatement.yang");

        assertThat((node instanceof YangModule), is(true));
        assertThat(node.getNodeType(), is(YangNodeType.MODULE_NODE));
        YangModule yangNode = (YangModule) node;
        assertThat(yangNode.getName(), is("rock"));

        YangRpc yangRpc = (YangRpc) yangNode.getChild();
        assertThat(yangRpc.getName(), is("rock-the-house"));
        assertThat(yangRpc.getDescription(), is("\"description\""));
        assertThat(yangRpc.getReference(), is("\"reference\""));
        assertThat(yangRpc.getStatus(), is(YangStatusType.CURRENT));

        YangTypeDef typeDef = (YangTypeDef) yangRpc.getChild();
        assertThat(typeDef.getName(), is("my-type"));
        assertThat(typeDef.getStatus(), is(YangStatusType.DEPRECATED));
        assertThat(typeDef.getTypeDefBaseType().getDataType(), is(YangDataTypes.INT32));
    }
}
