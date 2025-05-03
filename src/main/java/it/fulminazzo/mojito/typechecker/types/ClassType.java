package it.fulminazzo.mojito.typechecker.types;

import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.objects.GenericsObjectClassType;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType;
import it.fulminazzo.mojito.utils.StringUtils;
import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the class of a {@link Type}.
 */
public interface ClassType extends Type, ClassVisitorObject<ClassType, Type, ParameterTypes> {

    @Override
    default boolean isPrimitive() {
        return this instanceof PrimitiveClassType;
    }

    /**
     * Checks whether the current class type extends the provided type.
     * If not, throws a {@link TypeCheckerException}.
     *
     * @param classType the class type
     */
    default void checkExtends(final @NotNull ClassType classType) {
        if (!classType.toJavaClass().isAssignableFrom(toJavaClass()))
            throw TypeCheckerException.invalidType(classType, this);
    }

    @Override
    default @NotNull Type newObject(final @NotNull Constructor<?> constructor,
                                    final @NotNull ParameterTypes parameterTypes) throws TypeException {
        if (!Modifier.isPublic(constructor.getModifiers()))
            throw TypeException.cannotAccessMethod(this, constructor);
        else return toType();
    }

    /**
     * Converts the current {@link ClassType} to the matching {@link Type}.
     *
     * @return the type
     */
    @NotNull Type toType();

    @Override
    default @NotNull Type toObject() {
        return toType();
    }

    @Override
    default @NotNull ClassType toClass() {
        return ClassType.of(Class.class);
    }

    /**
     * Gets a new {@link ClassType} from the given class name.
     * First checks if the given name comprehends a generic notation (%name%&lt;%parameters%&gt;).
     * If it does, a {@link GenericsObjectClassType} is returned.
     * Then, it tries to obtain from {@link PrimitiveClassType}.
     * If it fails, uses the fields of {@link ObjectClassType}.
     * Otherwise, a new type is created.
     *
     * @param className the class name
     * @return the class type
     * @throws TypeException the exception thrown in case the class is not found
     */
    static @NotNull ClassType of(final @NotNull String className) throws TypeException {
        String genericClassPattern = "([^<]+)<(.*)>";
        Matcher matcher = Pattern.compile(genericClassPattern).matcher(className);
        if (matcher.matches()) return of(matcher.group(1), matcher.group(2));
        try {
            String lowerCase = className.toLowerCase();
            if (lowerCase.equals(className)) return PrimitiveClassType.valueOf(className.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        return ObjectClassType.of(className);
    }

    /**
     * Gets a new {@link GenericsObjectClassType} from the given class name.
     * It uses the given <b>genericTypes</b> as list of parameters, which are
     * then each passed to {@link #of(String)}.
     *
     * @param className    the class name
     * @param genericTypes the generic types
     * @return class type
     * @throws TypeException the exception thrown in case a class is not found
     */
    static @NotNull ClassType of(final @NotNull String className,
                                 final @NotNull String genericTypes) throws TypeException {
        String[] types = StringUtils.quoteSplitter(genericTypes, ", *", "<", ">");
        List<ClassType> classTypes = new LinkedList<>();
        for (String type : types) classTypes.add(of(type));
        return new GenericsObjectClassType(ObjectType.of(className), classTypes);
    }

    /**
     * Gets a new {@link ClassType} from the given class.
     * Tries first to obtain from {@link PrimitiveClassType}.
     * If it fails, uses the fields of {@link ObjectClassType}.
     * Otherwise, a new type is created.
     *
     * @param clazz the class
     * @return the class type
     */
    static @NotNull ClassType of(final @NotNull Class<?> clazz) {
        for (PrimitiveClassType type : PrimitiveClassType.values())
            if (type.toJavaClass().equals(clazz)) return type;
        return ObjectClassType.of(clazz);
    }

    /**
     * Prints the given string to the format of a class.
     *
     * @param output the output
     * @return the new output
     */
    static @NotNull String print(final @NotNull String output) {
        return output + ".class";
    }

}
