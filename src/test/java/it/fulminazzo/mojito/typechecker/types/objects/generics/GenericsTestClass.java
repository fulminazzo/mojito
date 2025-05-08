package it.fulminazzo.mojito.typechecker.types.objects.generics;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

@Getter
@Setter
class GenericsTestClass<F extends Number & Comparator<String>> {

    private F first;

}
