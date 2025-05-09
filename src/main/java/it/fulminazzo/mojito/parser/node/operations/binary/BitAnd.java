package it.fulminazzo.mojito.parser.node.operations.binary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * The operation associated with {@link it.fulminazzo.mojito.tokenizer.TokenType#BIT_AND}.
 */
public class BitAnd extends BinaryOperation {

    /**
     * Instantiates a new Bit and operation.
     *
     * @param left  the first operand
     * @param right the second operand
     */
    public BitAnd(@NotNull Node left, @NotNull Node right) {
        super(left, right);
    }

}
