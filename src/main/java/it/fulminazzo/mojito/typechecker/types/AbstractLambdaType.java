package it.fulminazzo.mojito.typechecker.types;

import it.fulminazzo.mojito.environment.scopetypes.ScopeType;
import it.fulminazzo.mojito.parser.node.Node;
import it.fulminazzo.mojito.typechecker.TypeChecker;
import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType;
import it.fulminazzo.mojito.typechecker.types.objects.generics.GenericsObjectClassType;
import it.fulminazzo.mojito.typechecker.types.variables.TypeLiteralVariableContainer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The following class represents a partially parsed {@link it.fulminazzo.mojito.parser.node.Lambda}
 * node by the {@link it.fulminazzo.mojito.typechecker.TypeChecker}.
 * It still requires further verification in order to be properly converted to a type.
 */
@Getter
public final class AbstractLambdaType implements Type {
    private final @NotNull TypeChecker typeChecker;
    private final @NotNull List<TypeLiteralVariableContainer> parametersNames;
    private final @NotNull Node code;

    /**
     * Instantiates a new Abstract lambda type.
     *
     * @param typeChecker     the type checker
     * @param parametersNames the parameter names
     * @param code            the code
     */
    public AbstractLambdaType(final @NotNull TypeChecker typeChecker,
                              final @NotNull List<TypeLiteralVariableContainer> parametersNames,
                              final @NotNull Node code) {
        this.typeChecker = typeChecker;
        this.parametersNames = parametersNames;
        this.code = code;
    }

    /**
     * Converts the current abstract type to an actual type, given the expected type.
     *
     * @param expectedType the expected type
     * @return the type
     */
    public @NotNull Type toActualType(final @NotNull ClassType expectedType) {
        Method functionalMethod = expectedType.getFunctionalMethod();

        int parametersCount = getParametersCount();
        if (parametersCount != functionalMethod.getParameterCount())
            throw TypeCheckerException.invalidType(expectedType, this);

        this.typeChecker.visitScoped(ScopeType.CODE_BLOCK, () -> {
            Class<?> returnType = functionalMethod.getReturnType();
            if (parametersCount > 0) {
                GenericsObjectClassType classType = expectedType.check(GenericsObjectClassType.class);
                List<ClassType> parameterTypes = new ArrayList<>(classType.getGenericTypes().values());
                List<TypeLiteralVariableContainer> parametersNames = getParametersNames();
                for (int i = 0; i < parametersCount; i++) {
                    ClassType c = parameterTypes.get(i);
                    this.typeChecker.getEnvironment().declare(c, parametersNames.get(i).namedEntity(), c.toType());
                }
                java.lang.reflect.Type genericReturnType = functionalMethod.getGenericReturnType();
                if (!genericReturnType.equals(returnType))
                    returnType = classType.getGenericTypes().get(genericReturnType.getTypeName()).toJavaClass();
            }

            Type code = getCode().accept(this.typeChecker);
            if (!returnType.equals(void.class)) code.checkAssignableFrom(ClassType.of(returnType));
            else code.check(Types.NO_TYPE, ObjectType.of(void.class));

            return code;
        });

        return expectedType.toType();
    }

    /**
     * Gets parameters count.
     *
     * @return the parameters count
     */
    public int getParametersCount() {
        return this.parametersNames.size();
    }

    @Override
    public @NotNull ClassType toClass() {
        return ObjectClassType.OBJECT;
    }

}
