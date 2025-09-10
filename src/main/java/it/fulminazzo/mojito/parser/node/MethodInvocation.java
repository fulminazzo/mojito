package it.fulminazzo.mojito.parser.node;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the parameters invoked during a method invocation.
 */
@Getter
public class MethodInvocation extends NodeImpl {
    private final @NotNull List<Node> parameters;

    /**
     * Instantiates a new Method invocation.
     *
     * @param parameters the parameters
     */
    public MethodInvocation(final @NotNull List<Node> parameters) {
        this.parameters = parameters;
    }

    @Override
    public @NotNull String toString() {
        return parseSingleListClassPrint();
    }

}
