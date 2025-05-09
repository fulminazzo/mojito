package it.fulminazzo.mojito.parser.node.operations.binary;

import it.fulminazzo.mojito.parser.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the cast made to an expression.
 */
public class Cast extends BinaryOperation {

    /**
     * Instantiates a new Cast operation.
     *
     * @param type       the type to cast to
     * @param expression the expression to cast
     */
    public Cast(@NotNull Node type, @NotNull Node expression) {
        super(type, expression);
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public @NotNull Node getType() {
        return this.left;
    }

    /**
     * Gets expression.
     *
     * @return the expression
     */
    public @NotNull Node getExpression() {
        return this.right;
    }

}
