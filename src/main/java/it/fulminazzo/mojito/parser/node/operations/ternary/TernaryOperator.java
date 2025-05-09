package it.fulminazzo.mojito.parser.node.operations.ternary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the Java ternary operator operation.
 */
public class TernaryOperator extends TernaryOperation {

    /**
     * Instantiates a new Ternary operator.
     *
     * @param expression   the expression
     * @param firstResult  the first result
     * @param secondResult the second result
     */
    public TernaryOperator(@NotNull Node expression, @NotNull Node firstResult, @NotNull Node secondResult) {
        super(expression, firstResult, secondResult);
    }

}
