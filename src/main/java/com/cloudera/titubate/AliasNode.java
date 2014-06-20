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
 * A node that serves as an alias to another node. An alias node may not be
 * visited.
 */
public class AliasNode extends Node {

    private final String id;
    private String targetId;

    public AliasNode(String id) {
      this.id = id;
      targetId = null;
    }

    @Override
    public void visit(Environment env, State state, Properties props)
        throws NodeException {
      throw new NodeException("May not visit alias " + id);
    }

    /**
     * Gets the string representation of this node. This implementation returns
     * the node's ID.
     *
     * @return string form
     */
    public String toString() {
      return id;
    }

    /**
     * Sets the ID of the target node for this alias node.
     *
     * @param targetId target node ID
     */
    public void setTargetId(String targetId) {
      this.targetId = targetId;
    }
    /**
     * Gets the ID of the target node for this alias node.
     *
     * @return target node ID
     */
    public String getTargetId() {
        return targetId;
    }
}
