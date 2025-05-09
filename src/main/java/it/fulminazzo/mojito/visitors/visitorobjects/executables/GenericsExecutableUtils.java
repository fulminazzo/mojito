package it.fulminazzo.mojito.visitors.visitorobjects.executables;

import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * A collection of utilities for {@link ExecutableContainer} that support generic types.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class GenericsExecutableUtils {

    /**
     * Checks if the given {@link Type} is a {@link Class}.
     * If it is, the fallback class is returned.
     * Otherwise, it is converted with {@link #typeNameToClass(Map, String)}.
     *
     * @param <C>      the type of the class visitor object
     * @param type     the type
     * @param types    the types
     * @param fallback the fallback class
     * @return the found class
     */
    public static <C extends ClassVisitorObject<C, ?, ?>> @NotNull Class<?> getClassFromType(
            final @NotNull Type type,
            final @NotNull Map<String, C> types,
            final @NotNull Class<?> fallback
    ) {
        String typeName = type.getTypeName().replace("[]", "");
        if (!(type instanceof Class<?>) && types.containsKey(typeName))
            return typeNameToClass(types, type.getTypeName());
        else return fallback;
    }

    /**
     * Loops through the given {@link Map} and gets the corresponding class from the given string.
     * If it contains "[]", the class is returned in array form.
     *
     * @param <C>      the type of the class visitor object
     * @param types    the types
     * @param typeName the type name
     * @return the converted class
     */
    public static <C extends ClassVisitorObject<C, ?, ?>> @NotNull Class<?> typeNameToClass(
            final @NotNull Map<String, C> types,
            final @NotNull String typeName
    ) {
        if (typeName.contains("[]")) {
            Class<?> typeClass = typeNameToClass(types, typeName.substring(0, typeName.indexOf("[]")));
            return Array.newInstance(typeClass, 0).getClass();
        } else return types.get(typeName).toJavaClass();
    }

}
