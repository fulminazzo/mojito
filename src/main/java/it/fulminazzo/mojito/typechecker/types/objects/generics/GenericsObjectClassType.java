package it.fulminazzo.mojito.typechecker.types.objects.generics;

import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.ParameterTypes;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import it.fulminazzo.mojito.typechecker.types.objects.CustomObjectClassType;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType;
import it.fulminazzo.mojito.typechecker.types.variables.TypeFieldContainer;
import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObject;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObjectException;
import it.fulminazzo.mojito.visitors.visitorobjects.variables.FieldContainer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
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
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public void checkExtends(@NotNull ClassType classType) {
        if (true) return;
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public @NotNull Type newObject(@NotNull Constructor<?> constructor, @NotNull ParameterTypes parameterTypes) throws TypeException {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public <T extends VisitorObject<ClassType, Type, ParameterTypes>> @NotNull T check(@NotNull Class<T> classType) {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public @NotNull Type check(Type @NotNull ... expectedTypes) {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public @NotNull Type checkNot(Type @NotNull ... expectedTypes) {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public @NotNull Type checkAssignableFrom(@NotNull ClassType classType) {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public @NotNull TypeFieldContainer getField(@NotNull Field field) throws TypeException {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public @NotNull Type invokeMethod(@NotNull Method method, @NotNull ParameterTypes parameterTypes) throws TypeException {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public boolean compatibleWith(@NotNull Object object) {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public @NotNull Type newObject(@NotNull ParameterTypes parameters) throws VisitorObjectException {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public boolean is(@NotNull Class<?> object) {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public boolean is(Type @NotNull ... objects) {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public boolean isAssignableFrom(@NotNull ClassVisitorObject<ClassType, Type, ParameterTypes> classVisitorObject) {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public @NotNull FieldContainer<ClassType, Type, ParameterTypes> getField(@NotNull String fieldName) throws VisitorObjectException {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public @NotNull Type invokeMethod(@NotNull String methodName, @NotNull ParameterTypes parameters) throws VisitorObjectException {
        //TODO:
        throw new IllegalStateException("Not implemented yet");
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
