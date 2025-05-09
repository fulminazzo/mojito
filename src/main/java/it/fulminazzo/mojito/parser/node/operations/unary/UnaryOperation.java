package it.fulminazzo.mojito.parser.node.operations.unary;

import it.fulminazzo.mojito.parser.node.Node;
import it.fulminazzo.mojito.parser.node.operations.Operation;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an {@link Operation} with one operand.
 */
public abstract class UnaryOperation extends Operation {
    protected final @NotNull Node operand;

    /**
     * Instantiates a new Unary operation.
     *
     * @param operand the operand
     */
    public UnaryOperation(final @NotNull Node operand) {
        this.operand = operand;
    }

}
