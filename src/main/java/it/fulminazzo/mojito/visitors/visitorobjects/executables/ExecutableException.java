package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import it.fulminazzo.mojito.exceptions.FormatRuntimeException;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown by {@link ExecutableContainer} implementations.
 */
final class ExecutableException extends FormatRuntimeException {

    /**
     * Instantiates a new Executable exception.
     *
     * @param message the message
     * @param args    the arguments to add in the message format
     */
    ExecutableException(final @NotNull String message, final Object @NotNull ... args) {
        super(message, args);
    }

    /**
     * Generates a {@link ExecutableException} with message:
     * <i>%clazz% does not contain a return type</i>
     *
     * @param clazz the clazz
     * @return the executable exception
     */
    public static @NotNull ExecutableException noReturnType(final @NotNull Class<?> clazz) {
        return new ExecutableException("%s does not contain a return type", clazz.getSimpleName());
    }

}
