package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import it.fulminazzo.mojito.visitors.visitorobjects.GenericsContainer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * Holds critical information about an executable.
 */
public interface ExecutableContainer<E extends Executable> {

    /**
     * Gets the executable modifiers.
     *
     * @return the modifiers
     */
    int getModifiers();

    /**
     * Gets the executable return type.
     *
     * @return the return type
     */
    @NotNull Class<?> getReturnType();

    /**
     * Gets the executable name.
     *
     * @return the name
     */
    @NotNull String getName();

    /**
     * Get the executable parameter types.
     *
     * @return an array containing the classes of the parameters
     */
    @NotNull Class<?>[] getParameterTypes();

    /**
     * Checks if the executable accepts variable arguments.
     *
     * @return true if it does
     */
    boolean isVarArgs();

    /**
     * Gets the actual executable contained in this executable.
     *
     * @return the executable
     */
    @NotNull E getActualExecutable();

    /**
     * Gets an instance of a {@link ExecutableContainer} from the given method.
     *
     * @param method the method
     * @return the executable container
     */
    static @NotNull ExecutableContainer<Method> of(final @NotNull Method method) {
        return new MethodContainerImpl(method);
    }

    /**
     * Gets an instance of a {@link ExecutableContainer} from the given method.
     * It supports generics types, thanks to the given container.
     *
     * @param method            the method
     * @param genericsContainer the generics container
     * @return the executable container
     */
    static @NotNull ExecutableContainer<Method> of(final @NotNull Method method,
                                           final @NotNull GenericsContainer<?> genericsContainer) {
        return new GenericsMethodContainerImpl<>(method, genericsContainer);
    }

}
