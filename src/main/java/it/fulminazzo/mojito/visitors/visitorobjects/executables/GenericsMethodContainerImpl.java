package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject;
import it.fulminazzo.mojito.visitors.visitorobjects.GenericsContainer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * An implementation of {@link ExecutableContainer} for {@link Method}s that supports generic types.
 *
 * @param <C> the type of the parameterized types
 */
@Getter
class GenericsMethodContainerImpl<C extends ClassVisitorObject<C, ?, ?>> extends MethodContainerImpl {
    private final @NotNull Class<?> returnType;
    private final @NotNull Class<?>[] parameterTypes;

    /**
     * Instantiates a new Generics method container.
     *
     * @param method            the method to create from
     * @param genericsContainer the generics container to get the parameters from
     */
    public GenericsMethodContainerImpl(final @NotNull Method method,
                                       final @NotNull GenericsContainer<C> genericsContainer) {
        super(method);
        final Map<String, C> types = genericsContainer.getGenericTypes(method);

        this.returnType = GenericsExecutableUtils.getClassFromType(method.getGenericReturnType(), types, method.getReturnType());

        this.parameterTypes = GenericsExecutableUtils.getActualParameterTypes(types, method);
    }

}
