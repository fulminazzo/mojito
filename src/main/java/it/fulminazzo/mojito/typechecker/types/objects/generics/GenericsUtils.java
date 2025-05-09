package it.fulminazzo.mojito.typechecker.types.objects.generics;

import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * A collection of utilities for the classes of this package.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class GenericsUtils {

    /**
     * Gets the hierarchy "path" from one class to the other.
     * The hierarchy path is a list of classes or interfaces
     * chained together by a one on one hierarchy relation.
     *
     * @param start the class to start from
     * @param end   the final reached class
     * @return the path
     */
    public static @NotNull List<Class<?>> getClassHierarchy(
            final @NotNull Class<?> start,
            final @NotNull Class<?> end
    ) {
        if (start.equals(end)) return Collections.singletonList(end);

        List<Class<?>> tmp = new LinkedList<>();
        Class<?> superClass = start.getSuperclass();
        if (superClass != null) tmp.add(superClass);
        tmp.addAll(Arrays.asList(start.getInterfaces()));

        for (Class<?> clazz : tmp) {
            List<Class<?>> hierarchy = getClassHierarchy(clazz, end);
            if (!hierarchy.isEmpty()) {
                LinkedList<Class<?>> result = new LinkedList<>(hierarchy);
                result.addFirst(start);
                return result;
            }
        }

        return new LinkedList<>();
    }

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
