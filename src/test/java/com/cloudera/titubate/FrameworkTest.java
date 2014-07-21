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

import com.cloudera.titubate.Framework.Opts;
import java.io.File;
import java.util.Properties;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

public class FrameworkTest {
    private NodeKeeper nk;
    private Framework f;
    @Before public void setUp() {
        nk = createMock(NodeKeeper.class);
        f = new Framework(nk);
    }
    @Test public void testRun() throws Exception {
        Environment env = new Environment();
        State state = new State();

        Node n = createMock(Module.class);
        expect(nk.getNode("graph.xml")).andReturn(n);
        replay(nk);
        n.visit(env, state, new Properties());
        replay(n);
        f.run("graph.xml", env, state);
        verify(n);
    }
    @Test public void testRun_Fail() throws Exception {
        Environment env = new Environment();
        State state = new State();

        Node n = createMock(Module.class);
        expect(nk.getNode("graph.xml")).andReturn(n);
        replay(nk);
        n.visit(env, state, new Properties());
        expectLastCall().andThrow(new NodeException());
        replay(n);
        f.run("graph.xml", env, state);
        verify(n);
    }

    @Test public void testLoadEnvironment() throws Exception {
        Opts opts = new Opts();
        File f = new File(FrameworkTest.class.getResource("/env1.properties").toURI());
        opts.configDir = f.getParentFile().getAbsolutePath();
        opts.configFileName = "env1.properties";
        opts.testId = "mytest";
        Environment env = Framework.loadEnvironment(opts);
        assertEquals("bar", env.get("foo"));
        assertEquals("that", env.get("this"));
        assertEquals("mytest", env.get(Environment.KEY_TEST_ID));
    }
    @Test public void testLoadEnvironment_Minimal() throws Exception {
        Opts opts = new Opts();
        File f = new File(FrameworkTest.class.getResource("/env1.properties").toURI());
        opts.configDir = f.getParentFile().getAbsolutePath();
        opts.configFileName = "env1.properties";
        Environment env = Framework.loadEnvironment(opts);
        assertEquals("bar", env.get("foo"));
        assertEquals("that", env.get("this"));
        String testId = env.get(Environment.KEY_TEST_ID);
        try {
            UUID.fromString(testId);
        } catch (IllegalArgumentException e) {
            fail("Generated test ID is not a UUID: " + testId);
        }
    }

    @Test public void testParseArgs() {
        Opts opts = new Opts();
        String[] args = new String[] {
            "--config-dir", "/config/dir",
            "--config-file-name", "rw.conf",
            "--test-id", "mytest",
            "--graph", "graph.xml"
        };
        assertTrue(opts.parseArgs("theprogram", args));
        assertEquals("/config/dir", opts.configDir);
        assertEquals("rw.conf", opts.configFileName);
        assertEquals("mytest", opts.testId);
        assertEquals("graph.xml", opts.graph);
    }
    @Test public void testParseArgs_Bad() {
        Opts opts = new Opts();
        String[] args = new String[] {
            "--config-dir", "/config/dir",
            "--graph"
        };
        assertFalse(opts.parseArgs("theprogram", args));
    }
    @Test public void testParseArgs_Missing() {
        Opts opts = new Opts();
        String[] args = new String[] {
            "--config-file-name", "rw.conf",
            "--test-id", "mytest",
            "--graph", "graph.xml"
        };
        assertFalse(opts.parseArgs("theprogram", args));
    }
    @Test public void testParseArgs_Help() {
        Opts opts = new Opts();
        String[] args = new String[] { "--help" };
        assertFalse(opts.parseArgs("theprogram", args));
    }
}
