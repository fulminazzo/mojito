package it.fulminazzo.javaparser.typechecker.types.objects;

import it.fulminazzo.javaparser.typechecker.types.ClassType;
import it.fulminazzo.javaparser.typechecker.types.Type;
import it.fulminazzo.javaparser.typechecker.types.TypeWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link ClassObjectType} with a class different from the default types.
 */
class CustomClassObjectType extends TypeWrapper implements ClassType {

    /**
     * Instantiates a new Custom class object type.
     *
     * @param internalType the internal type
     */
    public CustomClassObjectType(@NotNull ObjectType internalType) {
        super(internalType);
    }

    @Override
    public Class<?> toJavaClass() {
        return ((ObjectType) getInternalType()).getInnerClass();
    }

    @Override
    public boolean compatibleWith(@NotNull Type type) {
        if (type instanceof ObjectType) {
            ObjectType objectType = (ObjectType) type;
            return this.object.equals(objectType);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.object.toString().replace("Type", "ClassType");
    }

}
