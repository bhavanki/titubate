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

import java.util.Map;

/**
 * A keeper of {@link Node}s, indexed by ID; essentially a wrapper of a
 * {@link NodeFactory}. This class is not thread-safe.
 */
public class NodeKeeper {
    private final Map<String,Node> nodes = new java.util.HashMap<String,Node>();
    private final NodeFactory nodeFactory;

    /**
     * Creates a new keeper.
     *
     * @param nodeFactory node factory to use when creating new nodes
     */
    public NodeKeeper(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    /**
     * Gets a node. If it does not already exist, it is created using this
     * keeper's node factory.
     *
     * @param id node ID
     * @return node specified by id
     * @throws NodeCreationException if a new node needed to be created, but
     * there was a problem building it
     */
    public Node getNode(String id) {
        if (nodes.containsKey(id)) {
            return nodes.get(id);
        }

        Node node = nodeFactory.createNode(id);
        nodes.put(id, node);
        return node;
    }

    /**
     * Checks if a node with the given ID already exists.
     *
     * @param id node ID
     * @return true if node exists
     */
    public boolean hasNode(String id) {
        return nodes.containsKey(id);
    }
    /**
     * Adds a node. Any node already created with the given ID is discarded.
     *
     * @param id node ID
     * @param n node
     */
    public void addNode(String id, Node n) {
        nodes.put(id, n);
    }
}
