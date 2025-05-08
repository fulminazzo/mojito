package it.fulminazzo.mojito.typechecker.types.objects.generics;

import it.fulminazzo.mojito.typechecker.types.ClassType;
import it.fulminazzo.mojito.typechecker.types.Type;
import it.fulminazzo.mojito.typechecker.types.TypeException;
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType;
import it.fulminazzo.mojito.typechecker.types.variables.TypeFieldContainer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A special {@link Type} that supports generic types.
 * This allows to keep track of the defined class per each type
 * using an internal map and to avoid incompatibilities.
 */
@Getter
public class GenericsObjectType extends ObjectType implements Type {
    private final Map<String, ClassType> genericTypes;

    /**
     * Instantiates a new Generics object type.
     *
     * @param clazz        the class
     * @param genericTypes the generic types
     */
    public GenericsObjectType(final @NotNull Class<?> clazz,
                              final @NotNull Collection<ClassType> genericTypes) {
        super(clazz);
        this.genericTypes = GenericsUtils.genericTypesToMap(clazz, genericTypes);
    }

    @Override
    public @NotNull TypeFieldContainer getField(@NotNull Field field) throws TypeException {
        TypeFieldContainer fieldContainer = super.getField(field);
        java.lang.reflect.Type fieldType = field.getGenericType();
        if (fieldType instanceof Class<?>)
            return fieldContainer;
        ClassType actualFieldType = this.genericTypes.get(fieldType.getTypeName());
        return new TypeFieldContainer(
                this,
                actualFieldType,
                fieldContainer.getName(),
                actualFieldType.toType());
    }

    @Override
    public @NotNull ClassType toClass() {
        return new GenericsObjectClassType(this, this.genericTypes.values());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass().hashCode(), this.object, this.genericTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GenericsObjectType) {
            GenericsObjectType other = (GenericsObjectType) o;
            return this.object.equals(other.object) && this.genericTypes.equals(other.genericTypes);
        }
        return false;
    }

    @Override
    public @NotNull String toString() {
        return Type.print(ObjectType.getClassName(this.object) + String.format("<%s>",
                this.genericTypes.values().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
        ));
    }

}
