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

import com.cloudera.titubate.Recorder.Status;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MemoryRecorderTest {
    private MemoryRecorder r;
    @Before public void setUp(){
        r = new MemoryRecorder();
    }

    @Test public void testRecording() {
        State s1 = new State();
        s1.set("number", 1);
        assertEquals(Status.SAVED, r.record("n1", s1));
        State s2 = new State();
        s2.set("number", 2);
        assertEquals(Status.SAVED, r.record("n2", s2));
        State s3 = new State();
        s3.set("number", 3);
        assertEquals(Status.SAVED, r.record("n3", s3));

        int i = 1;
        for (TrailElement e : r.getTrail()) {
            assertEquals("n" + i, e.getNodeId());
            assertEquals(i, e.getState().get("number"));
            i++;
        }
        assertEquals(4, i);
    }
    @Test public void testCapacity() {
        r = new MemoryRecorder(2);

        State s1 = new State();
        s1.set("number", 1);
        assertEquals(Status.SAVED, r.record("n1", s1));
        State s2 = new State();
        s2.set("number", 2);
        assertEquals(Status.SAVED, r.record("n2", s2));
        State s3 = new State();
        s3.set("number", 3);
        assertEquals(Status.SAVED_WITH_OVERFLOW, r.record("n3", s3));

        int i = 2;
        for (TrailElement e : r.getTrail()) {
            assertEquals("n" + i, e.getNodeId());
            assertEquals(i, e.getState().get("number"));
            i++;
        }
        assertEquals(4, i);
    }
    @Test public void testClearTrail() {
        assertEquals(Status.SAVED, r.record("n1", new State()));
        r.clearTrail();
        for (TrailElement e : r.getTrail()) {
            fail("Elements remained after clearing trail");
        }
    }
}
