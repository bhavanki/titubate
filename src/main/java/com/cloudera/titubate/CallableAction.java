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
import java.util.concurrent.Callable;

/**
 * An action to be called from within a {@link CallableNode}.
 */
public abstract class CallableAction implements Callable<Void> {
    private Environment env;
    private State state;
    private Properties props;

    /**
     * Initializes this action.
     *
     * @param env environment to use
     * @param state state to use
     * @param props node properties
     */
    public void initialize(Environment env, State state, Properties props) {
        this.env = env;
        this.state = state;
        this.props = props;
    }

    /**
     * Gets the environment to use.
     *
     * @return environment to use
     */
    protected Environment env() { return env; }
    /**
     * Gets the state to use.
     *
     * @return state to use
     */
    protected State state() { return state; }
    /**
     * Gets the node properties to use.
     *
     * @return node properties to use
     */
    protected Properties properties() { return props; }
}
