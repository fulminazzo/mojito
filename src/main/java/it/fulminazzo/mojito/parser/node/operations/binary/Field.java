package it.fulminazzo.mojito.parser.node.operations.binary;

import it.fulminazzo.mojito.parser.node.Node;
import it.fulminazzo.mojito.parser.node.literals.Literal;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the retrieval of a field:
 * <code>%object%.%field%</code>
 */
public class Field extends BinaryOperation implements Literal {

    /**
     * Instantiates a new Field operation.
     *
     * @param object    the object
     * @param fieldName the field name
     */
    public Field(@NotNull Node object, @NotNull Literal fieldName) {
        super(object, fieldName);
    }

    @Override
    public @NotNull String getLiteral() {
        return String.format("%s.%s", this.left, ((Literal) this.right).getLiteral());
    }

}
