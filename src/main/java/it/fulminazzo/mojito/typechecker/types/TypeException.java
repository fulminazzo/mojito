package it.fulminazzo.mojito.typechecker.types;

import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObjectException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * An exception thrown by {@link Type} objects.
 */
public class TypeException extends VisitorObjectException {

    /**
     * Instantiates a new Type exception.
     *
     * @param message the message
     * @param args    the arguments to add in the message format
     */
    private TypeException(final @NotNull String message, final Object @NotNull ... args) {
        super(message, args);
    }

    /**
     * Generates a {@link TypeException} with message:
     * <i>Could not find class '%clazz%'</i>
     *
     * @param clazz the clazz
     * @return the type exception
     */
    public static @NotNull TypeException classNotFound(final @NotNull String clazz) {
        return new TypeException("Could not find class '%s'", clazz);
    }

    /**
     * Generates a {@link TypeException} with message:
     * <i>Could not find field '%field%' in type %type%</i>
     *
     * @param type  the type
     * @param field the field
     * @return the type exception
     */
    public static @NotNull TypeException fieldNotFound(final @NotNull ClassType type,
                                                       final @NotNull String field) {
        return new TypeException("Could not find field '%s' in type %s", field, type);
    }

    /**
     * Generates a {@link TypeException} with message:
     * <i>Cannot assign a value to final variable '%field%'</i>
     *
     * @param field the field
     * @return the type exception
     */
    public static @NotNull TypeException cannotModifyFinalField(final @NotNull String field) {
        return new TypeException("Cannot assign a value to final variable '%s'", field);
    }

    /**
     * Generates a {@link TypeException} with message:
     * <i>Type %type% cannot access field '%field%' with access-level '%access-level%'</i>
     *
     * @param type  the type
     * @param field the field
     * @return the type exception
     */
    public static @NotNull TypeException cannotAccessField(final @NotNull ClassType type,
                                                           final @NotNull Field field) {
        return new TypeException("Type %s cannot access field '%s' with access level '%s'",
                type, field.getName(), getVisibilityModifier(field));
    }

    /**
     * Generates a {@link TypeException} with message:
     * <i>Type %type% cannot access non-static field '%field%' from a static context</i>
     *
     * @param type  the type
     * @param field the field
     * @return the type exception
     */
    public static @NotNull TypeException cannotAccessStaticField(final @NotNull ClassType type,
                                                                 final @NotNull String field) {
        return new TypeException("Type %s cannot access non-static field '%s' from a static context",
                type, field);
    }

    /**
     * Generates a {@link TypeException} with message:
     * <i>Could not find method %method_format% in type %type%</i>
     *
     * @param type           the type
     * @param method         the method
     * @param parameterTypes the parameter types
     * @return the type exception
     */
    public static @NotNull TypeException methodNotFound(final @NotNull ClassType type,
                                                        final @NotNull String method,
                                                        final @NotNull ParameterTypes parameterTypes) {
        return new TypeException("Could not find method %s in type %s", formatMethod(method, parameterTypes), type);
    }

    /**
     * Generates a {@link TypeException} with message:
     * <i>Types mismatch: cannot apply parameters %parameter_types_format% to method %method_format% in type %type%</i>
     *
     * @param type           the type
     * @param method         the method
     * @param parameterTypes the parameter types
     * @return the type exception
     */
    public static @NotNull TypeException typesMismatch(final @NotNull ClassType type,
                                                       final @NotNull Executable method,
                                                       final @NotNull ParameterTypes parameterTypes) {
        return new TypeException("Types mismatch: cannot apply parameters %s to method %s in type %s",
                formatParameters(parameterTypes), formatMethod(method), type);
    }

    /**
     * Generates a {@link TypeException} with message:
     * <i>Type %type% cannot access method '%method_format%' with access-level '%access-level%'</i>
     *
     * @param type   the type
     * @param method the method
     * @return the type exception
     */
    public static @NotNull TypeException cannotAccessMethod(final @NotNull ClassType type,
                                                            final @NotNull Executable method) {
        return new TypeException("Type %s cannot access method %s with access level '%s'",
                type, formatMethod(method), getVisibilityModifier(method));
    }

    /**
     * Generates a {@link TypeException} with message:
     * <i>Type %type% cannot access non-static method '%method_format%' from a static context</i>
     *
     * @param type           the type
     * @param method         the method
     * @param parameterTypes the parameter types
     * @return the type exception
     */
    public static @NotNull TypeException cannotAccessStaticMethod(final @NotNull ClassType type,
                                                                  final @NotNull String method,
                                                                  final @NotNull ParameterTypes parameterTypes) {
        return new TypeException("Type %s cannot access non-static method %s from a static context",
                type, formatMethod(method, parameterTypes));
    }

    /**
     * Gets the visibility modifier from the given accessible object in a string format.
     *
     * @param accessibleObject the accessible object
     * @return the visibility modifier
     */
    static @NotNull String getVisibilityModifier(final @NotNull Member accessibleObject) {
        int modifiers = accessibleObject.getModifiers();
        if (Modifier.isProtected(modifiers)) return "protected";
        else if (Modifier.isPublic(modifiers)) return "public";
        else if (Modifier.isPrivate(modifiers)) return "private";
        else return "package";
    }

    /**
     * Formats the given method and parameter types to a string.
     *
     * @param method the method
     * @return the string
     */
    static @NotNull String formatMethod(final @NotNull Executable method) {
        return formatMethod(method.getName(), new ParameterTypes(Arrays.stream(method.getParameterTypes())
                .map(ClassType::of)
                .collect(Collectors.toList())));
    }

}
