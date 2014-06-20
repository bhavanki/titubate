package com.cloudera.titubate;

/**
 * A factory for different sorts of {@link Node}s. This class is not
 * thread-safe.
 */
public class NodeFactory {
    private final NodeKeeper nodeKeeper;
    private final PrefixExpander prefixExpander;
    /**
     * Creates a new node factory.
     *
     * @param nodeKeeper node keeper to get previously made nodes from and to
     * save new nodes to
     * @param prefixExpander prefix expander
     */
    public NodeFactory(NodeKeeper nodeKeeper, PrefixExpander prefixExpander) {
        this.nodeKeeper = nodeKeeper;
        this.prefixExpander = prefixExpander;
    }
    /**
     * Creates a node.<p>
     *
     * <ul>
     * <li>If the node ID is "END" or starts with "dummy", a {@link DummyNode}
     *     is made.</li>
     * <li>If the node ID starts with "alias", an {@link AliasNode} is
     *     made.</li>
     * <li>If a source is provided, its expansion is passed to the node keeper
     *     for node retrieval / creation; otherwise, the expansion of the ID
     *     is passed.</li>
     * </ul>
     *
     * @param id node ID
     * @param src node source
     * @return new node, or previously made node if available
     */
    public Node createNode(String id, String src) throws Exception {
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

        if (src == null || src.isEmpty()) {
            return nodeKeeper.getNode(prefixExpander.expand(id));
        } else {
            return nodeKeeper.getNode(prefixExpander.expand(src));
        }
    }
}
