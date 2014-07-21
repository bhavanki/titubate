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

import java.util.Queue;

/**
 * A simple recorder that keeps information in a bounded queue in memory.
 */
public class MemoryRecorder implements Recorder {
    private final int capacity;
    private final Queue<TrailElement> trail;

    /**
     * The default capacity for the recorder's queue.
     */
    public static final int DEFAULT_CAPACITY = Integer.MAX_VALUE;

    /**
     * Creates a new recorder with default capacity.
     */
    public MemoryRecorder() {
        this(DEFAULT_CAPACITY);
    }
    /**
     * Creates a new recorder.
     *
     * @param capacity capacity
     */
    public MemoryRecorder(int capacity) {
        this.capacity = capacity;
        trail = new java.util.ArrayDeque<TrailElement>();
    }

    /**
     * Records visitation of a node. If the bounded queue in this recorder is
     * at capacity, then the oldest item in the trail is dropped.
     *
     * @param nodeId node ID
     * @param state state after visitation
     * @return result of recording: {@link Recorder$Status#SAVED} if recording
     * was successful, or {@link Recorder$Status#SAVED_WITH_OVERFLOW} if
     * recording was successful but the oldest record was dropped
     */
    @Override
    public Status record(String nodeId, State state) {
        boolean removed = false;
        while (trail.size() >= capacity) {
            trail.remove();
            removed = true;
        }
        trail.add(new TrailElement(nodeId, new State(state)));
        return removed ? Status.SAVED_WITH_OVERFLOW : Status.SAVED;
    }
    @Override
    public Iterable<TrailElement> getTrail() {
        return trail;
    }
    /**
     * Clears this recorder.
     */
    public void clearTrail() {
        trail.clear();
    }
}
