package it.fulminazzo.mojito.typechecker.types.objects.generics;

import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.ParameterTypes;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType;
import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject;
import it.fulminazzo.mojito.wrappers.ObjectWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GenericsObjectType extends ObjectWrapper<Class<?>> implements Type {
    private final Map<String, ClassType> genericTypes;

    public GenericsObjectType(final @NotNull Class<?> clazz,
                              final @NotNull List<ClassType> genericTypes) {
        super(clazz);
        this.genericTypes = GenericsUtils.genericTypesToMap(clazz, genericTypes);
    }

    @Override
    public @NotNull ClassType toClass() {
        //TODO:
        return null;
    }

    @Override
    public boolean is(@NotNull Class<?> object) {
        return Type.super.is(object);
    }

    @Override
    public boolean is(Type @NotNull ... objects) {
        return Type.super.is(objects);
    }

    @Override
    public boolean isAssignableFrom(@NotNull ClassVisitorObject<ClassType, Type, ParameterTypes> classVisitorObject) {
        return Type.super.isAssignableFrom(classVisitorObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass().hashCode(), this.object, this.genericTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GenericsObjectType) {
            GenericsObjectType other = (GenericsObjectType) o;
            return this.object.equals(other.object) && this.genericTypes.equals(other.genericTypes);
        }
        return false;
    }

    @Override
    public String toString() {
        return Type.print(ObjectType.getClassName(this.object) + String.format("<%s>",
                this.genericTypes.values().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
        ));
    }

}
