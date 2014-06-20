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
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AdjListTest {
    private static final int MAX_TRIES = 1000000;  // one million
    private AdjList l;
    @Before public void setUp() throws Exception {
        l = new AdjList();
    }
    @Test public void testFewEdges() {
        testEdges(3);
    }
    @Test public void testManyEdges() {
        testEdges(43);
    }
    private void testEdges(int numEdges) {
        for (int i = 1; i <= numEdges; i++) {
            l.addEdge(Integer.toString(i), i);
        }
        assertEquals(numEdges, l.size());
        for (int i = 1; i <= numEdges; i++) {
            assertEquals(i, l.weight(Integer.toString(i)));
        }
        boolean chosen[] = new boolean[numEdges];
        for (int i = 0; i < MAX_TRIES; i++) {
            String nextId = l.randomNeighbor();
            chosen[Integer.parseInt(nextId) - 1] = true;
            if (allTrue(chosen)) {
                return;
            }
        }
        fail("At least one node not chosen after " + MAX_TRIES + " tries: " +
             Arrays.toString(chosen));
    }
    private boolean allTrue(boolean[] bs) {
        for (boolean b : bs) {
            if (!b) {
                return false;
            }
        }
        return true;
    }
    @Test(expected=IllegalStateException.class)
    public void testNoEdges() {
        l.randomNeighbor();
    }
    @Test(expected=IllegalArgumentException.class)
    public void testZeroWeightEdge() {
        l.addEdge("foo", 0);
    }
    @Test(expected=IllegalArgumentException.class)
    public void testNegativeWeightEdge() {
        l.addEdge("foo", -42);
    }
}
