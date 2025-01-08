package it.fulminazzo.mojito.parser.node.values;

import it.fulminazzo.mojito.parser.node.NodeException;
import it.fulminazzo.mojito.tokenizer.TokenType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link TokenType#NUMBER_VALUE} literal.
 */
public class NumberValueLiteral extends ValueLiteral {

    /**
     * Instantiates a new Number literal.
     *
     * @param rawValue the raw value
     * @throws NodeException in case the value does not match with the {@link TokenType#regex()}
     */
    public NumberValueLiteral(final @NotNull String rawValue) throws NodeException {
        super(rawValue, TokenType.NUMBER_VALUE);
    }

}
