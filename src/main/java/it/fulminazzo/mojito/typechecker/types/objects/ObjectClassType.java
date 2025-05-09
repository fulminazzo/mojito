package it.fulminazzo.mojito.typechecker.types.objects;

import it.fulminazzo.fulmicollection.utils.ReflectionUtils;
import it.fulminazzo.fulmicollection.utils.StringUtils;
import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.*;
import it.fulminazzo.mojito.typechecker.types.objects.generics.GenericsObjectClassType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Represents the wrappers, {@link String} and {@link Object} classes.
 */
public enum ObjectClassType implements ClassType {
    /**
     * {@link java.lang.Byte}
     */
    BYTE(PrimitiveClassType.BYTE),
    /**
     * {@link java.lang.Short}
     */
    SHORT(PrimitiveClassType.SHORT),
    /**
     * {@link java.lang.Character}
     */
    CHARACTER(PrimitiveClassType.CHAR),
    /**
     * {@link java.lang.Integer}
     */
    INTEGER(PrimitiveClassType.INT),
    /**
     * {@link java.lang.Long}
     */
    LONG(PrimitiveClassType.LONG),
    /**
     * {@link java.lang.Float}
     */
    FLOAT(PrimitiveClassType.FLOAT),
    /**
     * {@link java.lang.Double}
     */
    DOUBLE(PrimitiveClassType.DOUBLE),
    /**
     * {@link java.lang.Boolean}
     */
    BOOLEAN(PrimitiveClassType.BOOLEAN),
    /**
     * {@link java.lang.String}
     */
    STRING,
    /**
     * {@link java.lang.Object}
     */
    OBJECT,
    ;

    private final @Nullable ClassType associatedType;

    ObjectClassType() {
        this(null);
    }

    ObjectClassType(final @Nullable PrimitiveClassType associatedType) {
        this.associatedType = associatedType;
    }

    @Override
    public @NotNull Type cast(@NotNull Type type) {
        if (type.equals(Types.NULL_TYPE)) return toType();
        if (this != OBJECT) {
            Type valueType = this.associatedType == null ? STRING : this.associatedType.toType();
            if (!type.is(valueType, ObjectType.of(toJavaClass())))
                throw TypeCheckerException.invalidCast(this, type);
        }
        return toType();
    }

    @Override
    public boolean compatibleWith(@NotNull Type type) {
        if (type.equals(Types.NULL_TYPE)) return true;
        else if (this.associatedType != null) return this.associatedType.compatibleWith(type);
        else {
            // Either STRING or OBJECT
            return !equals(STRING) || ObjectType.STRING.is(type);
        }
    }

    @Override
    public @NotNull Type toType() {
        return ObjectType.of(toJavaClass());
    }

    @Override
    public @NotNull Class<?> toJavaClass() {
        return ReflectionUtils.getClass("java.lang." + StringUtils.capitalize(name()));
    }

    @Override
    public @NotNull String toString() {
        return ClassType.print(StringUtils.capitalize(name()));
    }

    /**
     * Gets a new {@link ClassType} from the given class name.
     *
     * @param className the class name
     * @return the class type
     * @throws TypeException the exception thrown in case the class is not found
     */
    public static @NotNull ClassType of(final @NotNull String className) throws TypeException {
        return of(ObjectType.of(className).getInnerClass());
    }

    /**
     * Gets a new {@link ClassType} from the given class.
     * If it is present in this class, the corresponding field will be returned.
     * Otherwise, a new {@link CustomObjectClassType} will be created.
     *
     * @param clazz the class
     * @return the respective class type
     */
    public static @NotNull ClassType of(final @NotNull Class<?> clazz) {
        ObjectType type = ObjectType.of(clazz);
        try {
            return ObjectClassType.valueOf(type.getInnerClass().getSimpleName().toUpperCase());
        } catch (IllegalArgumentException e) {
            return new CustomObjectClassType(type);
        }
    }

    /**
     * Gets a new {@link ClassType} from the given {@link ClassType} that supports generic typing.
     *
     * @param classType    the class type
     * @param genericTypes the generic types
     * @return the class type
     */
    public static @NotNull ClassType of(final @NotNull ClassType classType,
                                        final @NotNull Collection<ClassType> genericTypes) {
        return of(classType.toJavaClass(), genericTypes);
    }

    /**
     * Gets a new {@link ClassType} from the given {@link Class} that supports generic typing.
     *
     * @param clazz        the class
     * @param genericTypes the generic types
     * @return the class type
     */
    public static @NotNull ClassType of(final @NotNull Class<?> clazz,
                                        final @NotNull Collection<ClassType> genericTypes) {
        return of(ObjectType.of(clazz), genericTypes);
    }

    /**
     * Gets a new {@link ClassType} from the given {@link ObjectType} that supports generic typing.
     *
     * @param type         the type
     * @param genericTypes the generic types
     * @return the class type
     */
    public static @NotNull ClassType of(final @NotNull ObjectType type,
                                        final @NotNull Collection<ClassType> genericTypes) {
        return new GenericsObjectClassType(type, genericTypes);
    }

}

