package it.fulminazzo.mojito.parser.node;

import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.mojito.visitors.Visitor;
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the most basic implementation of {@link Node}.
 */
public abstract class NodeImpl implements Node {
    private static final long serialVersionUID = 101010L;

    @Override
    public <T extends VisitorObject<?, T, ?>> T accept(final @NotNull Visitor<?, T, ?> visitor) {
        String methodName = "visit" + getClass().getSimpleName();
        Refl<?> node = new Refl<>(this);
        return new Refl<>(visitor).invokeMethod(methodName,
                node.getNonStaticFields().stream()
                        .map(node::getFieldObject)
                        .toArray(Object[]::new)
        );
    }

    @Override
    public boolean is(final @NotNull Class<? extends Node> nodeType) {
        return nodeType.isInstance(this);
    }

    @Override
    public int hashCode() {
        Refl<?> refl = new Refl<>(this);
        return Objects.hash(refl.getNonStaticFields().stream()
                .map(refl::getFieldObject)
                .filter(Objects::nonNull)
                .toArray());
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (o != null && getClass() == o.getClass()) {
            Refl<?> refl = new Refl<>(this);
            Refl<?> other = new Refl<>(o);
            for (final Field field : refl.getNonStaticFields())
                if (!Objects.equals(refl.getFieldObject(field), other.getFieldObject(field)))
                    return false;
            return true;
        }
        return false;
    }

    @Override
    public @NotNull String toString() {
        return print();
    }

    private @NotNull String print() {
        Refl<?> refl = new Refl<>(this);
        return getClass().getSimpleName() + "(" + refl.getNonStaticFields().stream()
                .map(refl::getFieldObject)
                .map(o -> o == null ? "null" : o.toString())
                .map(Object::toString)
                .collect(Collectors.joining(", ")) + ")";
    }

    /**
     * Fixes the current object {@link #toString()} method
     * by removing brackets deriving from internal lists.
     *
     * @return the output
     */
    protected @NotNull String parseSingleListClassPrint() {
        String output = print();
        final String className = getClass().getSimpleName();
        output = output.substring(className.length() + 2);
        output = output.substring(0, output.length() - 2);
        return String.format("%s(%s)", className, output);
    }

}
