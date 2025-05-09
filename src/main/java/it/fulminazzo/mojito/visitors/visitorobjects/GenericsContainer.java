package it.fulminazzo.mojito.visitors.visitorobjects;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a particular {@link VisitorObject} that supports generic types.
 *
 * @param <C> the type of the parameterized types
 */
public interface GenericsContainer<C extends ClassVisitorObject<C, ?, ?>> {

    /**
     * Gets the generic types of this container.
     *
     * @return the generic types
     */
    @NotNull Map<String, C> getGenericTypes();

    /**
     * Gets the generic types of this container.
     * Removes all the types that have the same names as the {@link Executable}
     * own parameter types.
     *
     * @param executable the executable
     * @return the generic types
     */
    default @NotNull Map<String, C> getGenericTypes(final @NotNull Executable executable) {
        final Map<String, C> types = new LinkedHashMap<>(getGenericTypes());
        Arrays.stream(executable.getTypeParameters()).map(Type::getTypeName).forEach(types::remove);
        return types;
    }

}
