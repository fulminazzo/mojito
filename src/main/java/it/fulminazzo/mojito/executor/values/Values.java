package it.fulminazzo.mojito.executor.values;

import it.fulminazzo.mojito.executor.ExecutorException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A collection of static immutable {@link Value}s.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Values {
    public static final @NotNull Value<?> NO_VALUE = new SingletonValue("NONE");
    public static final @NotNull Value<?> NULL_VALUE = new SingletonValue("nullvalue");

    /**
     * Represents a special value of {@link Value} that appears as a singleton.
     */
    static final class SingletonValue implements Value<Class<Object>>, ClassValue<Object> {
        private final @NotNull String valueName;

        /**
         * Instantiates a new Singleton value.
         *
         * @param valueName the value name
         */
        public SingletonValue(final @NotNull String valueName) {
            this.valueName = valueName;
        }

        @Override
        public boolean compatibleWith(@NotNull Value<?> object) {
            return equals(object);
        }

        @Override
        public Class<Object> getValue() {
            return null;
        }

        @Override
        public @NotNull ClassValue<Class<Object>> toClass() {
            throw ExecutorException.noClassValue(getClass());
        }

        @Override
        public int hashCode() {
            return Objects.hash(SingletonValue.class, this.valueName);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SingletonValue && this.valueName.equals(((SingletonValue) o).valueName);
        }

        @Override
        public @NotNull String toString() {
            return this.valueName;
        }

    }

}
