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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

public class XmlTrailReporter implements TrailReporter {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String NODE_END = "  </node>";
    private static final String EDGE_BEGIN = "    <edge id=\"";
    private static final String EDGE_END = "\" weight=\"1\" />";

    private final Module module;

    public XmlTrailReporter(Module module) {
        this.module = module;
    }

    public void report(Iterable<TrailElement> trail, OutputStream out)
        throws IOException {
        OutputStreamWriter osw = null;
        PrintWriter pw = null;
        try {
            osw = new OutputStreamWriter(out, UTF8);
            pw = new PrintWriter(osw);

            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<module>");

            Fixture fixture = module.getFixture();
            if (fixture != null) {
                pw.println("  <fixture id=\"" + fixture.getClass().getName() +
                           "\" />");
            }

            int currNodeNum = 0;
            TrailElement prev = null;
            for (TrailElement curr : trail) {
                String currId = "n" + currNodeNum++;
                String currSrc = curr.getNodeId();
                if (prev == null) {
                    pw.println("  <init id=\"" + currId + "\" />");
                } else {
                    pw.println(EDGE_BEGIN + currId + EDGE_END);
                    pw.println(NODE_END);
                }
                pw.println("  <node id=\"" + currId + "\" src=\"" + currSrc +
                           "\">");
                Properties props = module.getProps(currSrc);
                for (Map.Entry<Object,Object> e : props.entrySet()) {
                    pw.println("    <property key=\"" + (String) e.getKey() +
                               "\" value=\"" + (String) e.getValue() + "\" />");
                }
                prev = curr;
            }
            pw.println(EDGE_BEGIN + "END" + EDGE_END);
            pw.println(NODE_END);

            pw.println("</module>");
            if (pw.checkError()) {
                throw new IOException("Error writing to output stream " +
                                      "(sorry, no more information available)");
            }
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (osw != null) {
                osw.close();
            }
        }
    }
}
