package it.fulminazzo.mojito.parser.node.operators.ternary;

import it.fulminazzo.mojito.parser.node.Node;
import it.fulminazzo.mojito.parser.node.operators.Operation;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an {@link Operation} with three operands.
 */
public abstract class TernaryOperation extends Operation {
    protected final @NotNull Node first;
    protected final @NotNull Node second;
    protected final @NotNull Node third;

    /**
     * Instantiates a new Ternary operation.
     *
     * @param first  the first operand
     * @param second the second operand
     * @param third  the third operand
     */
    public TernaryOperation(final @NotNull Node first, final @NotNull Node second, final @NotNull Node third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

}
