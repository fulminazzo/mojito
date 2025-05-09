package it.fulminazzo.mojito.parser.node.operations.binary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * The operation associated with {@link it.fulminazzo.mojito.tokenizer.TokenType#NEW}.
 */
public class NewObject extends BinaryOperation {

    /**
     * Instantiates a new New object operation.
     *
     * @param left  the first operand
     * @param right the second operand
     */
    public NewObject(@NotNull Node left, @NotNull Node right) {
        super(left, right);
    }

}
