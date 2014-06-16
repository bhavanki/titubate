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

/**
 * A node that runs a <code>CallableAction</code> when visited.
 */
public class CallableNode extends Node {
    private final CallableAction action;
    /**
     * Creates a new node.
     *
     * @param action action to run when this node is visited
     */
    public CallableNode(CallableAction action) {
        this.action = action;
    }
    @Override
    public void visit(Environment env, State state, Properties props)
        throws NodeException {
        action.initialize(env, state, props);
        try {
            action.call();
        } catch (Exception e) {
            throw new NodeException(e);
        }
    }
    /**
     * Gets the string representation of this node. This implementation returns
     * the name of this node's action class.
     *
     * @return string form
     */
    @Override
    public String toString() {
        return action.getClass().getName();
    }
}
