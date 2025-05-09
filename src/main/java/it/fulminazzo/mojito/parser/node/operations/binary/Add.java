package it.fulminazzo.mojito.parser.node.operations.binary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * The operation associated with {@link it.fulminazzo.mojito.tokenizer.TokenType#ADD}.
 */
public class Add extends BinaryOperation {

    /**
     * Instantiates a new Add operation.
     *
     * @param left  the first operand
     * @param right the second operand
     */
    public Add(@NotNull Node left, @NotNull Node right) {
        super(left, right);
    }

}
