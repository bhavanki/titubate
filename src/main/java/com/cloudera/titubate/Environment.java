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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read-only environment that is available for random walk tests.
 */
public class Environment {
  private static final Logger LOG = LoggerFactory.getLogger(Environment.class);

  /**
   * The reserved ID used to store a test ID value.
   */
  public static final String KEY_TEST_ID = "_test_id";

  private final Map<String, Object> env;

  /**
   * Creates a new environment, taking data from the given map.
   *
   * @param env map of environment variable names to values
   */
  public Environment(Map<String, ?> env) {
    this.env = Collections.unmodifiableMap(new HashMap<String, Object>(env));
  }
  /**
   * Creates a new environment, taking data from the given properties.
   *
   * @param env properties to use as environment variables
   */
  public Environment(Properties p) {
    Map<String, Object> m = new HashMap<String, Object>();
    for (Map.Entry<Object, Object> e : p.entrySet()) {
      m.put(e.getKey().toString(), e.getValue());
    }
    this.env = Collections.unmodifiableMap(m);
  }

  /**
   * Checks if this environment has a value for the given key.
   *
   * @param key key
   * @return true if value is present, false otherwise
   */
  public boolean has(String key) {
    return env.containsKey(key);
  }
  /**
   * Gets a value from this environment for the given key.
   *
   * @param key key
   * @return value, or null if not present
   */
  public Object get(String key) {
    return env.get(key);
  }
  /**
   * Gets an integer value from this environment for the given key.
   *
   * @param key key
   * @return integer value, or null if not present
   * @throws ClassCastException if value is not an <code>Integer</code>
   */
  public Integer getInt(String key) {
    return (Integer) get(key);
  }
  /**
   * Gets a long value from this environment for the given key.
   *
   * @param key key
   * @return long value, or null if not present
   * @throws ClassCastException if value is not a <code>Long</code>
   */
  public Long getLong(String key) {
    return (Long) get(key);
  }
  /**
   * Gets a string value from this environment for the given key.
   *
   * @param key key
   * @return string value, or null if not present
   * @throws ClassCastException if value is not a <code>String</code>
   */
  public String getString(String key) {
    return (String) get(key);
  }

  public String dump() {
    // FIXME refactor better
    return State.dump(env);
  }
}
