package it.fulminazzo.mojito.handler.elements;

import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.fulmicollection.utils.ReflectionUtils;
import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObjectException;
import it.fulminazzo.mojito.visitors.visitorobjects.executables.ExecutableContainer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class ClassElement extends ElementImpl implements ClassVisitorObject<ClassElement, Element, ParameterElements> {

    private ClassElement(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public boolean isPrimitive() {
        return ReflectionUtils.isPrimitive(toJavaClass());
    }

    @Override
    public @NotNull Element cast(@NotNull Element object) {
        return Element.of(toJavaClass().cast(object.getElement()));
    }

    @Override
    public boolean compatibleWith(@NotNull Element object) {
        return toJavaClass().isAssignableFrom(object.toClass().toJavaClass());
    }

    @Override
    public @NotNull Element newObject(@NotNull ExecutableContainer<Constructor<?>> constructor, @NotNull ParameterElements parameters) throws VisitorObjectException {
        Refl<?> refl = new Refl<>(toJavaClass(), constructor.getParameterTypes(), parameters.stream()
                .map(Element::getElement).toArray(Object[]::new));
        return Element.of(refl.getObject());
    }

    @Override
    public @NotNull Class<?> toJavaClass() {
        return (Class<?>) getElement();
    }

    @Override
    public @NotNull Element toObject() {
        if (isPrimitive()) {
            Class<?> clazz = toJavaClass();
            if (clazz.equals(byte.class)) return Element.of((byte) 0);
            else if (clazz.equals(char.class)) return Element.of((char) 0);
            else if (clazz.equals(short.class)) return Element.of((short) 0);
            else if (clazz.equals(int.class)) return Element.of(0);
            else if (clazz.equals(long.class)) return Element.of(0L);
            else if (clazz.equals(float.class)) return Element.of(0f);
            else if (clazz.equals(double.class)) return Element.of(0d);
            else return Element.of(false);
        } else return Element.of(null);
    }

    public static ClassElement of(Class<?> clazz) {
        return new ClassElement(clazz);
    }

}
