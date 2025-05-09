package it.fulminazzo.mojito.parser.node.operations.unary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the operation of increment (++).
 */
public class Increment extends PrefixedOperation {

    /**
     * Instantiates a new Increment operation.
     *
     * @param operand the operand
     * @param before  if the token is before
     */
    public Increment(@NotNull Node operand, boolean before) {
        super(operand, before);
    }

}
