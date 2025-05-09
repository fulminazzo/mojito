package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * An implementation of {@link ExecutableContainer} for {@link Constructor}s.
 */
class ConstructorContainerImpl extends ExecutableContainerImpl<Constructor<?>> {

    /**
     * Instantiates a new Constructor container.
     *
     * @param constructor the constructor to create from
     */
    public ConstructorContainerImpl(final @NotNull Constructor<?> constructor) {
        super(constructor);
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        throw ExecutableException.noReturnType(getClass());
    }

}
