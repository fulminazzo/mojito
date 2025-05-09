package it.fulminazzo.mojito.parser.node.operations.unary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * The operation associated with {@link it.fulminazzo.mojito.tokenizer.TokenType#SUBTRACT}.
 */
public class Minus extends UnaryOperation {

    /**
     * Instantiates a new Minus operation.
     *
     * @param operand the operand
     */
    public Minus(@NotNull Node operand) {
        super(operand);
    }

}
