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

/**
 * A recorder remembers the trail of nodes in a test run.
 */
public interface Recorder {
    /**
     * The status returned from calling {@link #record(String,State)}.
     */
    public enum Status {
        SAVED, SAVED_WITH_OVERFLOW, FAILED
    }

    /**
     * Records visitation of a node.
     *
     * @param nodeId node ID
     * @param state state after visitation
     * @return result of recording
     */
    Status record(String nodeId, State state);
    /**
     * Gets the trail comprising the recording.
     *
     * @return iterable through trail
     */
    Iterable<TrailElement> getTrail();
}
