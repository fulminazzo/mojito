package it.fulminazzo.mojito.typechecker.types.objects.generics;

import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.ParameterTypes;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import it.fulminazzo.mojito.typechecker.types.objects.CustomObjectClassType;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType;
import it.fulminazzo.mojito.visitors.visitorobjects.GenericsContainer;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObjectException;
import it.fulminazzo.mojito.visitors.visitorobjects.executables.ExecutableContainer;
import lombok.Getter;
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
@Getter
public class GenericsObjectClassType extends CustomObjectClassType implements ClassType, GenericsContainer<ClassType> {
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
        if (type.is(GenericsObjectType.class)) {
            GenericsObjectType genericsObjectType = (GenericsObjectType) type;
            Class<?> typeClass = genericsObjectType.getInnerClass();
            Class<?> currentClass = toJavaClass();
            if (currentClass.isAssignableFrom(typeClass) || typeClass.isAssignableFrom(currentClass)) {
                Map<String, ClassType> genericTypes = genericsObjectType.getGenericTypes();
                for (String key : this.genericTypes.keySet()) {
                    ClassType genericType = genericTypes.get(key);
                    if (genericType != null)
                        this.genericTypes.get(key).check(genericType);
                }
            }
        }
        return super.cast(type);
    }

    @Override
    public boolean compatibleWith(@NotNull Type type) {
        if (type.is(GenericsObjectType.class))
            return GenericsUtils.checkCompatibility(this, (GenericsObjectType) type);
        else return super.compatibleWith(type);
    }

    @Override
    public @NotNull Type newObject(@NotNull ParameterTypes parameters) throws VisitorObjectException {
        //TODO:
        return super.newObject(parameters);
    }

    @Override
    public @NotNull Type newObject(@NotNull ExecutableContainer<Constructor<?>> constructor, @NotNull ParameterTypes parameterTypes) throws TypeException {
        //TODO:
        return super.newObject(constructor, parameterTypes);
    }

    @Override
    public @NotNull Type toType() {
        ObjectType objectType = (ObjectType) this.object;
        return new GenericsObjectType(objectType.getInnerClass(), this.genericTypes.values());
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
