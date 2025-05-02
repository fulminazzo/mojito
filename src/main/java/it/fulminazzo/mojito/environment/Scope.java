package it.fulminazzo.mojito.environment;

import it.fulminazzo.mojito.environment.scopetypes.ScopeType;
import it.fulminazzo.mojito.wrappers.BiObjectWrapper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A basic scope containing name value pairs of variables.
 *
 * @param <T> the type of the value
 */
class Scope<T> implements Scoped<T> {
    private final @NotNull Map<ObjectData, T> internalMap;
    private final @NotNull ScopeType scopeType;

    /**
     * Instantiates a new Scope.
     *
     * @param scopeType the scope type
     */
    public Scope(final @NotNull ScopeType scopeType) {
        this.internalMap = new LinkedHashMap<>();
        this.scopeType = scopeType;
    }

    @Override
    public @NotNull Optional<T> search(@NotNull final NamedEntity name) {
        return getKey(name).map(this.internalMap::get);
    }

    @Override
    public @NotNull Info lookupInfo(@NotNull NamedEntity name) throws ScopeException {
        return getKey(name).map(ObjectData::getInfo).orElseThrow(() -> ScopeException.noSuchVariable(name));
    }

    @Override
    public void markConstant(@NotNull NamedEntity name) throws ScopeException {
        getKey(name).orElseThrow(() -> ScopeException.noSuchVariable(name)).markConstant();
    }

    @Override
    public void declare(@NotNull Info info, @NotNull NamedEntity name, @NotNull T value) throws ScopeException {
        if (isDeclared(name)) throw ScopeException.alreadyDeclaredVariable(name);
        else this.internalMap.put(new ObjectData(info, name), value);
    }

    @Override
    public void update(@NotNull NamedEntity name, @NotNull T value) throws ScopeException {
        ObjectData key = getKey(name).orElseThrow(() -> ScopeException.noSuchVariable(name));
        if (key.getInfo().compatibleWith(value)) this.internalMap.put(key, value);
        else throw ScopeException.cannotAssignValue(value, key.getInfo());
    }

    @Override
    public @NotNull ScopeType scopeType() {
        return this.scopeType;
    }

    /**
     * Searches the internal map for the associated {@link ObjectData} with the given name.
     *
     * @param name the name
     * @return an optional containing the data (if found)
     */
    public @NotNull Optional<ObjectData> getKey(@NotNull NamedEntity name) {
        return this.internalMap.keySet().stream().filter(d -> d.getName().equals(name.getName())).findFirst();
    }

    /**
     * Represents the information of an object.
     */
    static class ObjectData extends BiObjectWrapper<Info, String> {
        @Getter
        private boolean constant;

        /**
         * Instantiates a new Object data.
         *
         * @param info the info
         * @param name the name
         */
        public ObjectData(final @NotNull Info info, final @NotNull NamedEntity name) {
            super(info, name.getName());
        }

        /**
         * Gets info.
         *
         * @return the info
         */
        public @NotNull Info getInfo() {
            return this.first;
        }

        /**
         * Gets name.
         *
         * @return the name
         */
        public @NotNull String getName() {
            return this.second;
        }

        /**
         * Marks the current object as constant.
         * When constant, it cannot be changed.
         */
        public void markConstant() {
            this.constant = true;
        }

    }

}
