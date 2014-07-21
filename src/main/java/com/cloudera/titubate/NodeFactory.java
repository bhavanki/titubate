package com.cloudera.titubate;

import java.io.File;

/**
 * A factory for different sorts of {@link Node}s. This class is not
 * thread-safe.
 */
public class NodeFactory {
    private final PrefixExpander prefixExpander;
    private final File moduleDir;

    /**
     * Creates a new node factory.
     *
     * @param prefixExpander prefix expander
     * @param moduleDir directory where XML module definitions reside
     */
    public NodeFactory(PrefixExpander prefixExpander, File moduleDir) {
        this.prefixExpander = prefixExpander;
        this.moduleDir = moduleDir;
    }
    /**
     * Creates a node.<p>
     *
     * <ul>
     * <li>If the node ID is "END" or starts with "dummy", a {@link DummyNode}
     *     is made.</li>
     * <li>If the node ID starts with "alias", an {@link AliasNode} is
     *     made.</li>
     * <li>If the node ID ends with ".xml", a {@link Module} is made by reading
     *     an XML module definition from the file with the same name.</li>
     * <li>The ID's expansion is used as the name of a class to instantiate. The
     *     class must be a {@link CallableAction} implementation, and a
     *     {@link CallableNode} for it is made.</li>
     * </ul>
     *
     * @param id node ID
     * @return new node
     * @throws NodeCreationException if a node could not be created
     */
    public Node createNode(final String id) throws NodeCreationException {
        if (id.equalsIgnoreCase("END") || id.startsWith("dummy")) {
            return new DummyNode(id);
        }

        if (id.startsWith("alias")) {
            return new AliasNode(id);
        }

        if (id.endsWith(".xml")) {
            try {
                return new XmlModuleFactory(new File(moduleDir, id), moduleDir)
                    .getModule();
            } catch (Exception e) {  // FIXME
                throw new NodeCreationException("Failed to load module " + id, e);
            }
        }

        String className = id;
        if (prefixExpander != null) {
            className = prefixExpander.expand(className);
        }

        Object idObject;
        try {
            idObject = Class.forName(className).newInstance();
        } catch (Exception e) {  // FIXME
            throw new NodeCreationException("Failed to create node object of class " +
                                            className, e);
        }
        if (idObject instanceof CallableAction) {
            return new CallableNode((CallableAction) idObject);
        } else {
            throw new NodeCreationException("Unsupported node object type " + className);
        }
    }
}
