package com.cloudera.titubate;

import java.io.File;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.easymock.Capture;
import static org.easymock.EasyMock.*;

public class NodeFactoryTest {
    private static File moduleDir;
    @BeforeClass public static void setUpClass() throws Exception {
        moduleDir = new File(NodeFactoryTest.class.getResource("/").toURI());
    }

    private PrefixExpander px;
    private NodeFactory f;
    @Before public void setUp() {
        px = createMock(PrefixExpander.class);
        f = new NodeFactory(px, moduleDir);
    }

    @Test public void testCreateDummy() throws Exception {
        testCreateDummy("dummy.test");
    }
    @Test public void testCreateEnd() throws Exception {
        testCreateDummy("END");
    }
    private void testCreateDummy(String id) throws Exception {
        Node n = f.createNode(id);
        DummyNode dn = (DummyNode) n;
        assertEquals(id, dn.toString());
    }

    @Test public void testCreateAlias() throws Exception {
        String id = "alias.test";
        Node n = f.createNode(id);
        AliasNode an = (AliasNode) n;
        assertEquals(id, an.toString());
    }

    // TBD - XML

    public static class NodeFactoryTestAction extends CallableAction {
        @Override
        public Void call() {
            return null;
        }
    }

    @Test public void testClass() throws Exception {
        String id = "a.foo";
        String className = NodeFactoryTestAction.class.getName();
        expect(px.expand(id)).andReturn(className);
        replay(px);

        Node n = f.createNode(id);
        assertTrue(n instanceof CallableNode);
        assertEquals(className, n.toString());
    }
    @Test public void testClass_NoExpansion() {
        String className = NodeFactoryTestAction.class.getName();

        f = new NodeFactory(null, moduleDir);
        Node n = f.createNode(className);
        assertTrue(n instanceof CallableNode);
        assertEquals(className, n.toString());
    }
    @Test(expected=NodeCreationException.class)
    public void testClass_Unsupported() {
        String className = String.class.getName();
        f.createNode(className);
    }
}
