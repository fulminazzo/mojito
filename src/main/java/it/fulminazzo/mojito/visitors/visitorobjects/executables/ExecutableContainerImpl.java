package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * A general implementation of {@link ExecutableContainer} for {@link Executable}s.
 */
@Getter
abstract class ExecutableContainerImpl implements ExecutableContainer {
    protected final @NotNull Method actualExecutable;

    /**
     * Instantiates a new Method container.
     *
     * @param method the method to create from
     */
    public ExecutableContainerImpl(final @NotNull Method method) {
        this.actualExecutable = method;
    }

    @Override
    public int getModifiers() {
        return this.actualExecutable.getModifiers();
    }

    @Override
    public @NotNull String getName() {
        return this.actualExecutable.getName();
    }

    @Override
    public @NotNull Class<?>[] getParameterTypes() {
        return this.actualExecutable.getParameterTypes();
    }

    @Override
    public boolean isVarArgs() {
        return this.actualExecutable.isVarArgs();
    }

}
