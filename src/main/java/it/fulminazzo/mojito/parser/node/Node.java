package it.fulminazzo.mojito.parser.node;

import it.fulminazzo.mojito.visitors.Visitor;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObject;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Represents a general node of the parser.
 */
public interface Node extends Serializable {

    /**
     * Allows the visitor to visit this node.
     * It does so by looking for a <code>visit%NodeName%</code> method.
     *
     * @param visitor the visitor
     * @param <T>     the type returned by the visitor
     * @return the node converted
     */
    <T extends VisitorObject<?, T, ?>> T accept(final @NotNull Visitor<?, T, ?> visitor);

    /**
     * Checks whether the current node is of the specified type.
     *
     * @param nodeType the node type
     * @return true if it is
     */
    boolean is(final @NotNull Class<? extends Node> nodeType);

}
