package it.fulminazzo.mojito.typechecker.types.variables;

import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.ParameterTypes;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import it.fulminazzo.mojito.visitors.visitorobjects.IncorrectMethodException;
import it.fulminazzo.mojito.visitors.visitorobjects.executables.ExecutableContainer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Support interface for classes of this package.
 */
interface TypeVariableContainer extends Type {

    @Override
    default boolean isClassType() {
        return getVariable().isClassType();
    }

    @Override
    default @NotNull Type check(Type @NotNull ... expectedTypes) {
        return getVariable().check(expectedTypes);
    }

    @Override
    default @NotNull Type checkNot(Type @NotNull ... expectedTypes) {
        return getVariable().checkNot(expectedTypes);
    }

    @Override
    default @NotNull Type checkAssignableFrom(@NotNull ClassType classType) {
        return getVariable().checkAssignableFrom(classType);
    }

    @Override
    default @NotNull TypeFieldContainer getField(@NotNull Field field) throws TypeException {
        return getVariable().getField(field);
    }

    @Override
    default @NotNull Type invokeMethod(@NotNull ExecutableContainer<Method> method, @NotNull ParameterTypes parameterTypes) throws TypeException, IncorrectMethodException {
        return getVariable().invokeMethod(method, parameterTypes);
    }

    /**
     * Gets the variable.
     *
     * @return the variable
     */
    @NotNull Type getVariable();

}
