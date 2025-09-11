package it.fulminazzo.mojito.typechecker.types;

import it.fulminazzo.mojito.parser.node.Node;
import it.fulminazzo.mojito.typechecker.types.variables.TypeLiteralVariableContainer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The following class represents a partially parsed {@link it.fulminazzo.mojito.parser.node.Lambda}
 * node by the {@link it.fulminazzo.mojito.typechecker.TypeChecker}.
 * It still requires further verification in order to be properly converted to a type.
 */
@Getter
public final class AbstractLambdaType implements Type {
    private final @NotNull List<TypeLiteralVariableContainer> parametersNames;
    private final @NotNull Node code;

    /**
     * Instantiates a new Abstract lambda type.
     *
     * @param parametersNames the parameter names
     * @param code            the code
     */
    public AbstractLambdaType(final @NotNull List<TypeLiteralVariableContainer> parametersNames,
                              final @NotNull Node code) {
        this.parametersNames = parametersNames;
        this.code = code;
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
        throw new UnsupportedOperationException();
    }

}
