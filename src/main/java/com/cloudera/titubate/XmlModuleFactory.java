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

import com.cloudera.titubate.Module.AdjList;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlModuleFactory {
    private static final Schema moduleSchema;

    static {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            moduleSchema = sf.newSchema(XmlModuleFactory.class.getClassLoader().getResource("module.xsd"));
        } catch (SAXException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final File xmlFile;
    private final DocumentBuilder db;

    public XmlModuleFactory(File xmlFile) throws ParserConfigurationException {
        this.xmlFile = xmlFile;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setSchema(moduleSchema);
        db = dbf.newDocumentBuilder();
    }

    public Module getModule(NodeKeeper nodeKeeper) throws Exception {
        Document d;
        try {
          d = db.parse(xmlFile);
        } catch (Exception e) {
          throw new Exception("Failed to parse: " + xmlFile, e);
        }

        // parse packages
        Map<String, String> prefixes = new java.util.HashMap<String, String>();  // <======
        NodeList nodelist = d.getDocumentElement().getElementsByTagName("package");
        for (int i = 0; i < nodelist.getLength(); i++) {
          Element el = (Element) nodelist.item(i);
          String value = el.getAttribute("value");
          if (!value.endsWith(".")) {
            value = value.concat(".");
          }
          prefixes.put(el.getAttribute("prefix"), value);
        }
        PrefixExpander prefixExpander = new PrefixExpander(prefixes);

        // parse fixture node
        Fixture fixture = null;  // <======
        nodelist = d.getDocumentElement().getElementsByTagName("fixture");
        if (nodelist.getLength() > 0) {
          Element fixtureEl = (Element) nodelist.item(0);
          fixture = (Fixture) Class.forName(prefixExpander.expand(fixtureEl.getAttribute("id"))).newInstance();
        }

        Map<String, AdjList> adjMap = new java.util.HashMap<String, AdjList>();  // <======
        Map<String, Properties> nodeProps = new java.util.HashMap<String, Properties>();  // <======
        Map<String, Set<String>> aliasMap = new java.util.HashMap<String, Set<String>>();  // <======

        // parse initial node
        Element initEl = (Element) d.getDocumentElement().getElementsByTagName("init").item(0);
        String initNodeId = initEl.getAttribute("id");  // <======
        Properties initProps = new Properties();
        String attr = initEl.getAttribute("maxHops");
        if (attr != null)
            initProps.setProperty("maxHops", attr);
        attr = initEl.getAttribute("maxSec");
        if (attr != null)
            initProps.setProperty("maxSec", attr);
        attr = initEl.getAttribute("teardown");
        if (attr != null)
            initProps.setProperty("teardown", attr);
        nodeProps.put(Module.INIT_NODE_ID, initProps);

        // parse all nodes
        nodelist = d.getDocumentElement().getElementsByTagName("node");
        for (int i = 0; i < nodelist.getLength(); i++) {
            Element nodeEl = (Element) nodelist.item(i);

            // get id and src attributes, and create node
            String id = nodeEl.getAttribute("id");
            if (adjMap.containsKey(id)) {
                // FIXME
                throw new Exception("Module already contains node with ID " + id);
            }
            String src = nodeEl.getAttribute("src");
            createNode(nodeKeeper, prefixExpander, id, src);

            // set some attributes in properties for later use
            Properties props = new Properties();
            props.setProperty("maxHops", nodeEl.getAttribute("maxHops"));
            props.setProperty("maxSec", nodeEl.getAttribute("maxSec"));
            props.setProperty("teardown", nodeEl.getAttribute("teardown"));

            // parse aliases
            NodeList aliaslist = nodeEl.getElementsByTagName("alias");
            Set<String> aliases = new TreeSet<String>();
            for (int j = 0; j < aliaslist.getLength(); j++) {
                Element propEl = (Element) aliaslist.item(j);
                if (!propEl.hasAttribute("name")) {
                    throw new Exception("Node " + id + " has alias with no identifying name");
                }
                String key = "alias." + propEl.getAttribute("name");
                aliases.add(key);
                AliasNode aliasNode = (AliasNode)
                    createNode(nodeKeeper, prefixExpander, key, null);
                aliasNode.setTargetId(id);
            }
            if (aliases.size() > 0) {
                aliasMap.put(id, aliases);
            }

            // parse properties of nodes
            NodeList proplist = nodeEl.getElementsByTagName("property");
            for (int j = 0; j < proplist.getLength(); j++) {
                Element propEl = (Element) proplist.item(j);
                if (!propEl.hasAttribute("key") || !propEl.hasAttribute("value")) {
                    throw new Exception("Node " + id + " has property with no key or value");
                }
                String key = propEl.getAttribute("key");
                if (key.equals("maxHops") || key.equals("maxSec") || key.equals("teardown")) {
                    throw new Exception("The following property can only be set in attributes: " + key);
                }
                props.setProperty(key, propEl.getAttribute("value"));
            }
            nodeProps.put(id, props);  // <======

            // parse edges of nodes
            AdjList edges = new AdjList();
            adjMap.put(id, edges);
            NodeList edgelist = nodeEl.getElementsByTagName("edge");
            if (edgelist.getLength() == 0) {
                throw new Exception("Node " + id + " has no edges");
            }
            for (int j = 0; j < edgelist.getLength(); j++) {
                Element edgeEl = (Element) edgelist.item(j);
                String edgeID = edgeEl.getAttribute("id");
                if (!edgeEl.hasAttribute("weight")) {
                    throw new Exception("Edge with ID " + edgeID + " is missing weight");
                }
                int weight = Integer.parseInt(edgeEl.getAttribute("weight"));
                edges.addEdge(edgeID, weight);
            }
        }  // parsing nodes

        return new Module(xmlFile.toString(), adjMap, nodeProps, aliasMap, prefixes, initNodeId,
                          fixture, nodeKeeper);
    }

    Node createNode(NodeKeeper nodeKeeper, PrefixExpander prefixExpander, String id, String src) throws Exception {
        if (id.equalsIgnoreCase("END") || id.startsWith("dummy")) {
            if (!nodeKeeper.hasNode(id)) {
                Node n = new DummyNode(id);
                nodeKeeper.addNode(id, n);
                return n;
            }
            return nodeKeeper.getNode(id);
        }

        if (id.startsWith("alias")) {
            if (!nodeKeeper.hasNode(id)) {
                Node n = new AliasNode(id);
                nodeKeeper.addNode(id, n);
                return n;
            }
            return nodeKeeper.getNode(id);
        }

        Node node;
        if (src == null || src.isEmpty()) {
            return nodeKeeper.getNode(prefixExpander.expand(id));
        } else {
            return nodeKeeper.getNode(prefixExpander.expand(src));
        }
    }
}
