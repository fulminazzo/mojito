package it.fulminazzo.javaparser.parser.node.types;

import it.fulminazzo.javaparser.tokenizer.TokenType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link TokenType#NUMBER_VALUE} literal.
 */
public class NumberLiteral extends BaseTypeLiteral {

    /**
     * Instantiates a new Number literal.
     *
     * @param rawValue the raw value
     */
    public NumberLiteral(final @NotNull String rawValue) {
        super(rawValue, TokenType.NUMBER_VALUE);
    }

}
