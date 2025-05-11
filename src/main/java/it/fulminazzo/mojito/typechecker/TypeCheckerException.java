package it.fulminazzo.mojito.typechecker;

import it.fulminazzo.mojito.exceptions.FormatRuntimeException;
import it.fulminazzo.mojito.tokenizer.TokenType;
import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown by the {@link it.fulminazzo.mojito.typechecker.TypeChecker}.
 */
public final class TypeCheckerException extends FormatRuntimeException {

    /**
     * Instantiates a new Type checker exception.
     *
     * @param message the message
     * @param args    the arguments to add in the message format
     */
    private TypeCheckerException(final @NotNull String message, final Object @NotNull ... args) {
        super(message, args);
    }

    /**
     * Instantiates a new Type checker exception.
     *
     * @param cause the inner exception
     */
    private TypeCheckerException(final @NotNull Throwable cause) {
        super(cause);
    }

    /**
     * Generates a {@link TypeCheckerException} with message the one from the given {@link Throwable}.
     *
     * @param cause the cause
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException of(final @NotNull Throwable cause) {
        return new TypeCheckerException(cause);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Invalid type received: expected %expected% but got %actual% instead</i>
     *
     * @param expected the expected type
     * @param actual   the actual type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException invalidType(final @NotNull Class<?> expected,
                                                            final @NotNull Object actual) {
        return new TypeCheckerException("Invalid type received: expected %s but got %s instead",
                expected.getSimpleName(), actual.getClass().getSimpleName());
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Invalid type received: expected %expected% but got %actual% instead</i>
     *
     * @param expected the expected type
     * @param actual   the actual type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException invalidType(final @NotNull Object expected,
                                                            final @NotNull Object actual) {
        return new TypeCheckerException("Invalid type received: expected %s but got %s instead",
                expected, actual);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Invalid type received: expected not %expected%</i>
     *
     * @param type the expected type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException invalidUnexpectedType(final @NotNull Type type) {
        return new TypeCheckerException("Invalid type received: expected not %s", type);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Inconvertible types: cannot cast '%objectType%' to '%classToCast%'</i>
     *
     * @param classToCast the expected type
     * @param objectType  the actual type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException invalidCast(final @NotNull ClassType classToCast,
                                                            final @NotNull Type objectType) {
        return new TypeCheckerException("Inconvertible types: cannot cast '%s' to '%s'",
                objectType, classToCast);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>%clazz% does not have a {@link ClassType}</i>
     *
     * @param type the type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException noClassType(final @NotNull Class<?> type) {
        return new TypeCheckerException("%s does not have a %s",
                type.getSimpleName(), ClassType.class.getSimpleName());
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Cannot resolve symbol '%symbol%'</i>
     *
     * @param symbol the symbol
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException cannotResolveSymbol(final @NotNull String symbol) {
        return new TypeCheckerException("Cannot resolve symbol '%s'", symbol);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Invalid array size '%size%' specified: natural number required</i>
     *
     * @param size the size
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException invalidArraySize(final int size) {
        return new TypeCheckerException("Invalid array size '%s' specified: natural number required", size);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Type %type% does not have any associated primitive type</i>
     *
     * @param type the type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException noPrimitive(final @NotNull Type type) {
        return new TypeCheckerException("Type %s does not have any associated primitive type", type);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Type %type% does not have any associated wrapper type</i>
     *
     * @param type the type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException noWrapper(final @NotNull Type type) {
        return new TypeCheckerException("Type %s does not have any associated wrapper type", type);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Operator '%operator%' cannot be applied to '%left%', '%right%'</i>
     *
     * @param operator the operator
     * @param left     the left operand
     * @param right    the right operand
     * @return the visitor exception
     */
    public static @NotNull TypeCheckerException unsupportedOperation(final @NotNull TokenType operator,
                                                                     final @NotNull Type left,
                                                                     final @NotNull Type right) {
        return new TypeCheckerException("Operator '%s' cannot be applied to '%s', '%s'",
                operator.regex().replace("\\", ""), left.toClass(), right.toClass());
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Operator '%operator%' cannot be applied to '%operand%'</i>
     *
     * @param operator the operator
     * @param operand  the operand
     * @return the visitor exception
     */
    public static @NotNull TypeCheckerException unsupportedOperation(final @NotNull TokenType operator,
                                                                     final @NotNull Type operand) {
        return new TypeCheckerException("Operator '%s' cannot be applied to '%s'",
                operator.regex().replace("\\", ""), operand.toClass());
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Unhandled exception: %type%</i>
     *
     * @param type the type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException unhandledException(final @NotNull ClassType type) {
        return new TypeCheckerException("Unhandled exception: %s", type);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Exception %type% has already been caught</i>
     *
     * @param type the type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException exceptionAlreadyCaught(final @NotNull ClassType type) {
        return new TypeCheckerException("Exception '%s' has already been caught", type);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Types in a multi-catch must be disjoint: '%subtype%' is a subclass of '%type%'</i>
     *
     * @param subtype the subtype
     * @param type    the type
     * @return the type checker exception
     */
    public static @NotNull TypeCheckerException exceptionsNotDisjoint(final @NotNull ClassType subtype,
                                                                      final @NotNull ClassType type) {
        return new TypeCheckerException("Types in a multi-catch must be disjoint: '%s' is a subclass of '%s'",
                subtype, type);
    }

    /**
     * Generates a {@link TypeCheckerException} with message:
     * <i>Invalid generic parameters passed for type %type%: it requires %expectedSize% types, but %actualSize% were given.</i>
     *
     * @param type    the type
     * @param expectedSize the expected size
     * @param actualSize   the actual size
     * @return type checker exception
     */
    public static @NotNull TypeCheckerException invalidGenericTypeSize(final @NotNull ClassType type,
                                                                       final int expectedSize,
                                                                       final int actualSize) {
        return new TypeCheckerException("Invalid generic parameters passed for type %s: it requires %s types, but %s were given.",
                type, expectedSize, actualSize);
    }

}
