package it.fulminazzo.mojito.parser.node.operations.binary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * The operation associated with {@link it.fulminazzo.mojito.tokenizer.TokenType#RSHIFT}.
 */
public class RShift extends BinaryOperation {

    /**
     * Instantiates a new RShift operation.
     *
     * @param left  the first operand
     * @param right the second operand
     */
    public RShift(@NotNull Node left, @NotNull Node right) {
        super(left, right);
    }

}
