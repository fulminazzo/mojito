package it.fulminazzo.mojito.executor.values.variables;

import it.fulminazzo.mojito.executor.values.ClassValue;
import it.fulminazzo.mojito.executor.values.ParameterValues;
import it.fulminazzo.mojito.executor.values.Value;
import it.fulminazzo.mojito.executor.values.ValueException;
import it.fulminazzo.mojito.visitors.visitorobjects.executables.ExecutableContainer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Support interface for classes of this package.
 */
@SuppressWarnings("unchecked")
interface ValueVariableContainer<V> extends Value<V> {

    @Override
    default V getValue() {
        return (V) getVariable().getValue();
    }

    @Override
    default boolean isCharacter() {
        return getVariable().isCharacter();
    }

    @Override
    default boolean isInteger() {
        return getVariable().isInteger();
    }

    @Override
    default boolean isLong() {
        return getVariable().isLong();
    }

    @Override
    default boolean isFloat() {
        return getVariable().isFloat();
    }

    @Override
    default boolean isDouble() {
        return getVariable().isDouble();
    }

    @Override
    default boolean isBoolean() {
        return getVariable().isBoolean();
    }

    @Override
    default boolean isString() {
        return getVariable().isString();
    }

    @Override
    default @NotNull ClassValue<V> checkClass() {
        return (ClassValue<V>) getVariable().checkClass();
    }

    @Override
    default @NotNull ValueFieldContainer<V> getField(@NotNull Field field) {
        return (ValueFieldContainer<V>) getVariable().getField(field);
    }

    @Override
    default @NotNull Value<?> invokeMethod(@NotNull ExecutableContainer<Method> method, @NotNull ParameterValues parameters) throws ValueException {
        return getVariable().invokeMethod(method, parameters);
    }
    
    @NotNull Value<?> getVariable();
    
}
