package it.fulminazzo.mojito.typechecker.types.objects.generics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class GenericsTestClass<F extends GenericsTestClass.Numeric & GenericsTestClass.Decimal> {

    public F first;
    public Integer second;

    interface Numeric {

    }

    interface Decimal {

    }

    static class MockTestClass implements Numeric, Decimal {

    }

}
