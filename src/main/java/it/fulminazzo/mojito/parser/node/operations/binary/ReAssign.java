package it.fulminazzo.mojito.parser.node.operations.binary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * The operation associated with {@link it.fulminazzo.mojito.tokenizer.TokenType#ASSIGN}.
 */
public class ReAssign extends BinaryOperation {

    /**
     * Instantiates a new Re assign operation.
     *
     * @param left  the first operand
     * @param right the second operand
     */
    public ReAssign(final @NotNull Node left, final @NotNull Node right) {
        super(left, right);
    }

}
