/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.titubate;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class NodeKeeperTest {
    private NodeFactory nf;
    private NodeKeeper nk;
    @Before public void setUp() throws Exception {
        nf = createMock(NodeFactory.class);
        nk = new NodeKeeper(nf);
    }
    @Test public void testGet() {
        String id = "id";
        Node n = createMock(Node.class);
        expect(nf.createNode(id)).andReturn(n);
        replay(nf);

        Node n1 = nk.getNode(id);
        assertSame(n, n1);
        Node n2 = nk.getNode(id);
        assertSame(n, n2);
    }
    @Test public void testHasAndAdd() {
        Node n = new DummyNode("test");
        assertFalse(nk.hasNode("test"));
        nk.addNode("test", n);
        assertTrue(nk.hasNode("test"));
        assertSame(n, nk.getNode("test"));
        assertFalse(nk.hasNode("nope"));
    }
}
