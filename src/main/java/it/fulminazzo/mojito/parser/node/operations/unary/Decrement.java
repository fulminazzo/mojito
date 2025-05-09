package it.fulminazzo.mojito.parser.node.operations.unary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the operation of increment (--).
 */
public class Decrement extends PrefixedOperation {

    /**
     * Instantiates a new Decrement operation.
     *
     * @param operand the operand
     * @param before  if the token is before
     */
    public Decrement(@NotNull Node operand, boolean before) {
        super(operand, before);
    }

}
