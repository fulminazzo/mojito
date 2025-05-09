package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import it.fulminazzo.mojito.visitors.visitorobjects.GenericsContainer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Holds critical information about a method.
 */
public interface ExecutableContainer {

    /**
     * Gets the method modifiers.
     *
     * @return the modifiers
     */
    int getModifiers();

    /**
     * Gets the method return type.
     *
     * @return the return type
     */
    @NotNull Class<?> getReturnType();

    /**
     * Gets the method name.
     *
     * @return the name
     */
    @NotNull String getName();

    /**
     * Get the method parameter types.
     *
     * @return an array containing the classes of the parameters
     */
    @NotNull Class<?>[] getParameterTypes();

    /**
     * Checks if the method accepts variable arguments.
     *
     * @return true if it does
     */
    boolean isVarArgs();

    /**
     * Gets the actual method contained in this method.
     *
     * @return the method
     */
    @NotNull Method getActualExecutable();

    /**
     * Gets an instance of a {@link ExecutableContainer} from the given method.
     *
     * @param method the method
     * @return the method container
     */
    static @NotNull ExecutableContainer of(final @NotNull Method method) {
        return new MethodContainerImpl(method);
    }

    /**
     * Gets an instance of a {@link ExecutableContainer} from the given method.
     * It supports generics types, thanks to the given container.
     *
     * @param method            the method
     * @param genericsContainer the generics container
     * @return the method container
     */
    static @NotNull ExecutableContainer of(final @NotNull Method method,
                                           final @NotNull GenericsContainer<?> genericsContainer) {
        return new GenericsMethodContainerImpl<>(method, genericsContainer);
    }

}
