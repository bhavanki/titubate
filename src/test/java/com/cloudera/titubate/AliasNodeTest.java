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

public class AliasNodeTest {
    private static final String ID = "aka";
    private Environment env;
    private State state;
    private AliasNode n;
    @Before public void setUp() {
        env = new Environment();
        state = new State();
        n = new AliasNode(ID);
    }
    @Test public void testToString() {
        assertEquals(ID, n.toString());
    }
    @Test(expected=NodeException.class)
    public void testVisit() throws Exception {
        Properties props = new Properties();
        n.visit(env, state, props);
    }
    @Test public void testTargeting() {
        n.setTargetId("targetId");
        assertEquals("targetId", n.getTargetId());
    }
}
