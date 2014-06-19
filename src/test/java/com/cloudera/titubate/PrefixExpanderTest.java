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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PrefixExpanderTest {
    private static Map<String,String> expansions;
    @BeforeClass public static void setUpClass() {
        expansions = new java.util.HashMap<String,String>();
        expansions.put("a", "abcde");
        expansions.put("t", "thank.you");
        expansions.put("i", "am.a.robot");
    }
    private PrefixExpander x;
    @Before public void setUp() {
        x = new PrefixExpander(expansions);
    }
    @Test public void testXml() {
        assertEquals("whatever.xml", x.expand("whatever.xml"));
    }
    @Test public void testNoDot() {
        assertEquals("apple", x.expand("apple"));
    }
    @Test public void testNormal() {
        assertEquals("abcde.f", x.expand("a.f"));
        assertEquals("thank.you.madam", x.expand("t.madam"));
        assertEquals("am.a.robot.beepboop", x.expand("i.beepboop"));
    }
    @Test public void testMissing() {
        assertEquals("x.z", x.expand("x.z"));
    }
    @Test public void testNoExpansions() {
        x = new PrefixExpander(null);
        assertEquals("whatever.xml", x.expand("whatever.xml"));
        assertEquals("apple", x.expand("apple"));
        assertEquals("a.f", x.expand("a.f"));
    }
}
