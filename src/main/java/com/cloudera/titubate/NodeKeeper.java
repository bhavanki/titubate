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

public class NodeKeeper {
    private final Map<String,Node> nodes = new java.util.HashMap<String,Node>();
    private final File moduleDir;

    public NodeKeeper(File moduleDir) {
        this.moduleDir = moduleDir;
    }

    /**
     * Gets a node. If it does not already exist, it is created.
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

    public boolean hasNode(String id) {
        return nodes.containsKey(id);
    }
    public void addNode(String id, Node n) {
        nodes.put(id, n);
    }
}
