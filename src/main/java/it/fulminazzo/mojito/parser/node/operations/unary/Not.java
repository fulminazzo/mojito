package it.fulminazzo.mojito.parser.node.operations.unary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * The operation associated with {@link it.fulminazzo.mojito.tokenizer.TokenType#NOT}.
 */
public class Not extends UnaryOperation {

    /**
     * Instantiates a new Not operation.
     *
     * @param operand the operand
     */
    public Not(@NotNull Node operand) {
        super(operand);
    }

}
