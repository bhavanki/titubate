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
import org.junit.Test;
import static org.junit.Assert.*;

public class StateTest {
    private State state;
    @Before public void setUpClass() {
        state = new State();
    }
    @Test public void testObject() {
        state.set("test", this);
        assertTrue(state.has("test"));
        assertSame(this, state.get("test"));
        assertFalse(state.has("foo"));

        assertNull(state.get("foo"));
        assertNull(state.get(null));
    }
    @Test public void testString() {
        state.set("test", "case");
        assertTrue(state.has("test"));
        assertEquals("case", state.get("test"));
    }
    @Test public void testInt() {
        state.set("test", 42);
        assertTrue(state.has("test"));
        assertEquals(Integer.valueOf(42), state.get("test"));
    }
    @Test public void testLong() {
        state.set("test", 187L);
        assertTrue(state.has("test"));
        assertEquals(Long.valueOf(187L), state.get("test"));
    }
    @Test public void testRemove() {
        state.set("test", "case");
        assertTrue(state.has("test"));
        state.remove("test");
        assertFalse(state.has("test"));
        assertNull(state.get("test"));
    }
}
