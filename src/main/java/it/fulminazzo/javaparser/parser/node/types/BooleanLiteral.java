package it.fulminazzo.javaparser.parser.node.types;

import it.fulminazzo.javaparser.tokenizer.TokenType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link TokenType#BOOLEAN_VALUE} literal.
 */
public class BooleanLiteral extends BaseTypeLiteral {

    /**
     * Instantiates a new Boolean literal.
     *
     * @param rawValue the raw value
     */
    public BooleanLiteral(final @NotNull String rawValue) {
        super(rawValue, TokenType.BOOLEAN_VALUE);
    }

}