package it.fulminazzo.mojito.parser.node;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an {@link Assignment} of a constant.
 */
public class FinalAssignment extends NodeImpl {
    private final @NotNull Assignment assignment;

    public FinalAssignment(@NotNull Assignment assignment) {
        this.assignment = assignment;
    }

}
