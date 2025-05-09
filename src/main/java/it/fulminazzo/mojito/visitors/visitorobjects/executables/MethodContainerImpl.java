package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * An implementation of {@link ExecutableContainer} for {@link Method}s.
 */
@Getter
public
class MethodContainerImpl implements ExecutableContainer {
    private final @NotNull Method actualMethod;

    /**
     * Instantiates a new Method container.
     *
     * @param method the method to create from
     */
    public MethodContainerImpl(final @NotNull Method method) {
        this.actualMethod = method;
    }

    @Override
    public int getModifiers() {
        return this.actualMethod.getModifiers();
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return this.actualMethod.getReturnType();
    }

    @Override
    public @NotNull String getName() {
        return this.actualMethod.getName();
    }

    @Override
    public @NotNull Class<?>[] getParameterTypes() {
        return this.actualMethod.getParameterTypes();
    }

    @Override
    public boolean isVarArgs() {
        return this.actualMethod.isVarArgs();
    }

}
