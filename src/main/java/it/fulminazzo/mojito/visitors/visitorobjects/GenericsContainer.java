package it.fulminazzo.mojito.visitors.visitorobjects;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents a particular {@link VisitorObject} that supports generic types.
 *
 * @param <C> the type of the parameterized types
 */
public interface GenericsContainer<C extends ClassVisitorObject<C, ?, ?>> {

    /**
     * Gets generic types.
     *
     * @return the generic types
     */
    @NotNull Map<String, C> getGenericTypes();

}
