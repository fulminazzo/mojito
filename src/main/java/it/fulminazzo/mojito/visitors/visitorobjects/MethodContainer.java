package it.fulminazzo.mojito.visitors.visitorobjects;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Holds critical informations about a method.
 */
@Getter
public final class MethodContainer {
    private final @NotNull Class<?>[] parameterTypes;
    private final boolean varArgs;

    /**
     * Instantiates a new Method container.
     *
     * @param method the method to create from
     */
    public MethodContainer(final @NotNull Method method) {
        this.parameterTypes = method.getParameterTypes();
        this.varArgs = method.isVarArgs();
    }

}
