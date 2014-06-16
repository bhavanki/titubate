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
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class EnvironmentTest {
    private static Properties envProps;

    private Environment env;
    @BeforeClass public static void setUpClass() {
        envProps = new Properties();
        envProps.setProperty("foo", "bar");
        envProps.setProperty("ding", "dong");
    }
    @Test public void testByProperties() {
        env = new Environment(envProps);
        testGetters();
    }
    @Test public void testByMap() {
        Map<String, String> m = new java.util.HashMap<String, String>();
        for (Map.Entry<Object, Object> e : envProps.entrySet()) {
            m.put(e.getKey().toString(), e.getValue().toString());
        }
        env = new Environment(m);
    }
    private void testGetters() {
        assertTrue(env.has("foo"));
        assertFalse(env.has("zip"));
        assertFalse(env.has(null));

        assertEquals("bar", env.get("foo"));
        assertEquals("dong", env.get("ding"));
        assertNull(env.get("zip"));
        assertNull(env.get(null));
    }

    @Test public void testNumerics() {
        Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("integer", "42");
        m.put("long", "187");
        env = new Environment(m);
        assertEquals(Integer.valueOf(42), env.getInt("integer"));
        assertEquals(Long.valueOf(187L), env.getLong("long"));

        assertNull(env.getInt("zip"));
        assertNull(env.getLong("zip"));
        assertNull(env.getInt(null));
        assertNull(env.getLong(null));
    }
    @Test public void testEmpty() {
        env = new Environment();
        assertNull(env.get("foo"));
    }
}
