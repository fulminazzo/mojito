package it.fulminazzo.javaparser.typechecker;

import it.fulminazzo.javaparser.typechecker.types.ClassType;
import it.fulminazzo.javaparser.typechecker.types.LiteralType;
import it.fulminazzo.javaparser.typechecker.types.Type;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown by the {@link it.fulminazzo.javaparser.typechecker.TypeChecker}.
 */
public class TypeCheckerException extends RuntimeException {

    /**
     * Instantiates a new Type checker exception.
     *
     * @param message the message
     * @param args    the arguments to add in the message format
     */
    private TypeCheckerException(final @NotNull String message, final Object @NotNull ... args) {
        super(String.format(message, args));
    }

    /**
     * Generates a {@link TypeCheckerException} with message the one from the given {@link Throwable}.
     *
     * @param cause the cause
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException of(final @NotNull Throwable cause) {
        return new TypeCheckerException(cause.getMessage());
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Invalid type received: expected %expected% but got %actual% instead.</i>
     *
     * @param expected the expected type
     * @param actual   the actual type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException invalidType(final @NotNull Type expected,
                                                            final @NotNull Type actual) {
        return new TypeCheckerException("Invalid type received: expected %s but got %s instead.",
                expected, actual);
    }

    /**
     * Generates a new exception with an invalid type related message.
     *
     * @param expected the expected type
     * @param actual   the actual type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException invalidType(final @NotNull Class<? extends Type> expected,
                                                            final @NotNull Type actual) {
        return new TypeCheckerException("Invalid type received: expected %s but got %s instead.",
                expected.getSimpleName(), actual.getClass().getSimpleName());
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>{@link LiteralType} does not have a {@link ClassType}</i>
     *
     * @return the type exception
     */
    public static @NotNull TypeCheckerException literalConversion() {
        return new TypeCheckerException("%s does not have a %s",
                LiteralType.class.getSimpleName(), ClassType.class.getSimpleName());
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Cannot resolve symbol '%symbol%'</i>
     *
     * @param symbol the symbol
     * @return the type exception
     */
    public static @NotNull TypeCheckerException cannotResolveSymbol(final @NotNull String symbol) {
        return new TypeCheckerException("Cannot resolve symbol '%s'", symbol);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Invalid array size '%size%' specified: natural number required</i>
     *
     * @param size the size
     * @return the type exception
     */
    public static @NotNull TypeCheckerException invalidArraySize(final int size) {
        return new TypeCheckerException("Invalid array size '%s' specified: natural number required", size);
    }

}
