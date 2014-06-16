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

import java.io.File;
import java.util.Map;

/**
 * A keeper of {@link Node}s, indexed by ID. This class is not thread-safe.
 */
public class NodeKeeper {
    private final Map<String,Node> nodes = new java.util.HashMap<String,Node>();
    private final File moduleDir;

    /**
     * Creates a new keeper.
     *
     * @param moduleDir directory where XML module definitions reside
     */
    public NodeKeeper(File moduleDir) {
        this.moduleDir = moduleDir;
    }

    /**
     * Gets a node. If it does not already exist, it is created. The following
     * sorts of node IDs are supported for node creation.<p>
     *
     * <ul>
     * <li><i>*</i>.xml - file name for an XML module definition</li>
     * <li>END - a {@link DummyNode} with name "END"</li>
     * <li>anything else - the name of a {@link CallableAction} implementation
     * with a no-argument constructor</li>
     * </ul>
     *
     * @param id node ID
     * @return node specified by id
     */
    public Node getNode(String id) {
        if (nodes.containsKey(id)) {
            return nodes.get(id);
        }

        Node node;
        if (id.endsWith(".xml")) {
            try {
                node = new XmlModuleFactory(new File(moduleDir, id))
                    .getModule(this);
            } catch (Exception e) {  // FIXME
                throw new NodeCreationException("Failed to load module " + id, e);
            }
        } else if (id.equalsIgnoreCase("END")) {
            node = new DummyNode(id);
        } else {
            Object idObject;
            try {
                idObject = Class.forName(id).newInstance();
            } catch (Exception e) {  // FIXME
                throw new NodeCreationException("Failed to create node object of class " +
                                   id, e);
            }
            if (idObject instanceof CallableAction) {
                node = new CallableNode((CallableAction) idObject);
            } else {
                throw new NodeCreationException("Unsupported node object type " + id);
            }
        }
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
