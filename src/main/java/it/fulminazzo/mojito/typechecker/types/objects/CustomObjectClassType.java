package it.fulminazzo.mojito.typechecker.types.objects;

import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.TypeWrapper;
import it.fulminazzo.mojito.typechecker.types.Types;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link ObjectClassType} with a class different from the default types.
 */
public class CustomObjectClassType extends TypeWrapper implements ClassType {

    /**
     * Instantiates a new Custom class object type.
     *
     * @param internalType the internal type
     */
    protected CustomObjectClassType(@NotNull ObjectType internalType) {
        super(internalType);
    }

    @Override
    public @NotNull Type toType() {
        return this.object;
    }

    @Override
    public @NotNull Type cast(@NotNull Type type) {
        if (type.equals(Types.NULL_TYPE)) return toType();
        if (type.is(ObjectType.class)) {
            Class<?> typeClass = ((ObjectType) type).getInnerClass();
            Class<?> currentClass = toJavaClass();
            if (currentClass.isAssignableFrom(typeClass) || typeClass.isAssignableFrom(currentClass))
                return toType();
        }
        throw TypeCheckerException.invalidCast(this, type);
    }

    @Override
    public boolean compatibleWith(@NotNull Type type) {
        if (type instanceof ObjectType)
            return toJavaClass().isAssignableFrom(((ObjectType) type).getInnerClass());
        else return type.equals(Types.NULL_TYPE);
    }

    @Override
    public @NotNull Class<?> toJavaClass() {
        return ((ObjectType) getInternalType()).getInnerClass();
    }

    @Override
    public @NotNull String toString() {
        return ClassType.print(ObjectType.getClassName(((ObjectType) this.object).getInnerClass()));
    }

}
