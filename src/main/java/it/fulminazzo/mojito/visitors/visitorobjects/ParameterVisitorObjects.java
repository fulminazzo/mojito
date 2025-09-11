package it.fulminazzo.mojito.visitors.visitorobjects;

import it.fulminazzo.mojito.wrappers.ObjectWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents the list of parameters required during the invocation
 * of a method or the initialization of an object.
 *
 * @param <C> the class type of the parameters
 * @param <O> the type of the parameters
 * @param <P> the type of the {@link ParameterVisitorObjects}
 */
public abstract class ParameterVisitorObjects<
        C extends ClassVisitorObject<C, O, P>,
        O extends VisitorObject<C, O, P>,
        P extends ParameterVisitorObjects<C, O, P>
        > extends ObjectWrapper<List<O>>
        implements VisitorObject<C, O, P>, Iterable<O> {

    /**
     * Instantiates a new Parameter visitor object.
     *
     * @param parameters the parameters
     */
    public ParameterVisitorObjects(final @NotNull List<O> parameters) {
        super(parameters);
    }

    /**
     * Gets the number of parameters.
     *
     * @return the number of parameters
     */
    public int size() {
        return this.object.size();
    }

    /**
     * Replaces the parameter at the given index.
     *
     * @param i         the index
     * @param parameter the new parameter
     */
    public void set(int i, O parameter) {
        this.object.set(i, parameter);
    }

    /**
     * Gets the parameter at the given index.
     *
     * @param i the index
     * @return the parameter
     */
    public O get(int i) {
        return this.object.get(i);
    }

    /**
     * Converts this class to an array of Java {@link Class}es.
     *
     * @return the classes
     */
    public Class<?> @NotNull [] toJavaClassArray() {
        return this.object.stream()
                .map(o -> o.isNull() ? null : o.toClass())
                .map(c -> c == null ? null : c.toJavaClass())
                .toArray(Class[]::new);
    }

    @Override
    public @NotNull C toClass() {
        throw noClassType(getClass());
    }

    /**
     * Gets a stream of the internal objects.
     *
     * @return the stream
     */
    public @NotNull Stream<O> stream() {
        return this.object.stream();
    }

    @Override
    public @NotNull Iterator<O> iterator() {
        return stream().iterator();
    }

}
