package it.fulminazzo.mojito.typechecker.types.objects.generics;

import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.ParameterTypes;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType;
import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject;
import it.fulminazzo.mojito.wrappers.ObjectWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

public class GenericsObjectType extends ObjectWrapper<Class<?>> implements Type {
    private final Map<String, ClassType> genericTypes;

    public GenericsObjectType(final @NotNull Class<?> clazz,
                              final @NotNull List<ClassType> genericTypes) {
        super(clazz);
        this.genericTypes = new LinkedHashMap<>();
        TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();

        if (typeParameters.length != genericTypes.size())
            throw TypeCheckerException.invalidGenericTypeSize(ClassType.of(clazz), typeParameters.length, genericTypes.size());

        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable<? extends Class<?>> expectedParameter = typeParameters[i];
            ClassType actualType = genericTypes.get(i);
            java.lang.reflect.Type[] bounds = expectedParameter.getBounds();
            for (java.lang.reflect.Type bound : bounds)
                try {
                    actualType.checkExtends(ClassType.of(bound.getTypeName()));
                } catch (TypeException e) {
                    throw TypeCheckerException.of(e);
                }
            this.genericTypes.put(expectedParameter.getName(), actualType);
        }
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
