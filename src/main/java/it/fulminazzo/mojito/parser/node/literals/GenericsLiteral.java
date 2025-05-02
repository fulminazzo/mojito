package it.fulminazzo.mojito.parser.node.literals;

import it.fulminazzo.mojito.parser.node.NodeException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An extension of {@link Literal} that supports generic typing.
 */
public class GenericsLiteral extends LiteralImpl implements Literal {
    private final @NotNull List<Literal> types;

    /**
     * Instantiates a new Literal.
     *
     * @param rawValue the raw value
     */
    public GenericsLiteral(@NotNull String rawValue, @NotNull List<Literal> types) throws NodeException {
        super(rawValue);
        this.types = types;
    }

}
