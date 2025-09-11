package it.fulminazzo.mojito.typechecker.types;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class MockClassType implements ClassType {

    @Override
    public @NotNull Type toType() {
        return new MockType();
    }

    @Override
    public @NotNull Type cast(@NotNull Type object) {
        return new MockType();
    }

    @Override
    public boolean compatibleWith(@NotNull Type object) {
        return object instanceof MockType;
    }

    @Override
    public @NotNull Class<?> toJavaClass() {
        return getClass();
    }

    public void accept(Runnable function) {
    }

    public void accept(Consumer<Integer> function) {
    }

    public void accept(Function<Integer, Integer> function) {
    }

    public void accept(BiConsumer<Integer, Integer> function) {
    }

    public void accept(BiFunction<Integer, Integer, Integer> function) {
    }

}
