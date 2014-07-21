package com.cloudera.titubate;

import com.cloudera.titubate.Module.AdjList;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.easymock.Capture;
import static org.easymock.EasyMock.*;

public class XmlModuleFactoryTest {
    private static File getFile(String resource) throws URISyntaxException {
        return new File(XmlModuleFactoryTest.class.getResource(resource).toURI());
    }

    private static File moduleDir;
    @BeforeClass public static void setUpClass() throws URISyntaxException {
        moduleDir =
            new File(XmlModuleFactoryTest.class.getResource("/").toURI());
    }

    private NodeFactory nf;
    private XmlModuleFactory xmf;
    @Before public void setUp() {
        nf = createMock(NodeFactory.class);
    }

    @Test public void testMinimum() throws Exception {
        File f = getFile("/minimum.xml");
        xmf = new XmlModuleFactory(f, moduleDir);
        xmf.setTestNodeFactory(nf);
        expect(nf.createNode("dummy.node0")).andReturn(createMock(Node.class));
        expect(nf.createNode("dummy.node1")).andReturn(createMock(Node.class));
        replay(nf);

        Module m = xmf.getModule();
        verify(nf);

        NodeKeeper nk = m.getNodeKeeper();
        assertTrue(nk.hasNode("dummy.node0"));
        assertTrue(nk.hasNode("dummy.node1"));

        Map<String, AdjList> adjMap = m.getAdjacencyMap();
        assertEquals(2, adjMap.size());
        AdjList l = adjMap.get("dummy.node0");
        assertEquals(1, l.size());
        assertEquals(1, l.weight("dummy.node1"));
        l = adjMap.get("dummy.node1");
        assertEquals(1, l.size());
        assertEquals(2, l.weight("END"));
    }

    @Test public void testPrefixes() throws Exception {
        File f = getFile("/prefixes.xml");
        xmf = new XmlModuleFactory(f, moduleDir);
        xmf.setTestNodeFactory(nf);
        expect(nf.createNode("dummy.node0")).andReturn(createMock(Node.class));
        expect(nf.createNode("a.Node1")).andReturn(createMock(Node.class));
        replay(nf);

        Module m = xmf.getModule();
        verify(nf);

        NodeKeeper nk = m.getNodeKeeper();
        assertTrue(nk.hasNode("dummy.node0"));
        assertTrue(nk.hasNode("a.Node1"));

        Map<String, AdjList> adjMap = m.getAdjacencyMap();
        assertEquals(2, adjMap.size());
        AdjList l = adjMap.get("dummy.node0");
        assertEquals(1, l.size());
        assertEquals(1, l.weight("a.Node1"));
        l = adjMap.get("a.Node1");
        assertEquals(1, l.size());
        assertEquals(2, l.weight("END"));
    }

    private Module readDummyNode0Module(String resource) throws Exception {
        File f = getFile(resource);
        xmf = new XmlModuleFactory(f, moduleDir);
        xmf.setTestNodeFactory(nf);
        expect(nf.createNode("dummy.node0")).andReturn(createMock(Node.class));
        replay(nf);

        Module m = xmf.getModule();
        verify(nf);

        Map<String, AdjList> adjMap = m.getAdjacencyMap();
        assertEquals(1, adjMap.size());
        // etc.

        return m;
    }

    @Test public void testFixture() throws Exception {
        Module m = readDummyNode0Module("/fixture.xml");
        assertTrue(m.getFixture() instanceof TestFixture);
    }
    @Test public void testInit() throws Exception {
        Module m = readDummyNode0Module("/init.xml");
        Properties initProps = m.getProps(Module.INIT_NODE_ID);
        assertEquals("100", initProps.getProperty(Module.PROPERTY_MAX_HOPS));
        assertEquals("200", initProps.getProperty(Module.PROPERTY_MAX_SECONDS));
        assertEquals("false", initProps.getProperty(Module.PROPERTY_TEARDOWN));
        assertEquals("dummy.node0", m.getInitNodeId());
    }
    @Test public void testNodeAttributesAndProperties() throws Exception {
        Module m = readDummyNode0Module("/nodeattrsandprops.xml");
        Properties nodeProps = m.getProps("dummy.node0");
        assertEquals("100", nodeProps.getProperty(Module.PROPERTY_MAX_HOPS));
        assertEquals("200", nodeProps.getProperty(Module.PROPERTY_MAX_SECONDS));
        assertEquals("false", nodeProps.getProperty(Module.PROPERTY_TEARDOWN));
        assertEquals("bar", nodeProps.getProperty("foo"));
        assertEquals("dong", nodeProps.getProperty("ding"));
    }
    @Test public void testAliases() throws Exception {
        File f = getFile("/alias.xml");
        xmf = new XmlModuleFactory(f, moduleDir);
        xmf.setTestNodeFactory(nf);
        AliasNode an = createMock(AliasNode.class);
        expect(nf.createNode("dummy.node0")).andReturn(createMock(Node.class));
        expect(nf.createNode("alias.aka")).andReturn(an);
        replay(nf);
        an.setTargetId("dummy.node0");
        replay(an);

        Module m = xmf.getModule();
        verify(nf);
        verify(an);

        Map<String, AdjList> adjMap = m.getAdjacencyMap();
        assertEquals(1, adjMap.size());
        // etc.
    }
    @Test public void testMultipleEdges() throws Exception {
        File f = getFile("/multipleedges.xml");
        xmf = new XmlModuleFactory(f, moduleDir);
        xmf.setTestNodeFactory(nf);
        expect(nf.createNode("dummy.node0")).andReturn(createMock(Node.class));
        expect(nf.createNode("dummy.node1a")).andReturn(createMock(Node.class));
        expect(nf.createNode("dummy.node1b")).andReturn(createMock(Node.class));
        replay(nf);

        Module m = xmf.getModule();
        verify(nf);

        Map<String, AdjList> adjMap = m.getAdjacencyMap();
        assertEquals(3, adjMap.size());
        AdjList l = adjMap.get("dummy.node0");
        assertEquals(2, l.size());
        assertEquals(1, l.weight("dummy.node1a"));
        assertEquals(2, l.weight("dummy.node1b"));
        l = adjMap.get("dummy.node1a");
        assertEquals(1, l.size());
        assertEquals(3, l.weight("END"));
        l = adjMap.get("dummy.node1b");
        assertEquals(1, l.size());
        assertEquals(4, l.weight("END"));
    }
    @Test public void testImplicitEND() throws Exception {
        File f = getFile("/implicitend.xml");
        xmf = new XmlModuleFactory(f, moduleDir);
        xmf.setTestNodeFactory(nf);
        expect(nf.createNode("dummy.node0")).andReturn(createMock(Node.class));
        expect(nf.createNode("dummy.node1")).andReturn(createMock(Node.class));
        replay(nf);

        Module m = xmf.getModule();
        verify(nf);

        Map<String, AdjList> adjMap = m.getAdjacencyMap();
        assertEquals(2, adjMap.size());
        AdjList l = adjMap.get("dummy.node0");
        assertEquals(1, l.size());
        assertEquals(1, l.weight("dummy.node1"));
        l = adjMap.get("dummy.node1");
        assertEquals(1, l.size());
        assertEquals(1, l.weight("END"));
    }
    // error conditions to test:
    // - repeated ID
    // - malformed alias
    // - malformed node property
    // - forbidden node properties
    // - edge with missing weight
}
