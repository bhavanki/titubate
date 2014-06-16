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
 * A read-only environment that is available for random walk tests. All keys
 * and values are strings.
 */
public class Environment {
  private static final Logger LOG = LoggerFactory.getLogger(Environment.class);

  /**
   * The reserved ID used to store a test ID value.
   */
  public static final String KEY_TEST_ID = "_test_id";

  private final Map<String, String> env;

  /**
   * Creates an empty environment.
   */
  public Environment() {
    env = Collections.emptyMap();
  }
  /**
   * Creates a new environment, taking data from the given map.
   *
   * @param env map of environment variable names to values
   */
  public Environment(Map<String, String> env) {
    this.env = Collections.unmodifiableMap(new HashMap<String, String>(env));
  }
  /**
   * Creates a new environment, taking data from the given properties.
   *
   * @param env properties to use as environment variables
   */
  public Environment(Properties p) {
    Map<String, String> m = new HashMap<String, String>();
    for (Map.Entry<Object, Object> e : p.entrySet()) {
      m.put(e.getKey().toString(), e.getValue().toString());
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
  public String get(String key) {
    return env.get(key);
  }
  /**
   * Gets a value from this environment for the given key, parsed as an int.
   *
   * @param key key
   * @return integer value, or null if not present
   * @throws NumberFormatException if value is not an integer string
   */
  public Integer getInt(String key) {
    String value = get(key);
    if (value == null) {
      return null;
    }
    return Integer.parseInt(value);
  }
  /**
   * Gets a value from this environment for the given key, parsed as a long.
   *
   * @param key key
   * @return long value, or null if not present
   * @throws NumberFormatException if value is not a long string
   */
  public Long getLong(String key) {
    String value = get(key);
    if (value == null) {
      return null;
    }
    return Long.parseLong(value);
  }

  public String dump() {
    // FIXME refactor better
    return State.dump(env);
  }
}
