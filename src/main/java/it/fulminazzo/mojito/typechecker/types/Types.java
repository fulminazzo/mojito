package it.fulminazzo.mojito.typechecker.types;

import it.fulminazzo.mojito.typechecker.TypeCheckerException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A collection of static immutable {@link Type}s.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Types {
    public static final @NotNull Type NO_TYPE = new SingletonType("NONE");
    public static final @NotNull Type NULL_TYPE = new SingletonType("nulltype");

    /**
     * Represents a special type of {@link Type} that appears as a singleton.
     */
    static class SingletonType implements Type, ClassType {
        private final @NotNull String typeName;

        /**
         * Instantiates a new Singleton type.
         *
         * @param typeName the type name
         */
        public SingletonType(final @NotNull String typeName) {
            this.typeName = typeName;
        }

        @Override
        public @NotNull Type cast(@NotNull Type object) {
            return this;
        }

        @Override
        public boolean compatibleWith(@NotNull Type object) {
            return equals(object);
        }

        @Override
        public @NotNull Class<?> toJavaClass() {
            throw TypeCheckerException.noClassType(getClass());
        }

        @Override
        public @NotNull Type toType() {
            return this;
        }

        @Override
        public @NotNull ClassType toClass() {
            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(SingletonType.class, this.typeName);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SingletonType && this.typeName.equals(((SingletonType) o).typeName);
        }

        @Override
        public @NotNull String toString() {
            return this.typeName;
        }

    }

}
