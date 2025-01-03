package it.fulminazzo.javaparser.executor.values;

import it.fulminazzo.javaparser.executor.ExecutorException;
import it.fulminazzo.javaparser.wrappers.ObjectWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents the default conversion for {@link it.fulminazzo.javaparser.tokenizer.TokenType#LITERAL}.
 */
public class LiteralValue extends ObjectWrapper<String> implements Value<String> {

    /**
     * Instantiates a new Literal value.
     *
     * @param literal the literal
     */
    public LiteralValue(final @NotNull String literal) {
        super(Objects.requireNonNull(literal, "Expected literal to be not null"));
    }

    @Override
    public @NotNull ClassValue<String> toClassValue() {
        throw ExecutorException.noClassValue(getClass());
    }

    @Override
    public String getValue() {
        return this.object;
    }

}
