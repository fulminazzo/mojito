package it.fulminazzo.javaparser.executor.values.arrays;

import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.javaparser.executor.values.ClassValue;
import it.fulminazzo.javaparser.executor.values.Value;
import it.fulminazzo.javaparser.wrappers.ObjectWrapper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a general array {@link Value}.
 *
 * @param <A> the type of the array
 */
@Getter
@SuppressWarnings("unchecked")
public class ArrayValue<A> extends ObjectWrapper<List<Value<A>>> implements Value<A> {
    private final @NotNull ClassValue<A> componentsType;

    /**
     * Instantiates a static array value.
     *
     * @param componentsType the components type
     * @param size           the size of the array
     */
    ArrayValue(final @NotNull ClassValue<A> componentsType, final int size) {
        super(new LinkedList<>());
        this.componentsType = componentsType;
        for (int i = 0; i < size; i++) this.object.add(componentsType.toValue());
    }

    /**
     * Instantiates a dynamic array value.
     *
     * @param componentsType the components type
     * @param values         the values of the array
     */
    ArrayValue(final @NotNull ClassValue<A> componentsType, final @NotNull Collection<Value<A>> values) {
        super(new LinkedList<>());
        this.componentsType = componentsType;
        List<Value<A>> list = new LinkedList<>(values);
        for (int i = 0; i < values.size(); i++) this.object.add(list.get(i));
    }

    /**
     * Gets the corresponding value at the given index.
     *
     * @param index the index
     * @return value
     */
    public Value<A> get(final int index) {
        return this.object.get(index);
    }

    /**
     * Allows to manually set a value at the specified index.
     *
     * @param index the index
     * @param value the value
     */
    public void set(final int index, final Value<?> value) {
        this.object.set(index, (Value<A>) value);
    }

    @Override
    public @NotNull ClassValue<A> toClass() {
        return new ArrayClassValue<>(this.componentsType);
    }

    /**
     * Gets values.
     *
     * @return the values
     */
    public @NotNull List<Value<A>> getValues() {
        return this.object;
    }

    @Override
    public @NotNull A getValue() {
        Class<A> componentsType = this.componentsType.getValue();
        A array = (A) Array.newInstance(componentsType, this.object.size());
        for (int i = 0; i < this.object.size(); i++)
            Array.set(array, i, this.object.get(i).getValue());
        return array;
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s(%s, %s)", getClass().getSimpleName(), this.componentsType, this.object);
    }

    /**
     * Instantiates a static array value.
     *
     * @param <V>            the type of the value
     * @param componentsType the components type
     * @param size           the size of the array
     * @return the array value
     */
    public static <V> @NotNull ArrayValue<V> of(final @NotNull ClassValue<V> componentsType,
                                                final int size) {
        return new ArrayValue<>(componentsType, size);
    }

    /**
     * Instantiates a dynamic array value.
     *
     * @param <V>            the type of the value
     * @param componentsType the components type
     * @param values         the values of the array
     * @return the array value
     */
    public static <V> @NotNull ArrayValue<V> of(final @NotNull ClassValue<V> componentsType,
                                                final @NotNull Collection<Value<V>> values) {
        return new ArrayValue<>(componentsType, values);
    }

    /**
     * Instantiates a new array value from the given array.
     *
     * @param <V>    the components type
     * @param <T>    the type of the object
     * @param object the object
     * @return the array value
     */
    public static <V, T> @NotNull ArrayValue<V> of(final @NotNull T object) {
        Class<V> componentType = (Class<V>) object.getClass().getComponentType();
        final Collection<Value<V>> values;
        Refl<?> arrayUtils = new Refl<>(ArrayUtils.class);
        if (componentType.isPrimitive()) values = arrayUtils.invokeMethod("toValueCollection", object);
        else values = arrayUtils.invokeMethod("toValueCollection", new Class[]{Object[].class}, object);
        return of(ClassValue.of(componentType), values);
    }

}
