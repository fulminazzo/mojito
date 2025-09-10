package it.fulminazzo.mojito.parser.node;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a lambda expression.
 */
public class Lambda extends NodeImpl {
    private final @NotNull Node parameters;
    private final @NotNull Node code;

    /**
     * Instantiates a new Lambda.
     *
     * @param parameters the parameters
     * @param code       the code
     */
    public Lambda(final @NotNull Node parameters, final @NotNull Node code) {
        this.parameters = parameters;
        this.code = code;
    }

}
