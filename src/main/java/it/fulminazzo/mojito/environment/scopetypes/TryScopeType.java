package it.fulminazzo.mojito.environment.scopetypes;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a TRY {@link ScopeType}.
 * This special type allows for multiple {@link Throwable}s to be
 * passed in order to verify that the current scope matches with
 * the caught exceptions.
 */
@Getter
public final class TryScopeType implements ScopeType {
    private final @NotNull Set<Class<Throwable>> caughtExceptions;

    /**
     * Instantiates a new Try scope type.
     */
    TryScopeType() {
        this.caughtExceptions = new LinkedHashSet<>();
    }

    /**
     * Instantiates a new Try scope type.
     *
     * @param caughtExceptions the caught exceptions
     */
    TryScopeType(final @NotNull Stream<Class<Throwable>> caughtExceptions) {
        this.caughtExceptions = caughtExceptions.collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public @NotNull String name() {
        return String.format("TRY(%s)", this.caughtExceptions.stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(", ")));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TryScopeType && this.caughtExceptions.equals(((TryScopeType) o).caughtExceptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), this.caughtExceptions);
    }

    @Override
    public @NotNull String toString() {
        return name();
    }

}
