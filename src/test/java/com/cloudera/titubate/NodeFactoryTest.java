package com.cloudera.titubate;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.easymock.Capture;
import static org.easymock.EasyMock.*;

public class NodeFactoryTest {
    private NodeKeeper nk;
    private PrefixExpander px;
    private NodeFactory f;
    @Before public void setUp() {
        nk = createMock(NodeKeeper.class);
        px = createMock(PrefixExpander.class);
        f = new NodeFactory(nk, px);
    }

    @Test public void testCreateNewDummy() throws Exception {
        testCreateNewDummy("dummy.test");
    }
    @Test public void testCreateNewEnd() throws Exception {
        testCreateNewDummy("END");
    }
    private void testCreateNewDummy(String id) throws Exception {
        Capture<Node> cn = new Capture<Node>();
        expect(nk.hasNode(id)).andReturn(false);
        nk.addNode(eq(id), capture(cn));
        replay(nk);

        Node n = f.createNode(id, null);
        verify(nk);
        DummyNode dn = (DummyNode) cn.getValue();
        assertEquals(id, dn.toString());
        assertSame(dn, n);
    }
    @Test public void testCreateExistingDummy() throws Exception {
        testCreateExistingDummy("dummy.test");
    }
    @Test public void testCreateExistingEnd() throws Exception {
        testCreateExistingDummy("END");
    }
    private void testCreateExistingDummy(String id) throws Exception {
        Capture<Node> cn = new Capture<Node>();
        expect(nk.hasNode(id)).andReturn(true);
        DummyNode dn = new DummyNode(id);
        expect(nk.getNode(id)).andReturn(dn);
        replay(nk);

        Node n = f.createNode(id, null);
        verify(nk);
        assertSame(dn, n);
    }

    @Test public void testCreateNewAlias() throws Exception {
        String id = "alias.test";
        Capture<Node> cn = new Capture<Node>();
        expect(nk.hasNode(id)).andReturn(false);
        nk.addNode(eq(id), capture(cn));
        replay(nk);

        Node n = f.createNode(id, null);
        verify(nk);
        AliasNode an = (AliasNode) cn.getValue();
        assertEquals(id, an.toString());
        assertSame(an, n);
    }
    @Test public void testCreateExistingAlias() throws Exception {
        String id = "alias.test";
        Capture<Node> cn = new Capture<Node>();
        expect(nk.hasNode(id)).andReturn(true);
        AliasNode an = new AliasNode(id);
        expect(nk.getNode(id)).andReturn(an);
        replay(nk);

        Node n = f.createNode(id, null);
        verify(nk);
        assertSame(an, n);
    }

    @Test public void testNK_Source() throws Exception {
        String id = "foo";
        String source = "a.bar";
        expect(px.expand("a.bar")).andReturn("a.b.c.bar");
        replay(px);
        Node n = createMock(Node.class);
        expect(nk.getNode("a.b.c.bar")).andReturn(n);
        replay(nk);

        assertSame(n, f.createNode(id, source));
    }
    @Test public void testNK_NullSource() throws Exception {
        testNK_NoSource(null);
    }
    @Test public void testNK_EmptySource() throws Exception {
        testNK_NoSource("");
    }
    private void testNK_NoSource(String source) throws Exception {
        String id = "a.foo";
        expect(px.expand("a.foo")).andReturn("a.b.c.foo");
        replay(px);
        Node n = createMock(Node.class);
        expect(nk.getNode("a.b.c.foo")).andReturn(n);
        replay(nk);

        assertSame(n, f.createNode(id, source));
    }
}
