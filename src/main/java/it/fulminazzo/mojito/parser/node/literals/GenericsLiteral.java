package it.fulminazzo.mojito.parser.node.literals;

import it.fulminazzo.mojito.parser.node.NodeException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An extension of {@link Literal} that supports generic typing.
 */
@Getter
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

    @Override
    public @NotNull String toString() {
        return String.format("%s(%s, [%s])",
                getClass().getSimpleName(),
                getLiteral(),
                this.types.stream().map(Object::toString).collect(Collectors.joining(", "))
        );
    }

}
