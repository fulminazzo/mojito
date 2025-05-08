package it.fulminazzo.mojito.wrappers;

import java.util.Objects;

/**
 * Represents a wrapper for an object.
 * Provides useful methods like {@link #hashCode()}, {@link #equals(Object)} and {@link #toString()}.
 *
 * @param <O> the type of the object
 */
public class ObjectWrapper<O> {
    protected final O object;

    /**
     * Instantiates a new Object wrapper.
     *
     * @param object the object
     */
    public ObjectWrapper(O object) {
        this.object = object;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), this.object);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass().equals(o.getClass()) && Objects.equals(this.object, ((ObjectWrapper<?>) o).object);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), this.object);
    }

}
