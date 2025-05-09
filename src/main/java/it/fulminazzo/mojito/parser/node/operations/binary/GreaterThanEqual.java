package it.fulminazzo.mojito.parser.node.operations.binary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * The operation associated with {@link it.fulminazzo.mojito.tokenizer.TokenType#GREATER_THAN_EQUAL}.
 */
public class GreaterThanEqual extends BinaryOperation {

    /**
     * Instantiates a new Greater than equal operation.
     *
     * @param left  the first operand
     * @param right the second operand
     */
    public GreaterThanEqual(@NotNull Node left, @NotNull Node right) {
        super(left, right);
    }

}
