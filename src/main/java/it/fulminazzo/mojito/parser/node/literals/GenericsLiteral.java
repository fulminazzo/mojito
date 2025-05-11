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
     * Instantiates a new Generics Literal.
     *
     * @param rawValue the raw value
     * @param types    the types
     * @throws NodeException the node exception
     */
    public GenericsLiteral(@NotNull String rawValue, @NotNull List<Literal> types) throws NodeException {
        super(rawValue);
        this.types = types;
    }

    /**
     * Converts the current literal to a generic {@link Literal}.
     *
     * @return literal
     */
    public @NotNull Literal toLiteral() {
        try {
            return Literal.of(getLiteral());
        } catch (NodeException e) {
            throw new IllegalStateException("Unreachable code");
        }
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
