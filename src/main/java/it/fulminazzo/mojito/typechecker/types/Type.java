package it.fulminazzo.mojito.typechecker.types;

import it.fulminazzo.mojito.tokenizer.TokenType;
import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType;
import it.fulminazzo.mojito.typechecker.types.variables.TypeFieldContainer;
import it.fulminazzo.mojito.visitors.visitorobjects.executables.ExecutableContainer;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Represents a general type parsed by the {@link it.fulminazzo.mojito.typechecker.TypeChecker}.
 */
public interface Type extends VisitorObject<ClassType, Type, ParameterTypes> {

    @Override
    default boolean isNull() {
        return is(Types.NULL_TYPE);
    }

    @Override
    default boolean isPrimitive() {
        return is(PrimitiveType.class);
    }

    /**
     * Checks whether the current type is a {@link ClassType}.
     *
     * @return true if it is
     */
    default boolean isClassType() {
        return is(ClassType.class);
    }

    /**
     * Checks that the current type class is {@link ClassType}.
     * Throws {@link TypeCheckerException} in case it is not.
     *
     * @return the current type cast to the expected one
     */
    default @NotNull ClassType checkClass() {
        return check(ClassType.class);
    }

    /**
     * Checks that the current type class is of the specified one.
     * Throws {@link TypeCheckerException} in case it is not.
     *
     * @param <T>       the class of the type
     * @param classType the class of the type
     * @return the current type cast to the expected one
     */
    default <T extends VisitorObject<ClassType, Type, ParameterTypes>> @NotNull T check(final @NotNull Class<T> classType) {
        if (is(classType)) return classType.cast(this);
        else throw TypeCheckerException.invalidType(classType, this);
    }

    /**
     * Checks whether the current type is equal to one of the expected ones.
     * Throws a {@link TypeCheckerException} in case no match is found.
     *
     * @param expectedTypes the expected types
     * @return this type
     */
    default @NotNull Type check(final Type @NotNull ... expectedTypes) {
        if (expectedTypes.length == 0)
            throw new IllegalArgumentException(String.format("Cannot compare type %s with no types", this));
        for (Type expectedType : expectedTypes)
            if (is(expectedType)) return this;
        throw TypeCheckerException.invalidType(expectedTypes[0], this);
    }

    /**
     * Checks whether the current type is NOT equal to one of the expected ones.
     * Throws a {@link TypeCheckerException} in case no match is found.
     *
     * @param expectedTypes the expected types
     * @return this type
     */
    default @NotNull Type checkNot(final Type @NotNull ... expectedTypes) {
        if (expectedTypes.length == 0)
            throw new IllegalArgumentException(String.format("Cannot compare type %s with no types", this));
        for (Type expectedType : expectedTypes)
            if (is(expectedType)) throw TypeCheckerException.invalidUnexpectedType(this);
        return this;
    }

    /**
     * Checks whether the current type extends the given {@link ClassType}.
     * If it does not, a {@link TypeCheckerException} is thrown.
     *
     * @param classType the class type
     * @return this type
     */
    default @NotNull Type checkAssignableFrom(final @NotNull ClassType classType) {
        if (!isAssignableFrom(classType)) throw TypeCheckerException.invalidType(classType, this);
        else return this;
    }

    @Override
    default @NotNull TypeFieldContainer getField(final @NotNull Field field) throws TypeException {
        ClassType classType = isClassType() ? (ClassType) this : toClass();
        if (!Modifier.isPublic(field.getModifiers())) throw TypeException.cannotAccessField(classType, field);
        else if (isClassType() && !Modifier.isStatic(field.getModifiers()))
            throw TypeException.cannotAccessStaticField(classType, field.getName());
        ClassType fieldClassType = ClassType.of(field.getType());
        return new TypeFieldContainer(this, fieldClassType, field.getName(), fieldClassType.toType());
    }

    @Override
    default @NotNull Type invokeMethod(final @NotNull ExecutableContainer<Method> method,
                                       final @NotNull ParameterTypes parameterTypes) throws TypeException {
        ClassType classType = isClassType() ? (ClassType) this : toClass();
        if (!Modifier.isPublic(method.getModifiers()))
            throw TypeException.cannotAccessMethod(classType, method.getActualExecutable());
        else if (isClassType() && !Modifier.isStatic(method.getModifiers()))
            throw TypeException.cannotAccessStaticMethod(classType, method.getName(), parameterTypes);
        return ClassType.of(method.getReturnType()).toType();
    }

    @Override
    default @NotNull Type toPrimitive() {
        throw TypeCheckerException.noPrimitive(this);
    }

    @Override
    default @NotNull Type toWrapper() {
        throw TypeCheckerException.noWrapper(this);
    }

    /**
     * Prints the given string to the format of a type.
     *
     * @param output the output
     * @return the new output
     */
    static @NotNull String print(final @NotNull String output) {
        return String.format("Type(%s)", output);
    }

    /*
        EXCEPTIONS
     */

    @Override
    default @NotNull TypeException fieldNotFound(final @NotNull ClassType classVisitorObject,
                                                 final @NotNull String field) {
        return TypeException.fieldNotFound(classVisitorObject, field);
    }

    @Override
    default @NotNull TypeException methodNotFound(final @NotNull ClassType classObject,
                                                  final @NotNull String method,
                                                  final @NotNull ParameterTypes parameters) {
        return TypeException.methodNotFound(classObject, method, parameters);
    }

    @Override
    default @NotNull TypeException typesMismatch(final @NotNull ClassType classObject,
                                                 final @NotNull Executable method,
                                                 final @NotNull ParameterTypes parameters) {
        return TypeException.typesMismatch(classObject, method, parameters);
    }

    @Override
    default @NotNull RuntimeException noClassType(final @NotNull Class<?> type) {
        return TypeCheckerException.noClassType(type);
    }

    /*
        OPERATIONS
     */

    @Override
    default @NotNull Type and(final @NotNull Type other) {
        return OperationUtils.executeBooleanComparison(TokenType.AND, this, other);
    }

    @Override
    default @NotNull Type or(final @NotNull Type other) {
        return OperationUtils.executeBooleanComparison(TokenType.OR, this, other);
    }

    @Override
    default @NotNull Type equal(final @NotNull Type other) {
        return OperationUtils.executeObjectComparison(TokenType.EQUAL, this, other);
    }

    @Override
    default @NotNull Type notEqual(final @NotNull Type other) {
        return OperationUtils.executeObjectComparison(TokenType.NOT_EQUAL, this, other);
    }

    @Override
    default @NotNull Type lessThan(final @NotNull Type other) {
        return OperationUtils.executeBinaryComparison(TokenType.LESS_THAN, this, other);
    }

    @Override
    default @NotNull Type lessThanEqual(final @NotNull Type other) {
        return OperationUtils.executeBinaryComparison(TokenType.LESS_THAN_EQUAL, this, other);
    }

    @Override
    default @NotNull Type greaterThan(final @NotNull Type other) {
        return OperationUtils.executeBinaryComparison(TokenType.GREATER_THAN, this, other);
    }

    @Override
    default @NotNull Type greaterThanEqual(final @NotNull Type other) {
        return OperationUtils.executeBinaryComparison(TokenType.GREATER_THAN_EQUAL, this, other);
    }

    @Override
    default @NotNull Type bitAnd(final @NotNull Type other) {
        return OperationUtils.executeBinaryBitOperation(TokenType.BIT_AND, this, other);
    }

    @Override
    default @NotNull Type bitOr(final @NotNull Type other) {
        return OperationUtils.executeBinaryBitOperation(TokenType.BIT_OR, this, other);
    }

    @Override
    default @NotNull Type bitXor(final @NotNull Type other) {
        return OperationUtils.executeBinaryBitOperation(TokenType.BIT_XOR, this, other);
    }

    @Override
    default @NotNull Type lshift(final @NotNull Type other) {
        return OperationUtils.executeBinaryOperation(TokenType.LSHIFT, this, other);
    }

    @Override
    default @NotNull Type rshift(final @NotNull Type other) {
        return OperationUtils.executeBinaryOperation(TokenType.RSHIFT, this, other);
    }

    @Override
    default @NotNull Type urshift(final @NotNull Type other) {
        return OperationUtils.executeBinaryOperation(TokenType.URSHIFT, this, other);
    }


    @Override
    default @NotNull Type add(final @NotNull Type other) {
        if (is(ObjectType.STRING) || other.is(ObjectType.STRING)) return ObjectType.STRING;
        else return OperationUtils.executeBinaryOperationDecimal(TokenType.ADD, this, other);
    }

    @Override
    default @NotNull Type subtract(final @NotNull Type other) {
        return OperationUtils.executeBinaryOperationDecimal(TokenType.SUBTRACT, this, other);
    }

    @Override
    default @NotNull Type multiply(final @NotNull Type other) {
        return OperationUtils.executeBinaryOperationDecimal(TokenType.MULTIPLY, this, other);
    }

    @Override
    default @NotNull Type divide(final @NotNull Type other) {
        return OperationUtils.executeBinaryOperationDecimal(TokenType.DIVIDE, this, other);
    }

    @Override
    default @NotNull Type modulo(final @NotNull Type other) {
        return OperationUtils.executeBinaryOperationDecimal(TokenType.MODULO, this, other);
    }

    @Override
    default @NotNull Type minus() {
        return OperationUtils.executeUnaryOperationDecimal(TokenType.SUBTRACT, this);
    }

    @Override
    default @NotNull Type not() {
        return OperationUtils.executeUnaryOperationBoolean(TokenType.NOT, this);
    }

}
