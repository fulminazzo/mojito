package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject;
import it.fulminazzo.mojito.visitors.visitorobjects.GenericsContainer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * An implementation of {@link ExecutableContainer} for {@link Constructor}s that supports generic types.
 *
 * @param <C> the type of the parameterized types
 */
@Getter
class GenericsConstructorContainerImpl<C extends ClassVisitorObject<C, ?, ?>> extends ConstructorContainerImpl {
    private final @NotNull Class<?>[] parameterTypes;

    /**
     * Instantiates a new Generics constructor container.
     *
     * @param constructor       the constructor to create from
     * @param genericsContainer the generics container to get the parameters from
     */
    public GenericsConstructorContainerImpl(@NotNull Constructor<?> constructor,
                                            @NotNull GenericsContainer<C> genericsContainer) {
        super(constructor);
        final Map<String, C> types = genericsContainer.getGenericTypes(constructor);

        this.parameterTypes = GenericsExecutableUtils.getActualParameterTypes(types, constructor);
    }

}
