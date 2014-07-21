package com.cloudera.titubate;

import com.cloudera.titubate.Module.AdjList;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.easymock.Capture;
import static org.easymock.EasyMock.*;

public class XmlModuleFactoryTest {
    private static File getFile(String resource) throws URISyntaxException {
        return new File(XmlModuleFactoryTest.class.getResource(resource).toURI());
    }
    private NodeKeeper nk;
    private XmlModuleFactory xmf;
    @Before public void setUp() throws Exception {
        nk = createMock(NodeKeeper.class);
    }

    @Test public void testMinimum() throws Exception {
        File f = getFile("/minimum.xml");
        xmf = new XmlModuleFactory(f);

        Capture<Node> cn0 = new Capture<Node>();
        expect(nk.hasNode("dummy.node0")).andReturn(false);
        nk.addNode(eq("dummy.node0"), capture(cn0));
        Capture<Node> cn1 = new Capture<Node>();
        expect(nk.hasNode("dummy.node1")).andReturn(false);
        nk.addNode(eq("dummy.node1"), capture(cn1));
        replay(nk);
        Module m = xmf.getModule(nk);
        verify(nk);

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
        xmf = new XmlModuleFactory(f);

        Capture<Node> cn0 = new Capture<Node>();
        expect(nk.hasNode("dummy.node0")).andReturn(false);
        nk.addNode(eq("dummy.node0"), capture(cn0));
        Node n1 = createMock(Node.class);
        expect(nk.getNode("a.b.c.Node1")).andReturn(n1);
        replay(nk);
        Module m = xmf.getModule(nk);
        verify(nk);

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
        xmf = new XmlModuleFactory(f);

        Capture<Node> cn0 = new Capture<Node>();
        expect(nk.hasNode("dummy.node0")).andReturn(false);
        nk.addNode(eq("dummy.node0"), capture(cn0));
        replay(nk);
        Module m = xmf.getModule(nk);
        verify(nk);
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
        xmf = new XmlModuleFactory(f);

        Capture<Node> cn0 = new Capture<Node>();
        expect(nk.hasNode("dummy.node0")).andReturn(false);
        nk.addNode(eq("dummy.node0"), capture(cn0));
        Capture<Node> cn1 = new Capture<Node>();
        expect(nk.hasNode("alias.aka")).andReturn(false);
        nk.addNode(eq("alias.aka"), capture(cn1));
        replay(nk);
        Module m = xmf.getModule(nk);
        verify(nk);
        Map<String, AdjList> adjMap = m.getAdjacencyMap();
        assertEquals(1, adjMap.size());
        // etc.

        Node n1 = cn1.getValue();
        assertTrue(n1 instanceof AliasNode);
        assertEquals("dummy.node0", ((AliasNode) n1).getTargetId());
    }
    @Test public void testMultipleEdges() throws Exception {
        File f = getFile("/multipleedges.xml");
        xmf = new XmlModuleFactory(f);

        Capture<Node> cn0 = new Capture<Node>();
        expect(nk.hasNode("dummy.node0")).andReturn(false);
        nk.addNode(eq("dummy.node0"), capture(cn0));
        Capture<Node> cn1a = new Capture<Node>();
        expect(nk.hasNode("dummy.node1a")).andReturn(false);
        nk.addNode(eq("dummy.node1a"), capture(cn0));
        Capture<Node> cn1b = new Capture<Node>();
        expect(nk.hasNode("dummy.node1b")).andReturn(false);
        nk.addNode(eq("dummy.node1b"), capture(cn0));
        replay(nk);
        Module m = xmf.getModule(nk);
        verify(nk);

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
        xmf = new XmlModuleFactory(f);

        Capture<Node> cn0 = new Capture<Node>();
        expect(nk.hasNode("dummy.node0")).andReturn(false);
        nk.addNode(eq("dummy.node0"), capture(cn0));
        Capture<Node> cn1 = new Capture<Node>();
        expect(nk.hasNode("dummy.node1")).andReturn(false);
        nk.addNode(eq("dummy.node1"), capture(cn1));
        replay(nk);
        Module m = xmf.getModule(nk);
        verify(nk);

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
