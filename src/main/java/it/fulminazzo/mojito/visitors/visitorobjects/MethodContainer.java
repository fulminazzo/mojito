package it.fulminazzo.mojito.visitors.visitorobjects;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds critical information about a method.
 */
public interface MethodContainer {

    /**
     * Gets the method modifiers.
     *
     * @return the modifiers
     */
    int getModifiers();

    /**
     * Gets the method return type.
     *
     * @return the return type
     */
    @NotNull Class<?> getReturnType();

    /**
     * Gets the method name.
     *
     * @return the name
     */
    @NotNull String getName();

    /**
     * Get the method parameter types.
     *
     * @return an array containing the classes of the parameters
     */
    @NotNull Class<?>[] getParameterTypes();

    /**
     * Checks if the method accepts variable arguments.
     *
     * @return true if it does
     */
    boolean isVarArgs();

    /**
     * Gets the actual method contained in this method.
     *
     * @return the method
     */
    @NotNull Method getActualMethod();

    /**
     * Gets an instance of a {@link MethodContainer} from the given method.
     *
     * @param method the method
     * @return the method container
     */
    static @NotNull MethodContainer of(final @NotNull Method method) {
        return new MethodContainerImpl(method);
    }

    /**
     * Gets an instance of a {@link MethodContainer} from the given method.
     * It supports generics types, thanks to the given container.
     *
     * @param method            the method
     * @param genericsContainer the generics container
     * @return the method container
     */
    static @NotNull MethodContainer of(final @NotNull Method method,
                                       final @NotNull GenericsContainer<?> genericsContainer) {
        return new GenericsMethodContainerImpl<>(method, genericsContainer);
    }

    /**
     * An implementation of {@link MethodContainer}.
     */
    @Getter
    class MethodContainerImpl implements MethodContainer {
        private final @NotNull Method actualMethod;

        /**
         * Instantiates a new Method container.
         *
         * @param method the method to create from
         */
        public MethodContainerImpl(final @NotNull Method method) {
            this.actualMethod = method;
        }

        @Override
        public int getModifiers() {
            return this.actualMethod.getModifiers();
        }

        @Override
        public @NotNull Class<?> getReturnType() {
            return this.actualMethod.getReturnType();
        }

        @Override
        public @NotNull String getName() {
            return this.actualMethod.getName();
        }

        @Override
        public @NotNull Class<?>[] getParameterTypes() {
            return this.actualMethod.getParameterTypes();
        }

        @Override
        public boolean isVarArgs() {
            return this.actualMethod.isVarArgs();
        }

    }

    /**
     * An implementation of {@link MethodContainer} that supports generic types.
     *
     * @param <C> the type of the parameterized types
     */
    @Getter
    class GenericsMethodContainerImpl<C extends ClassVisitorObject<C, ?, ?>> extends MethodContainerImpl {
        private final @NotNull Class<?> returnType;
        private final @NotNull Class<?>[] parameterTypes;

        /**
         * Instantiates a new Generics method container.
         *
         * @param method            the method
         * @param genericsContainer the generics container to get the parameters from
         */
        public GenericsMethodContainerImpl(@NotNull Method method,
                                           @NotNull GenericsContainer<C> genericsContainer) {
            super(method);
            final Map<String, C> types = new LinkedHashMap<>(genericsContainer.getGenericTypes());
            Arrays.stream(method.getTypeParameters())
                    .map(Type::getTypeName)
                    .forEach(types::remove);

            Type returnType = method.getGenericReturnType();
            String returnName = returnType.getTypeName().replace("[]", "");
            if (!(returnType instanceof Class) && types.containsKey(returnName))
                this.returnType = getClassFromType(types, returnType.getTypeName());
            else this.returnType = method.getReturnType();

            this.parameterTypes = method.getParameterTypes();
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            for (int i = 0; i < genericParameterTypes.length; i++) {
                Type parameterType = genericParameterTypes[i];
                String parameterName = parameterType.getTypeName().replace("[]", "");
                if (!(parameterType instanceof Class<?>) && types.containsKey(parameterName))
                    this.parameterTypes[i] = getClassFromType(types, parameterType.getTypeName());
            }
        }

        private static <C extends ClassVisitorObject<C, ?, ?>> @NotNull Class<?> getClassFromType(
                final @NotNull Map<String, C> types,
                final @NotNull String typeName
        ) {
            if (typeName.contains("[]")) {
                Class<?> typeClass = getClassFromType(types, typeName.substring(0, typeName.indexOf("[]")));
                return Array.newInstance(typeClass, 0).getClass();
            } else return types.get(typeName).toJavaClass();
        }

    }

}
