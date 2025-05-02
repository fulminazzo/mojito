package it.fulminazzo.mojito.environment;

import it.fulminazzo.mojito.environment.scopetypes.ScopeType;
import it.fulminazzo.mojito.environment.scopetypes.TryScopeType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Optional;

/**
 * Represents a container for {@link Scope}s.
 *
 * @param <T> the type of the values
 */
public class Environment<T> implements Scoped<T> {
    private final @NotNull LinkedList<Scope<T>> scopes;

    /**
     * Instantiates a new Environment.
     */
    public Environment() {
        this.scopes = new LinkedList<>();
        enterScope(ScopeType.MAIN);
    }

    /**
     * Checks whether one of the {@link #scopes} is of type {@link TryScopeType}.
     * If it is, verifies that one of its exceptions is extended by the provided exception.
     *
     * @param exception the exception
     * @return true only if a match is found
     */
    public boolean isInTryScope(final @NotNull Class<? extends Throwable> exception) {
        for (Scope<T> scope : scopes) {
            ScopeType type = scope.scopeType();
            if (type instanceof TryScopeType) {
                TryScopeType tryType = (TryScopeType) type;
                for (Class<Throwable> caught : tryType.getCaughtExceptions())
                    if (caught.isAssignableFrom(exception))
                        return true;
            }
        }
        return false;
    }

    /**
     * Enters a new {@link Scope} with the given {@link ScopeType} as type.
     *
     * @param scopeType the scope type
     * @return this environment
     */
    public @NotNull Environment<T> enterScope(final @NotNull ScopeType scopeType) {
        this.scopes.addFirst(new Scope<>(scopeType));
        return this;
    }

    /**
     * Exit the latest {@link Scope}.
     * If one is remaining, throws an {@link IllegalStateException}.
     *
     * @return this environment
     */
    public @NotNull Environment<T> exitScope() {
        if (isMainScope()) throw new IllegalStateException("Cannot exit from main scope");
        this.scopes.removeFirst();
        return this;
    }

    /**
     * Checks whether the current scope is the main one.
     *
     * @return true if it is
     */
    public boolean isMainScope() {
        return scopeType() == ScopeType.MAIN;
    }

    @Override
    public @NotNull Optional<T> search(@NotNull final NamedEntity name) {
        return this.scopes.stream()
                .map(s -> s.search(name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public @NotNull Info lookupInfo(@NotNull NamedEntity name) throws ScopeException {
        for (Scope<T> scope : this.scopes)
            try {
                return scope.lookupInfo(name);
            } catch (ScopeException ignored) {
            }
        throw ScopeException.noSuchVariable(name);
    }

    @Override
    public void markConstant(@NotNull NamedEntity name) throws ScopeException {
        for (Scope<T> scope : this.scopes)
            try {
                scope.markConstant(name);
                return;
            } catch (ScopeException ignored) {
            }
        throw ScopeException.noSuchVariable(name);
    }

    @Override
    public void declare(@NotNull Info info, @NotNull NamedEntity name, @NotNull T value) throws ScopeException {
        if (isDeclared(name)) throw ScopeException.alreadyDeclaredVariable(name);
        else lastScope().declare(info, name, value);
    }

    @Override
    public void update(@NotNull NamedEntity name, @NotNull T value) throws ScopeException {
        for (Scope<T> scope : this.scopes)
            if (scope.isDeclared(name)) {
                scope.update(name, value);
                return;
            }
        throw ScopeException.noSuchVariable(name);
    }

    @Override
    public @NotNull ScopeType scopeType() {
        return lastScope().scopeType();
    }

    @Override
    public @NotNull Environment<T> check(final ScopeType @NotNull ... scopeTypes) throws ScopeException {
        for (Scope<T> scope : this.scopes)
            try {
                scope.check(scopeTypes);
                return this;
            } catch (ScopeException ignored) {
            }
        throw ScopeException.scopeTypeMismatch(scopeTypes);
    }

    /**
     * Returns the last declared {@link Scope}.
     *
     * @return the scope
     */
    @NotNull Scope<T> lastScope() {
        return this.scopes.getFirst();
    }

}
