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
 * A dummy node which does nothing but print out its name if requested.
 */
 public class DummyNode extends Node {
    private static final Logger LOG = LoggerFactory.getLogger(DummyNode.class);

    /**
     * The name of the property to set to have this node print its name upon
     * visitation. Recognized values: trace, debug, info, warn, error.
     */
    public static final String PRINT_NAME_PROPERTY = "printName";

    private final String name;
    public DummyNode(String name) {
      this.name = name;
    }

    @Override
    public void visit(Environment env, State state, Properties props) {
        String printRequested = props.getProperty(PRINT_NAME_PROPERTY);
        if (printRequested != null) {
            if ("error".equalsIgnoreCase(printRequested)) {
                LOG.error(name);
            } else if ("warn".equalsIgnoreCase(printRequested)) {
                LOG.warn(name);
            } else if ("info".equalsIgnoreCase(printRequested)) {
                LOG.info(name);
            } else if ("debug".equalsIgnoreCase(printRequested)) {
                LOG.debug(name);
            } else if ("trace".equalsIgnoreCase(printRequested)) {
                LOG.trace(name);
            } else {
                LOG.error("Unrecognized log level " + printRequested);
                LOG.error(name);
            }
        }
    }

    /**
     * Gets the string representation of this node. This implementation returns
     * the node's name.
     *
     * @return string form
     */
    @Override
    public String toString() {
        return name;
    }
}

