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

public class NodeTest {
    private static class TestNode extends Node {
        private boolean visited = false;
        @Override
        public void visit(Environment env, State state, Properties props) {
            visited = true;
        }
    }
    private static class TestNode2 extends Node {
        @Override
        public void visit(Environment env, State state, Properties props) {
        }
    }

    private TestNode n;
    @Before public void setUp() {
        n = new TestNode();
    }
    @Test public void testEquals() {
        assertTrue(n.equals(n));
        assertFalse(n.equals(null));
        Node n2 = new TestNode2();
        assertFalse(n.equals(n2));
        assertFalse(n2.equals(n));
    }
    @Test public void testHashCode() {
        int c = n.hashCode();
        assertEquals(c, new TestNode().hashCode());
    }
    @Test public void testToString() {
        assertEquals(TestNode.class.getName(), n.toString());
    }
    @Test public void testProgress() throws InterruptedException {
        long now = System.currentTimeMillis();
        n.progress();
        long delta = n.getProgress() - now;
        assertTrue(delta >= 0 && delta < 100L);  // 100 ms resolution
    }
}
