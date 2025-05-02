package it.fulminazzo.mojito.environment;

import it.fulminazzo.mojito.environment.scopetypes.ScopeType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MockEnvironment<T> extends Environment<T> {
    private final List<ScopeType> enteredScopes;
    private boolean constants;

    public MockEnvironment() {
        this.enteredScopes = new LinkedList<>();
        this.constants = true;
    }

    @Override
    public @NotNull Environment<T> enterScope(@NotNull ScopeType scopeType) {
        // Necessary check for super() method call.
        if (this.enteredScopes != null) this.enteredScopes.add(scopeType);
        return super.enterScope(scopeType);
    }

    public boolean enteredScope(final @NotNull ScopeType scopeType) {
        return this.enteredScopes.contains(scopeType);
    }

    public @NotNull Optional<T> search(@NotNull String name) {
        return super.search(NamedEntity.of(name));
    }

    public @NotNull Info lookupInfo(@NotNull String name) throws ScopeException {
        return super.lookupInfo(NamedEntity.of(name));
    }

    @Override
    public void markConstant(@NotNull NamedEntity name) throws ScopeException {
        if (this.constants) super.markConstant(name);
        else throw ScopeException.noSuchVariable(name);
    }

    public void declare(@NotNull Info info, @NotNull String name, @NotNull T value) throws ScopeException {
        super.declare(info, NamedEntity.of(name), value);
    }

    public void update(@NotNull String name, @NotNull T value) throws ScopeException {
        super.update(NamedEntity.of(name), value);
    }

    public @NotNull T lookup(@NotNull String name) throws ScopeException {
        return super.lookup(NamedEntity.of(name));
    }

    public boolean isDeclared(@NotNull String name) {
        return super.isDeclared(NamedEntity.of(name));
    }

    public void disableConstants() {
        this.constants = false;
    }

}
