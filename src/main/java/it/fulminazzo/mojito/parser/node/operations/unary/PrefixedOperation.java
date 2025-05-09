package it.fulminazzo.mojito.parser.node.operations.unary;

import it.fulminazzo.mojito.parser.node.Node;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A general type to represent a unary operation that can be suffixed or prefixed.
 */
@Getter
abstract class PrefixedOperation extends UnaryOperation {
    protected final boolean before;

    /**
     * Instantiates a new Prefixed operation.
     *
     * @param operand the operand
     * @param before  the before
     */
    public PrefixedOperation(final @NotNull Node operand,
                             final boolean before) {
        super(operand);
        this.before = before;
    }

}
