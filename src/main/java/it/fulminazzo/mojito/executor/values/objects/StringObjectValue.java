package it.fulminazzo.mojito.executor.values.objects;

import it.fulminazzo.mojito.executor.values.ClassValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link String} {@link ObjectValue}, with support for special operations.
 */
class StringObjectValue extends ObjectValue<String> {

    /**
     * Instantiates a new String object value.
     *
     * @param string the string
     */
    public StringObjectValue(final @NotNull String string) {
        super(string);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public @NotNull ClassValue<String> toClass() {
        return ObjectClassValue.STRING;
    }

}
