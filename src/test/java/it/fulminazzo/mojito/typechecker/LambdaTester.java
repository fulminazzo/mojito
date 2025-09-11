package it.fulminazzo.mojito.typechecker;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class LambdaTester {
    Runnable function1;
    Consumer<Integer> function2;
    Function<Integer, Integer> function3;
    BiConsumer<Integer, Integer> function4;
    BiFunction<Integer, Integer, Integer> function5;

    public LambdaTester() {
    }

    public LambdaTester(Runnable function) {
    }

    public LambdaTester(Consumer<Integer> function) {
    }

    public LambdaTester(Function<Integer, Integer> function) {
    }

    public LambdaTester(BiConsumer<Integer, Integer> function) {
    }

    public LambdaTester(BiFunction<Integer, Integer, Integer> function) {
    }

    public Object accept(String function) {
        return 0;
    }

    public int accept(Runnable function) {
        return 0;
    }

    public float accept(Consumer<Integer> function) {
        return 0;
    }

    public double accept(Function<Integer, Integer> function) {
        return 0;
    }

    public byte accept(BiConsumer<Integer, Integer> function) {
        return 0;
    }

    public short accept(BiFunction<Integer, Integer, Integer> function) {
        return 0;
    }

}
