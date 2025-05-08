package it.fulminazzo.mojito.typechecker.types.objects.generics;

import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * A collection of utilities for the classes of this package.
 */
final class GenericsUtils {

    /**
     * Converts the given list of {@link ClassType}s to a {@link Map}.
     * The keys are the names of the generic types taken from the given {@link Class}.
     *
     * @param clazz        the class of reference
     * @param genericTypes the generic types
     * @return the final map
     */
    public static Map<String, ClassType> genericTypesToMap(final @NotNull Class<?> clazz,
                                                           final @NotNull Collection<ClassType> genericTypes) {
        Map<String, ClassType> finalMap = new LinkedHashMap<>();
        TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();

        if (typeParameters.length != genericTypes.size())
            throw TypeCheckerException.invalidGenericTypeSize(ClassType.of(clazz), typeParameters.length, genericTypes.size());

        List<ClassType> types = new ArrayList<>(genericTypes);
        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable<? extends Class<?>> expectedParameter = typeParameters[i];
            ClassType actualType = types.get(i);
            java.lang.reflect.Type[] bounds = expectedParameter.getBounds();
            for (java.lang.reflect.Type bound : bounds)
                try {
                    actualType.checkExtends(ClassType.of(bound.getTypeName()));
                } catch (TypeException e) {
                    throw TypeCheckerException.of(e);
                }
            finalMap.put(expectedParameter.getName(), actualType);
        }

        return finalMap;
    }

}
