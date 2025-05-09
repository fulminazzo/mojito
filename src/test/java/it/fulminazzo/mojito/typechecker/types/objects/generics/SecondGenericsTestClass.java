package it.fulminazzo.mojito.typechecker.types.objects.generics;

public class SecondGenericsTestClass<
        F extends GenericsTestClass.Numeric & GenericsTestClass.Decimal,
        S
        > extends GenericsTestClass<F> {

}
