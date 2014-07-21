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

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.easymock.Capture;
import static org.easymock.EasyMock.*;

public class XmlTrailReporterTest {
    private ByteArrayOutputStream baos;
    private Module module;
    private XmlTrailReporter xtr;
    @Before public void setUp() {
        baos = new ByteArrayOutputStream();
        module = createMock(Module.class);
        xtr = new XmlTrailReporter(module);
    }

    private List<TrailElement> makeTrail() {
        List<TrailElement> l = new java.util.ArrayList<TrailElement>();
        State s1 = new State();
        s1.set("number", 1);
        l.add(new TrailElement("n1", s1));
        State s2 = new State();
        s2.set("number", 2);
        l.add(new TrailElement("n2", s2));
        State s3 = new State();
        s3.set("number", 3);
        l.add(new TrailElement("n3", s3));
        return l;
    }
    private static final String NL = System.getProperty("line.separator");
    private static final String MODULE_START =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL +
        "<module>" + NL;
    private static final String MODULE_ZERO =
        "  <init id=\"n0\" />" + NL +
        "  <node id=\"n0\" src=\"n1\">" + NL;
    private static final String MODULE_ONE =
        "    <edge id=\"n1\" weight=\"1\" />" + NL +
        "  </node>" + NL +
        "  <node id=\"n1\" src=\"n2\">" + NL;
    private static final String MODULE_TWO =
        "    <edge id=\"n2\" weight=\"1\" />" + NL +
        "  </node>" + NL +
        "  <node id=\"n2\" src=\"n3\">" + NL;
    private static final String MODULE_THREE =
        "    <edge id=\"END\" weight=\"1\" />" + NL +
        "  </node>" + NL +
        "</module>" + NL;
    private static final String MODULE_END =
        MODULE_ZERO + MODULE_ONE + MODULE_TWO + MODULE_THREE;
    private static final Properties EMPTY_PROPERTIES = new Properties();
    @Test public void testReport() throws Exception {
        expect(module.getFixture()).andReturn(null);
        expect(module.getProps("n1")).andReturn(EMPTY_PROPERTIES);
        expect(module.getProps("n2")).andReturn(EMPTY_PROPERTIES);
        expect(module.getProps("n3")).andReturn(EMPTY_PROPERTIES);
        replay(module);
        xtr.report(makeTrail(), baos);
        String xml = baos.toString("UTF-8");
        assertEquals(MODULE_START + MODULE_END, xml);
    }
    @Test public void testReportWithFixture() throws Exception {
        Fixture f = new Fixture() {
            public void setUp(Environment env, State state) {}
            public void tearDown(Environment env, State state) {}
        };
        expect(module.getFixture()).andReturn(f);
        expect(module.getProps("n1")).andReturn(EMPTY_PROPERTIES);
        expect(module.getProps("n2")).andReturn(EMPTY_PROPERTIES);
        expect(module.getProps("n3")).andReturn(EMPTY_PROPERTIES);
        replay(module);
        xtr.report(makeTrail(), baos);
        String xml = baos.toString("UTF-8");
        assertEquals(
            MODULE_START +
            "  <fixture id=\"" + f.getClass().getName() + "\" />" + NL +
            MODULE_END, xml);
    }
    @Test public void testReportWithNodeProperties() throws Exception {
        expect(module.getFixture()).andReturn(null);
        Properties p1 = new Properties();
        p1.setProperty("node", "1");
        expect(module.getProps("n1")).andReturn(p1);
        Properties p2 = new Properties();
        p2.setProperty("node", "2");
        expect(module.getProps("n2")).andReturn(p2);
        Properties p3 = new Properties();
        p3.setProperty("node", "3");
        expect(module.getProps("n3")).andReturn(p3);
        replay(module);
        xtr.report(makeTrail(), baos);
        String xml = baos.toString("UTF-8");
        assertEquals(
            MODULE_START +
            MODULE_ZERO +
            "    <property key=\"node\" value=\"1\" />" + NL +
            MODULE_ONE +
            "    <property key=\"node\" value=\"2\" />" + NL +
            MODULE_TWO +
            "    <property key=\"node\" value=\"3\" />" + NL +
            MODULE_THREE, xml);
    }
}
