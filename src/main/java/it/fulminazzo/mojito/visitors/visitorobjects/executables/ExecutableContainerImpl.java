package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * A general implementation of {@link ExecutableContainer} for {@link Executable}s.
 */
@Getter
abstract class ExecutableContainerImpl<E extends Executable> implements ExecutableContainer<E> {
    protected final @NotNull E actualExecutable;

    /**
     * Instantiates a new Executable container.
     *
     * @param executable the executable to create from
     */
    public ExecutableContainerImpl(final @NotNull E executable) {
        this.actualExecutable = executable;
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
