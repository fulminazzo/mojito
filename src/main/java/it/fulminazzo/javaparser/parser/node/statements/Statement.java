package it.fulminazzo.javaparser.parser.node.statements;

import it.fulminazzo.javaparser.parser.node.Node;
import it.fulminazzo.javaparser.parser.node.types.Literal;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a general statement.
 */
public class Statement extends Node {
    private final @NotNull Node expr;

    public Statement() {
        this(new Literal(""));
    }

    public Statement(final @NotNull Node expr) {
        this.expr = expr;
    }

}
