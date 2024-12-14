package it.fulminazzo.javaparser.parser.node;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the parameters invoked during a method invocation.
 */
public class MethodInvocation extends Node {
    private final List<Node> parameters;

    /**
     * Instantiates a new Method invocation.
     *
     * @param parameters the parameters
     */
    public MethodInvocation(final @NotNull List<Node> parameters) {
        this.parameters = parameters;
    }

}
