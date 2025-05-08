package it.fulminazzo.mojito.typechecker.types.objects.generics;

import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.ParameterTypes;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import it.fulminazzo.mojito.typechecker.types.objects.CustomObjectClassType;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObjectException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a {@link ObjectClassType} with a class different from the default types.
 * It supports generic typing.
 */
public class GenericsObjectClassType extends CustomObjectClassType implements ClassType {
    private final Map<String, ClassType> genericTypes;

    /**
     * Instantiates a new Generics object class type.
     *
     * @param internalType the internal type
     * @param genericTypes the generic types
     */
    public GenericsObjectClassType(final @NotNull ObjectType internalType,
                                   final @NotNull Collection<ClassType> genericTypes) {
        super(internalType);
        this.genericTypes = GenericsUtils.genericTypesToMap(toJavaClass(), genericTypes);
    }

    @Override
    public @NotNull Type cast(@NotNull Type type) {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public boolean compatibleWith(@NotNull Type type) {
        if (type.is(GenericsObjectType.class)) {
            GenericsObjectType genericsObjectType = (GenericsObjectType) type;
            if (!toJavaClass().isAssignableFrom(genericsObjectType.getInnerClass()))
                return false;
            Map<String, ClassType> genericTypes = genericsObjectType.getGenericTypes();
            for (String key : this.genericTypes.keySet()) {
                if (!genericTypes.containsKey(key)) return false;
                if (!this.genericTypes.get(key).is(genericTypes.get(key)))
                    return false;
            }
            return true;
        } else return super.compatibleWith(type);
    }

    @Override
    public @NotNull Type newObject(@NotNull ParameterTypes parameters) throws VisitorObjectException {
        //TODO:
        return super.newObject(parameters);
    }

    @Override
    public @NotNull Type newObject(@NotNull Constructor<?> constructor, @NotNull ParameterTypes parameterTypes) throws TypeException {
        //TODO:
        return super.newObject(constructor, parameterTypes);
    }

    @Override
    public @NotNull Type toType() {
        return new GenericsObjectType(this.object.getClass(), this.genericTypes.values());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass().hashCode(), this.object, this.genericTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GenericsObjectClassType) {
            GenericsObjectClassType other = (GenericsObjectClassType) o;
            return this.object.equals(other.object) && this.genericTypes.equals(other.genericTypes);
        }
        return super.equals(o);
    }

    @Override
    public @NotNull String toString() {
        String[] output = super.toString().split("\\.");
        return output[0] + "<" +
                this.genericTypes.values().stream()
                        .map(ClassType::toString)
                        .collect(Collectors.joining(", ")) +
                ">." + output[1];
    }

}
