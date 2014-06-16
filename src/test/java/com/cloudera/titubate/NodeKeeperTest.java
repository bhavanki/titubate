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

import java.io.File;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class NodeKeeperTest {
    public static class NodeKeeperTestAction extends CallableAction {
        @Override
        public Void call() {
            return null;
        }
    }
    private static File moduleDir;
    @BeforeClass public static void setUpClass() throws Exception {
        moduleDir = new File(NodeKeeperTest.class.getResource("/").toURI());
    }
    private NodeKeeper nk;
    @Before public void setUp() throws Exception {
        nk = new NodeKeeper(moduleDir);
    }
    @Test public void testEND() {
        assertFalse(nk.hasNode("END"));
        Node n = nk.getNode("END");
        assertTrue(n instanceof DummyNode);
        assertEquals("END", n.toString());
        assertTrue(nk.hasNode("END"));

        assertSame(n, nk.getNode("END"));
    }
    @Test public void testCallableNode() {
        String actionClassName = NodeKeeperTestAction.class.getName();
        assertFalse(nk.hasNode(actionClassName));
        Node n = nk.getNode(actionClassName);
        assertTrue(n instanceof CallableNode);
        assertEquals(actionClassName, n.toString());
        assertTrue(nk.hasNode(actionClassName));

        assertSame(n, nk.getNode(actionClassName));
    }
    // TBD - XML

    @Test public void testHasAndAdd() {
        Node n = new DummyNode("test");
        assertFalse(nk.hasNode("test"));
        nk.addNode("test", n);
        assertTrue(nk.hasNode("test"));
        assertSame(n, nk.getNode("test"));
        assertFalse(nk.hasNode("nope"));
    }
}
