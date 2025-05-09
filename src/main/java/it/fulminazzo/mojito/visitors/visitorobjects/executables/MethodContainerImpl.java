package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * An implementation of {@link ExecutableContainer} for {@link Method}s.
 */
class MethodContainerImpl extends ExecutableContainerImpl implements ExecutableContainer {

    /**
     * Instantiates a new Method container.
     *
     * @param method the method to create from
     */
    public MethodContainerImpl(final @NotNull Method method) {
        super(method);
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return this.actualMethod.getReturnType();
    }

}
