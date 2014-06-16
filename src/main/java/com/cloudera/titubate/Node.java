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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A node in a graph of test actions.
 */
public abstract class Node {
  private static final Logger LOG = LoggerFactory.getLogger(Node.class);

  private long progress = System.currentTimeMillis();

  /**
   * Visit this node.
   *
   * @param state Random walk state passed between nodes
   * @param env test environment
   * @param properties node-specific properties
   * @throws NodeException if node visitation fails
   */
  public abstract void visit(Environment env, State state, Properties props)
    throws NodeException;

  /**
   * Checks if this node equals another. Equal nodes have the same string
   * representation.
   *
   * @param o other object
   * @return true if this object equals the other
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Node)) {
      return false;
    }
    return toString().equals(o.toString());
  }
  @Override
  public int hashCode() {
    return toString().hashCode();
  }
  /**
   * Gets the string representation of this node. The default implementation
   * returns the name of this node's implementation class.
   *
   * @return string form
   */
  @Override
  public String toString() {
    return this.getClass().getName();
  }

  /**
   * Notes progress on the work for this node.
   */
  public synchronized void progress() {
    progress = System.currentTimeMillis();
  }
  /**
   * Gets the last time when progress was noted for this node.
   *
   * @return progress timestamp
   */
  public synchronized long getProgress() {
    return progress;
  }
}
