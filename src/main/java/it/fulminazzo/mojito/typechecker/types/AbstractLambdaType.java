package it.fulminazzo.mojito.typechecker.types;

import it.fulminazzo.mojito.parser.node.Node;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * The following class represents a partially parsed {@link it.fulminazzo.mojito.parser.node.Lambda}
 * node by the {@link it.fulminazzo.mojito.typechecker.TypeChecker}.
 * It still requires further verification in order to be properly converted to a type.
 */
@Getter
public final class AbstractLambdaType implements Type {
    private final int parametersCount;
    private final @NotNull Node code;

    /**
     * Instantiates a new Abstract lambda type.
     *
     * @param parametersCount the parameters count
     * @param code            the code
     */
    public AbstractLambdaType(int parametersCount, final @NotNull Node code) {
        this.parametersCount = parametersCount;
        this.code = code;
    }

    @Override
    public @NotNull ClassType toClass() {
        throw new UnsupportedOperationException();
    }

}
