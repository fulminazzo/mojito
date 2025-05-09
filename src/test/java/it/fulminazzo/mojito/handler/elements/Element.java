package it.fulminazzo.mojito.handler.elements;

import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.mojito.handler.HandlerException;
import it.fulminazzo.mojito.handler.elements.variables.ElementFieldContainer;
import it.fulminazzo.mojito.tokenizer.TokenType;
import it.fulminazzo.mojito.visitors.visitorobjects.executables.ExecutableContainer;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObject;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObjectException;
import it.fulminazzo.mojito.visitors.visitorobjects.variables.FieldContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public interface Element extends VisitorObject<ClassElement, Element, ParameterElements> {
    Element EMPTY = of("EMPTY");

    Object getElement();

    default @NotNull Class<?> getElementClass() {
        return getElement().getClass();
    }

    @Override
    default boolean isPrimitive() {
        return false;
    }

    @Override
    default boolean isNull() {
        return false;
    }

    @Override
    default <T extends VisitorObject<ClassElement, Element, ParameterElements>> @NotNull T check(@NotNull Class<T> clazz) {
        if (clazz.isInstance(this)) return (T) this;
        else throw new HandlerException("%s is not an instance of %s", this, clazz.getCanonicalName());
    }

    @Override
    default @NotNull ClassElement checkClass() {
        if (this instanceof ClassElement) return (ClassElement) this;
        else throw new HandlerException("%s is not a %s", this, ClassElement.class.getSimpleName());
    }

    @Override
    default @NotNull FieldContainer<ClassElement, Element, ParameterElements> getField(@NotNull Field field) throws VisitorObjectException {
        Refl<?> refl = new Refl<>(getElement());
        ClassElement classElement = ClassElement.of(field.getType());
        Element value = Element.of(refl.getFieldObject(field));
        return new ElementFieldContainer(this, classElement, field.getName(), value);
    }

    @Override
    default @NotNull Element invokeMethod(@NotNull ExecutableContainer<Method> method, @NotNull ParameterElements parameters) throws VisitorObjectException {
        Refl<?> refl = new Refl<>(getElement());
        Object returned = refl.invokeMethod(method.getReturnType(), method.getName(), method.getParameterTypes(),
                parameters.stream().map(Element::getElement).toArray(Object[]::new));
        return Element.of(returned);
    }

    @Override
    default @NotNull Element toPrimitive() {
        throw new HandlerException("%s is not a wrapper type", this);
    }

    @Override
    default @NotNull Element toWrapper() {
        throw new HandlerException("%s is not a primitive type", this);
    }

    static Element of(@Nullable Object object) {
        return new ElementImpl(object);
    }

    @Override
    default @NotNull VisitorObjectException fieldNotFound(@NotNull ClassElement classVisitorObject, @NotNull String field) {
        return new ElementException("Could not find field '%s' in type %s", field, classVisitorObject);
    }

    @Override
    default @NotNull VisitorObjectException methodNotFound(@NotNull ClassElement classVisitorObject, @NotNull String method,
                                                           @NotNull ParameterElements parameters) {
        return new ElementException("Could not find method %s(%s) in type %s", method, parameters, classVisitorObject);
    }

    @Override
    default @NotNull VisitorObjectException typesMismatch(@NotNull ClassElement classVisitorObject, @NotNull Executable method,
                                                          @NotNull ParameterElements parameters) {
        return new ElementException("Types mismatch: cannot apply parameters %s to method %s%s in type %s",
                parameters, method.getName(), Arrays.toString(method.getParameterTypes()), classVisitorObject);
    }

    @Override
    default @NotNull RuntimeException noClassType(@NotNull Class<?> type) {
        return new HandlerException("%s does not have a class", type.getCanonicalName());
    }

    default @NotNull RuntimeException unsupportedOperation(@NotNull TokenType operator,
                                                           @NotNull VisitorObject<ClassElement, Element, ParameterElements> left,
                                                           @NotNull VisitorObject<ClassElement, Element, ParameterElements> right) {
        return new HandlerException("Operator '%s' cannot be applied to '%s', '%s'", operator, left, right);
    }

    default @NotNull RuntimeException unsupportedOperation(@NotNull TokenType operator,
                                                           @NotNull VisitorObject<ClassElement, Element, ParameterElements> operand) {
        return new HandlerException("Operator '%s' cannot be applied to '%s'", operator, operand);
    }

    @Override
    default @NotNull Element and(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.AND, this, other));
    }

    @Override
    default @NotNull Element or(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.OR, this, other));
    }

    @Override
    default @NotNull Element equal(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.EQUAL, this, other));
    }

    @Override
    default @NotNull Element notEqual(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.NOT_EQUAL, this, other));
    }

    @Override
    default @NotNull Element lessThan(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.LESS_THAN, this, other));
    }

    @Override
    default @NotNull Element lessThanEqual(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.LESS_THAN_EQUAL, this, other));
    }

    @Override
    default @NotNull Element greaterThan(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.GREATER_THAN, this, other));
    }

    @Override
    default @NotNull Element greaterThanEqual(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.GREATER_THAN_EQUAL, this, other));
    }


    @Override
    default @NotNull Element bitAnd(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.BIT_AND, this, other));
    }

    @Override
    default @NotNull Element bitOr(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.BIT_OR, this, other));
    }

    @Override
    default @NotNull Element bitXor(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.BIT_XOR, this, other));
    }

    @Override
    default @NotNull Element lshift(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.LSHIFT, this, other));
    }

    @Override
    default @NotNull Element rshift(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.RSHIFT, this, other));
    }

    @Override
    default @NotNull Element urshift(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.URSHIFT, this, other));
    }


    @Override
    default @NotNull Element add(final @NotNull Element other) {
        Double first = Double.valueOf(getElement().toString());
        Double second = Double.valueOf(other.getElement().toString());
        return Element.of(first + second);
    }

    @Override
    default @NotNull Element subtract(final @NotNull Element other) {
        Double first = Double.valueOf(getElement().toString());
        Double second = Double.valueOf(other.getElement().toString());
        return Element.of(first - second);
    }

    @Override
    default @NotNull Element multiply(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.MULTIPLY, this, other));
    }

    @Override
    default @NotNull Element divide(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.DIVIDE, this, other));
    }

    @Override
    default @NotNull Element modulo(final @NotNull Element other) {
        return of(unsupportedOperation(TokenType.MODULO, this, other));
    }

    @Override
    default @NotNull Element minus() {
        return of(unsupportedOperation(TokenType.SUBTRACT, this));
    }

    @Override
    default @NotNull Element not() {
        return of(unsupportedOperation(TokenType.NOT, this));
    }

}
