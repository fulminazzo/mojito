package it.fulminazzo.mojito.parser.node.operations.binary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * The operation associated with {@link it.fulminazzo.mojito.tokenizer.TokenType#LESS_THAN}.
 */
public class LessThan extends BinaryOperation {

    /**
     * Instantiates a new Less than operation.
     *
     * @param left  the first operand
     * @param right the second operand
     */
    public LessThan(@NotNull Node left, @NotNull Node right) {
        super(left, right);
    }

}
