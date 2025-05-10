package it.fulminazzo.mojito.parser.node.literals;

import it.fulminazzo.mojito.parser.node.NodeImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a wildcard (?) with {@link it.fulminazzo.mojito.tokenizer.TokenType#SUPER} specified.
 */
public class WildcardSuper extends NodeImpl implements Literal {
    private final @NotNull Literal classLiteral;

    /**
     * Instantiates a new Wildcard super.
     *
     * @param classLiteral the literal that represents the class
     */
    public WildcardSuper(final @NotNull Literal classLiteral) {
        this.classLiteral = classLiteral;
    }

    @Override
    public @NotNull String getLiteral() {
        return "super " + this.classLiteral.getLiteral();
    }

}
