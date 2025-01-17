package it.fulminazzo.mojito.typechecker.types;

import org.jetbrains.annotations.NotNull;

public class MockType implements Type {

    @Override
    public @NotNull ClassType toClass() {
        return new MockClassType();
    }

}
