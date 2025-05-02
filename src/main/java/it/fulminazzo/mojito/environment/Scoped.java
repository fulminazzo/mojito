package it.fulminazzo.mojito.environment;

import it.fulminazzo.mojito.environment.scopetypes.ScopeType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An object that can hold values in the form (name, value) pair.
 *
 * @param <T> the type of the values
 */
interface Scoped<T> {

    /**
     * Searches for the given variable name.
     * If none is found, returns an empty {@link Optional}.
     *
     * @param name the name
     * @return the optional containing the value
     */
    @NotNull Optional<T> search(@NotNull final NamedEntity name);

    /**
     * Checks if a variable with the given name is declared.
     *
     * @param name the name
     * @return true if it is
     */
    default boolean isDeclared(final @NotNull NamedEntity name) {
        return search(name).isPresent();
    }

    /**
     * Finds the variable with the given name and returns its {@link Info}.
     *
     * @param name the name of the variable
     * @return the info of the variable
     * @throws ScopeException thrown if the variable is not declared
     */
    @NotNull Info lookupInfo(@NotNull NamedEntity name) throws ScopeException;

    /**
     * Finds the variable with the given name and marks it as constant.
     * When marked as constant, a variable cannot be changed.
     *
     * @param name the name of the variable
     * @throws ScopeException thrown if the variable is not declared
     */
    void markConstant(@NotNull NamedEntity name) throws ScopeException;

    /**
     * Finds the variable with the given name and returns its value.
     *
     * @param name the name of the variable
     * @return the value of the variable
     * @throws ScopeException thrown if the variable is not declared
     */
    default @NotNull T lookup(final @NotNull NamedEntity name) throws ScopeException {
        return search(name).orElseThrow(() -> ScopeException.noSuchVariable(name));
    }

    /**
     * Defines a variable with the given name and value.
     *
     * @param info  the information of the value
     * @param name  the name
     * @param value the value
     * @throws ScopeException thrown if the variable is already declared
     */
    void declare(@NotNull Info info, @NotNull NamedEntity name, @NotNull T value) throws ScopeException;

    /**
     * Updates the value of a variable.
     *
     * @param name  the name of the variable
     * @param value the new value
     * @throws ScopeException thrown if the variable is not declared
     */
    void update(@NotNull NamedEntity name, @NotNull T value) throws ScopeException;

    /**
     * Returns the scope type of the current scope.
     *
     * @return the scope type
     */
    @NotNull ScopeType scopeType();

    /**
     * Checks that the current scope is equal (or inside) to one of the given {@link ScopeType}s.
     *
     * @param scopeTypes the scope types
     * @return this object
     * @throws ScopeException thrown if the current scope type does not match
     */
    default @NotNull Scoped<T> check(final ScopeType @NotNull ... scopeTypes) throws ScopeException {
        for (ScopeType scopeType : scopeTypes)
            if (scopeType.equals(scopeType())) return this;
        throw ScopeException.scopeTypeMismatch(scopeTypes);
    }

}
