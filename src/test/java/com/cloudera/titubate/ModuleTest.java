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

import com.cloudera.titubate.Module.AdjList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class ModuleTest {
    private static final String SOURCE = "test.xml";
    private static final String INIT_NODE_ID = "node0";

    private NodeKeeper nk;
    private Map<String, AdjList> adjMap;
    private ModuleBuilder mb;
    private Environment env;
    private State state;
    @Before public void setUp() {
        nk = createMock(NodeKeeper.class);
        adjMap = new HashMap<String, AdjList>();
        mb = new ModuleBuilder(SOURCE, nk).adjMap(adjMap);
        env = createMock(Environment.class);
        state = createMock(State.class);
    }

    @Test public void testToString() {
        assertEquals(SOURCE, mb.build(INIT_NODE_ID).toString());
    }
    @Test public void testGetNode() {
        Node n = createMock(Node.class);
        expect(nk.getNode("abcde.f")).andReturn(n);
        replay(nk);
        Module m = mb.build(INIT_NODE_ID);
        assertSame(n, m.getNode("abcde.f"));
    }
    @Test public void testGetProps() {
        Module m = mb.build(INIT_NODE_ID);
        Properties p1 = m.getProps("node1");
        assertNotNull(p1);
        assertSame(p1, m.getProps("node1"));
        Properties p2 = m.getProps("node2");
        assertNotSame(p1, p2);
    }

    @Test public void testDoVisit_Untimed() throws Exception {
        Node n = createMock(Node.class);
        expect(nk.getNode("x")).andReturn(n);
        replay(nk);
        n.visit(eq(env), eq(state), anyObject(Properties.class));
        replay(n);
        mb.build(INIT_NODE_ID).doVisit("x", env, state);
        verify(n);
    }
    @Test public void testDoVisit_Timed() throws Exception {
        Node n = createMock(CallableNode.class);
        expect(nk.getNode("x")).andReturn(n);
        replay(nk);
        n.progress();
        n.visit(eq(env), eq(state), anyObject(Properties.class));
        replay(n);
        mb.build(INIT_NODE_ID).doVisit("x", env, state);
        verify(n);
    }

    @Test public void testThreeNodeGraph() throws Exception {
        testThreeNodeGraph(false, false, false, true, false);
    }
    @Test public void testLimitHops() throws Exception {
        testThreeNodeGraph(true, false, false, true, false);
    }
    @Test public void testLimitTime() throws Exception {
        testThreeNodeGraph(false, true, false, true, false);
    }
    @Test public void testFixture() throws Exception {
        testThreeNodeGraph(false, false, true, true, false);
    }
    @Test public void testFixture_NoTearDown() throws Exception {
        testThreeNodeGraph(false, false, true, false, false);
    }
    @Test(expected=NodeException.class)
    public void testNodeFailure() throws Exception {
        testThreeNodeGraph(false, false, false, false, true);
    }
    private void testThreeNodeGraph(boolean limitHops, boolean limitTime,
                                    boolean useFixture, boolean doTearDown,
                                    boolean failNode)
        throws Exception {
        String n1Id = "node1";
        AdjList l = new AdjList();
        l.addEdge(n1Id, 1);
        adjMap.put(INIT_NODE_ID, l);
        l = new AdjList();
        l.addEdge("END", 1);
        adjMap.put(n1Id, l);

        Node n0 = createMock(Node.class);
        expect(nk.getNode(INIT_NODE_ID)).andReturn(n0);
        Node n1;
        if (limitTime) {
            n1 = new Node() {
                @Override
                public void visit(Environment env, State state, Properties props)
                    throws NodeException {
                    try {
                        Thread.sleep(1500L);
                    } catch (InterruptedException e) {
                        throw new NodeException(e);
                    }
                }
            };

        } else {
            n1 = createMock(Node.class);
        }
        expect(nk.getNode(n1Id)).andReturn(n1).anyTimes();
        Node end = new DummyNode("END");
        if (!limitHops && !limitTime && !failNode) {
            expect(nk.getNode("END")).andReturn(end).anyTimes();
        }
        replay(nk);

        n0.visit(eq(env), eq(state), anyObject(Properties.class));
        replay(n0);
        if (!limitTime) {
            n1.visit(eq(env), eq(state), anyObject(Properties.class));
            if (failNode) {
                expectLastCall().andThrow(new IllegalStateException());
            }
            replay(n1);
        }

        Fixture fixture = createMock(Fixture.class);
        if (useFixture) {
            fixture.setUp(env, state);
            if (doTearDown) {
                fixture.tearDown(env, state);
            }
            replay(fixture);
            mb.fixture(fixture);
        }

        Map<String, Properties> nodeProps = new HashMap<String, Properties>();
        Properties initProps = new Properties();
        nodeProps.put(Module.INIT_NODE_ID, initProps);
        if (limitHops) {
            initProps.setProperty(Module.PROPERTY_MAX_HOPS, "1");
        }
        if (limitTime) {
            initProps.setProperty(Module.PROPERTY_MAX_SECONDS, "1");
        }
        if (useFixture && !doTearDown) {
            initProps.setProperty("teardown", "false");
        }
        mb.nodeProps(nodeProps);

        try {
            mb.build(INIT_NODE_ID).visit(env, state, new Properties());
        } finally {
            verify(n0);
            if (!limitTime) {
                verify(n1);
            }

            if (useFixture) {
                verify(fixture);
            }
        }
    }
}
