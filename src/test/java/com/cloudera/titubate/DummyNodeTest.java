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

public class DummyNodeTest {
    private static final String NAME = "dumdum";
    private Environment env;
    private State state;
    private DummyNode n;
    @Before public void setUp() {
        env = new Environment();
        state = new State();
        n = new DummyNode(NAME);
    }
    @Test public void testToString() {
        assertEquals(NAME, n.toString());
    }
    @Test public void testVisit() {
        Properties props = new Properties();
        props.setProperty(DummyNode.PRINT_NAME_PROPERTY, "info");
        n.visit(env, state, props);
        // nothing to assert right now
    }
    @Test public void testVisit_Silent() {
        Properties props = new Properties();
        n.visit(env, state, props);
        // nothing to assert right now
    }
    @Test public void testVisit_Unrecognized() {
        Properties props = new Properties();
        props.setProperty(DummyNode.PRINT_NAME_PROPERTY, "foo");
        n.visit(env, state, props);
        // nothing to assert right now
    }
}
