package it.fulminazzo.mojito.typechecker.types.objects;

import it.fulminazzo.mojito.typechecker.types.ClassType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link ObjectClassType} with a class different from the default types.
 * It supports generic typing.
 */
class GenericsObjectClassType extends CustomObjectClassType implements ClassType {

    /**
     * Instantiates a new Generics object class type.
     *
     * @param internalType the internal type
     */
    public GenericsObjectClassType(@NotNull ObjectType internalType) {
        super(internalType);
    }


}
