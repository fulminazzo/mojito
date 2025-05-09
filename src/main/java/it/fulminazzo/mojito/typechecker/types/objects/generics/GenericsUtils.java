package it.fulminazzo.mojito.typechecker.types.objects.generics;

import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of utilities for the classes of this package.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenericsUtils {
    public static final @NotNull Pattern GENERICS_CLASS_PATTERN = Pattern.compile("([^<]+)<(.*)>");

    /**
     * Checks whether the given {@link GenericsObjectType} is compatible with
     * {@link GenericsObjectClassType}. It does so by converting every parameterized type name of the first
     * with the names specified in the latter.
     * As an example, {@link Collection} uses <i>E</i> as type name, but {@link Iterable} specified <i>T</i>.
     * Therefore, the types saved as <i>E</i> are translated to <i>T</i>, before checking.
     *
     * @param model  the class type
     * @param target the object type
     * @return true if they are compatible
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean checkCompatibility(
            final @NotNull GenericsObjectClassType model,
            final @NotNull GenericsObjectType target
    ) {
        Class<?> modelClass = model.toJavaClass();
        Map<String, ClassType> modelGenericTypes = model.getGenericTypes();

        Class<?> targetClass = target.getInnerClass();
        Map<String, ClassType> targetGenericTypes = new LinkedHashMap<>(target.getGenericTypes());

        @NotNull List<Class<?>> hierarchy = getClassHierarchy(targetClass, modelClass);
        if (hierarchy.isEmpty()) return false;

        for (int i = 0; i < hierarchy.size() - 1; i++) {
            Class<?> current = hierarchy.get(i);
            Class<?> next = hierarchy.get(i + 1);

            Type superClass = next.isInterface() ?
                    Arrays.stream(current.getGenericInterfaces())
                            .filter(t -> {
                                Matcher matcher = GENERICS_CLASS_PATTERN.matcher(t.getTypeName());
                                // Already checked in previous steps
                                matcher.matches();
                                return matcher.group(1).equals(next.getName());
                            })
                            .findFirst().orElseThrow(IllegalStateException::new) :
                    current.getGenericSuperclass();

            Matcher matcher = GENERICS_CLASS_PATTERN.matcher(superClass.getTypeName());
            // Already checked in previous steps
            matcher.matches();
            String[] types = StringUtils.quoteSplitter(matcher.group(2), ", *", "<", ">");

            TypeVariable<? extends Class<?>>[] actualTypes = next.getTypeParameters();

            Map<String, ClassType> newTargetGenericTypes = new LinkedHashMap<>();
            for (int j = 0; j < actualTypes.length; j++)
                newTargetGenericTypes.put(actualTypes[j].getTypeName(), targetGenericTypes.get(types[j]));
            targetGenericTypes = newTargetGenericTypes;
        }

        for (String key : modelGenericTypes.keySet()) {
            if (!targetGenericTypes.containsKey(key)) return false;
            if (!modelGenericTypes.get(key).is(targetGenericTypes.get(key)))
                return false;
        }

        return true;
    }

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
    public static @NotNull Map<String, ClassType> genericTypesToMap(final @NotNull Class<?> clazz,
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
                actualType.checkExtends(ClassType.of(bound));
            finalMap.put(expectedParameter.getName(), actualType);
        }

        return finalMap;
    }

}
