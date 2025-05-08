package it.fulminazzo.mojito.visitors.visitorobjects;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Holds critical informations about a method.
 */
@Getter
public final class MethodContainer {
    private final @NotNull Method actualMethod;

    /**
     * Instantiates a new Method container.
     *
     * @param method the method to create from
     */
    public MethodContainer(final @NotNull Method method) {
        this.actualMethod = method;
    }

    /**
     * Gets the method modifiers.
     *
     * @return the modifiers
     */
    public int getModifiers() {
        return this.actualMethod.getModifiers();
    }

    /**
     * Gets the method return type.
     *
     * @return the return type
     */
    public @NotNull Class<?> getReturnType() {
        return this.actualMethod.getReturnType();
    }

    /**
     * Gets the method name.
     *
     * @return the name
     */
    public @NotNull String getName() {
        return this.actualMethod.getName();
    }

    /**
     * Get the method parameter types.
     *
     * @return an array containing the classes of the parameters
     */
    public @NotNull Class<?>[] getParameterTypes() {
        return this.actualMethod.getParameterTypes();
    }

    /**
     * Checks if the method accepts variable arguments.
     *
     * @return true if it does
     */
    public boolean isVarArgs() {
        return this.actualMethod.isVarArgs();
    }

}
