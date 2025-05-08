package it.fulminazzo.mojito.typechecker.types.objects;

import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.ParameterTypes;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject;
import it.fulminazzo.mojito.wrappers.ObjectWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericsObjectType extends ObjectWrapper<Class<?>> implements Type {
    private final Map<String, ClassType> genericTypes;

    public GenericsObjectType(final @NotNull Class<?> clazz,
                              final @NotNull List<ClassType> genericTypes) {
        super(clazz);
        this.genericTypes = new HashMap<>();
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

}
