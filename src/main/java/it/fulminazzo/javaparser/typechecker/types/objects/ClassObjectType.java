package it.fulminazzo.javaparser.typechecker.types.objects;

import it.fulminazzo.javaparser.typechecker.types.ClassType;
import it.fulminazzo.javaparser.typechecker.types.PrimitiveType;
import it.fulminazzo.javaparser.typechecker.types.Type;
import it.fulminazzo.javaparser.typechecker.types.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an {@link Object} class type.
 */
public enum ClassObjectType implements ClassType {
    /**
     * {@link java.lang.Byte}
     */
    BYTE(),
    /**
     * {@link java.lang.Character}
     */
    CHARACTER(),
    /**
     * {@link java.lang.Short}
     */
    SHORT(),
    /**
     * {@link java.lang.Integer}
     */
    INTEGER(),
    /**
     * {@link java.lang.Long}
     */
    LONG(),
    /**
     * {@link java.lang.Float}
     */
    FLOAT(),
    /**
     * {@link java.lang.Double}
     */
    DOUBLE(),
    /**
     * {@link java.lang.Boolean}
     */
    BOOLEAN(),
    /**
     * {@link java.lang.String}
     */
    STRING(),
    ;

    private final @Nullable PrimitiveType associatedType;

    ClassObjectType() {
        this(null);
    }

    ClassObjectType(final @Nullable PrimitiveType associatedType) {
        this.associatedType = associatedType;
    }

    @Override
    public boolean compatibleWith(@NotNull Type type) {
        for (Type compatibleType : this.compatibleTypes)
            if (compatibleType.equals(type)) return true;
        return false;
    }

}

