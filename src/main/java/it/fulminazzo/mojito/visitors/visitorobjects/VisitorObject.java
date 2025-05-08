package it.fulminazzo.mojito.visitors.visitorobjects;

import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.fulmicollection.utils.ReflectionUtils;
import it.fulminazzo.mojito.visitors.Visitor;
import it.fulminazzo.mojito.visitors.visitorobjects.variables.FieldContainer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * The object that will be returned by a {@link Visitor} instance.
 * Provides various useful methods for the visitor itself.
 *
 * @param <C> the type of the {@link ClassVisitorObject}
 * @param <O> the type of the {@link VisitorObject}
 * @param <P> the type of the {@link ParameterVisitorObjects}
 */
@SuppressWarnings("unchecked")
public interface VisitorObject<
        C extends ClassVisitorObject<C, O, P>,
        O extends VisitorObject<C, O, P>,
        P extends ParameterVisitorObjects<C, O, P>
        > {

    /**
     * Checks whether the current object is a basic object of Java.
     *
     * @return true if it is
     */
    boolean isPrimitive();

    /**
     * Checks whether the current object is null.
     *
     * @return true if it is
     */
    boolean isNull();

    /**
     * Checks whether the current object is of the one specified.
     *
     * @param object the class of the object
     * @return true if it is
     */
    default boolean is(final @NotNull Class<?> object) {
        return object.isAssignableFrom(getClass());
    }

    /**
     * Checks whether the current object is equal to any of the ones given.
     *
     * @param objects the objects
     * @return true if it is for at least one of them
     */
    default boolean is(final O @NotNull ... objects) {
        return Arrays.asList(objects).contains(this);
    }

    /**
     * Checks whether the current object extends the given {@link ClassVisitorObject}.
     *
     * @param classVisitorObject the class object
     * @return true if it is
     */
    default boolean isAssignableFrom(final @NotNull ClassVisitorObject<C, O, P> classVisitorObject) {
        return classVisitorObject.compatibleWith(this);
    }

    /**
     * Checks that the current object is an instance of the specified one.
     *
     * @param <T>   the type of the class
     * @param clazz the class
     * @return this object cast to the given class
     */
    <T extends VisitorObject<C, O, P>> @NotNull T check(final @NotNull Class<T> clazz);

    /**
     * Checks that the current object is {@link ClassVisitorObject}.
     *
     * @return the current object cast to the expected one
     */
    @NotNull C checkClass();

    /**
     * Gets the given field from the associated {@link ClassVisitorObject} and returns it.
     *
     * @param fieldName the field name
     * @return a {@link FieldContainer} containing the {@link ClassVisitorObject} and {@link VisitorObject} of the field
     * @throws VisitorObjectException the exception thrown in case of errors
     */
    default @NotNull FieldContainer<C, O, P> getField(final @NotNull String fieldName) throws VisitorObjectException {
        if (isPrimitive()) return toWrapper().getField(fieldName);
        C classVisitorObject = is(ClassVisitorObject.class) ? (C) this : toClass();
        try {
            Class<?> javaClass = classVisitorObject.toJavaClass();
            Field field = ReflectionUtils.getField(javaClass, fieldName);
            return getField(field);
        } catch (IllegalArgumentException e) {
            throw fieldNotFound(classVisitorObject, fieldName);
        }
    }

    /**
     * Gets the given field from the associated {@link ClassVisitorObject} and returns it.
     *
     * @param field the field
     * @return a {@link FieldContainer} containing the {@link ClassVisitorObject} and {@link VisitorObject} of the field
     * @throws VisitorObjectException the exception thrown in case of errors
     */
    @NotNull FieldContainer<C, O, P> getField(final @NotNull Field field) throws VisitorObjectException;

    /**
     * Searches and invokes the given method from the associated {@link ClassVisitorObject} and
     * returns the value returned from it.
     *
     * @param methodName the method name
     * @param parameters the parameters
     * @return the returned object from the method
     * @throws VisitorObjectException the exception thrown in case of errors
     */
    default @NotNull O invokeMethod(final @NotNull String methodName,
                                    final @NotNull P parameters) throws VisitorObjectException {
        if (isPrimitive()) return toWrapper().invokeMethod(methodName, parameters);
        C classVisitorObject = is(ClassVisitorObject.class) ? (C) this : toClass();
        try {
            Class<?> javaClass = classVisitorObject.toJavaClass();
            // Lookup methods from name and parameters count
            @NotNull List<Method> methods = ReflectionUtils.getMethods(javaClass, m ->
                    m.getName().equals(methodName) && VisitorObjectUtils.verifyExecutable(parameters, m));
            if (methods.isEmpty()) throw new IllegalArgumentException();

            Refl<?> refl = new Refl<>(ReflectionUtils.class);
            Class<?> @NotNull [] parametersTypes = parameters.toJavaClassArray();

            for (Method method : methods) {
                // For each one, validate its parameters
                if (Boolean.TRUE.equals(refl.invokeMethod("validateParameters",
                        new Class[]{Class[].class, Class[].class, boolean.class},
                        parametersTypes, method.getParameterTypes(), method.isVarArgs())))
                    return invokeMethod(method, parameters);
            }

            throw typesMismatch(classVisitorObject, methods.get(0), parameters);
        } catch (IllegalArgumentException e) {
            throw methodNotFound(classVisitorObject, methodName, parameters);
        }
    }

    /**
     * Invokes the given method from the associated {@link ClassVisitorObject} and
     * returns the value returned from it.
     *
     * @param method     the method
     * @param parameters the parameters
     * @return the returned object from the method
     * @throws VisitorObjectException the exception thrown in case of errors
     */
    @NotNull O invokeMethod(final @NotNull Method method, final @NotNull P parameters) throws VisitorObjectException;

    /**
     * Converts the current object to its primitive associated object.
     *
     * @return the primitive object
     */
    @NotNull O toPrimitive();

    /**
     * Converts the current object to its wrapper associated object.
     *
     * @return the wrapper object
     */
    @NotNull O toWrapper();

    /**
     * Gets the class associated with the current object.
     *
     * @return the class object
     */
    @NotNull C toClass();

    /*
        EXCEPTIONS
     */

    /**
     * Generates a {@link VisitorObjectException} with message:
     * <i>Could not find field '%field%' in type %classVisitorObject%</i>
     *
     * @param classVisitorObject the associated class visitor object
     * @param field              the field
     * @return the visitor object exception
     */
    @NotNull VisitorObjectException fieldNotFound(final @NotNull C classVisitorObject,
                                                  final @NotNull String field);

    /**
     * Generates a {@link VisitorObjectException} with message:
     * <i>Could not find method %method_format% in type %classVisitorObject%</i>
     *
     * @param classVisitorObject the class object
     * @param method             the method
     * @param parameters         the parameters
     * @return the visitor object exception
     */
    @NotNull VisitorObjectException methodNotFound(final @NotNull C classVisitorObject,
                                                   final @NotNull String method,
                                                   final @NotNull P parameters);

    /**
     * Generates a {@link VisitorObjectException} with message:
     * <i>Types mismatch: cannot apply parameters %parameter_types_format% to method %method_format% in type %classVisitorObject%</i>
     *
     * @param classVisitorObject the class object
     * @param method             the method
     * @param parameters         the parameters
     * @return the visitor object exception
     */
    @NotNull VisitorObjectException typesMismatch(final @NotNull C classVisitorObject,
                                                  final @NotNull Executable method,
                                                  final @NotNull P parameters);

    /**
     * Generates a {@link RuntimeException} with message:
     * <i>%clazz% does not have a class</i>
     *
     * @param type the type
     * @return the type runtime exception
     */
    @NotNull RuntimeException noClassType(final @NotNull Class<?> type);
    
    /*
        BINARY COMPARISONS
     */

    /**
     * Executes and comparison.
     *
     * @param other the other object
     * @return a boolean object
     */
    @NotNull O and(@NotNull O other);

    /**
     * Executes or comparison.
     *
     * @param other the other object
     * @return a boolean object
     */
    @NotNull O or(@NotNull O other);

    /**
     * Executes equal comparison.
     *
     * @param other the other object
     * @return a boolean object
     */
    @NotNull O equal(@NotNull O other);

    /**
     * Executes not equal comparison.
     *
     * @param other the other object
     * @return a boolean object
     */
    @NotNull O notEqual(@NotNull O other);

    /**
     * Executes less than comparison.
     *
     * @param other the other object
     * @return a boolean object
     */
    @NotNull O lessThan(@NotNull O other);

    /**
     * Executes less than equal comparison.
     *
     * @param other the other object
     * @return a boolean object
     */
    @NotNull O lessThanEqual(@NotNull O other);

    /**
     * Executes greater than comparison.
     *
     * @param other the other object
     * @return a boolean object
     */
    @NotNull O greaterThan(@NotNull O other);

    /**
     * Executes greater than equal comparison.
     *
     * @param other the other object
     * @return a boolean object
     */
    @NotNull O greaterThanEqual(@NotNull O other);

    /*
        BINARY OPERATIONS
     */

    /**
     * Executes bit and operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O bitAnd(@NotNull O other);

    /**
     * Executes bit or operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O bitOr(@NotNull O other);

    /**
     * Executes bit xor operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O bitXor(@NotNull O other);

    /**
     * Executes lshift operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O lshift(@NotNull O other);

    /**
     * Executes rshift operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O rshift(@NotNull O other);

    /**
     * Executes urshift operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O urshift(@NotNull O other);


    /**
     * Executes add operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O add(@NotNull O other);

    /**
     * Executes subtract operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O subtract(@NotNull O other);

    /**
     * Executes multiply operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O multiply(@NotNull O other);

    /**
     * Executes divide operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O divide(@NotNull O other);

    /**
     * Executes modulo operation.
     *
     * @param other the other object
     * @return the resulting object
     */
    @NotNull O modulo(@NotNull O other);

    /**
     * Executes minus operation.
     *
     * @return a boolean object
     */
    @NotNull O minus();

    /**
     * Executes not operation.
     *
     * @return a boolean object
     */
    @NotNull O not();

}
