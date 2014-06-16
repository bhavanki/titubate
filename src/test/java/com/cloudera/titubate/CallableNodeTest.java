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

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CallableNodeTest {
    private static class TestAction extends CallableAction {
        private Environment expectedEnv;
        private State expectedState;
        private Properties expectedProperties;
        boolean called = false;
        TestAction(Environment env, State state, Properties props) {
            expectedEnv = env;
            expectedState = state;
            expectedProperties = props;
        }
        @Override public Void call() {
            assertSame(expectedEnv, env());
            assertSame(expectedState, state());
            assertSame(expectedProperties, properties());
            called = true;
            return null;
        }
    }

    private Environment env;
    private State state;
    private Properties props;
    private CallableAction a;
    private CallableNode n;
    @Before public void setUp() throws Exception {
        env = new Environment();
        state = new State();
        props = new Properties();
        a = new TestAction(env, state, props);
        n = new CallableNode(a);
    }

    @Test public void testToString() {
        assertEquals(a.getClass().getName(), n.toString());
    }
    @Test public void testVisit() throws NodeException {
        n.visit(env, state, props);
        assertTrue(((TestAction) a).called);
    }
    @Test(expected=NodeException.class)
    public void testVisit_Throw() throws NodeException {
        a = new CallableAction() {
            @Override
            public Void call() {
                throw new IllegalArgumentException("nope");
            }
        };
        n = new CallableNode(a);
        try {
            n.visit(env, state, props);
        } catch (NodeException e) {
            assertTrue(e.getCause().getClass().equals(IllegalArgumentException.class));
            throw e;
        }
    }
}
