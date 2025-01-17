package it.fulminazzo.mojito.parser.node.arrays

import it.fulminazzo.mojito.parser.node.literals.Literal
import it.fulminazzo.mojito.parser.node.values.NumberValueLiteral
import spock.lang.Specification

class StaticArrayTest extends Specification {

    def 'test size method'() {
        given:
        def array = new StaticArray(Literal.of('int'), new NumberValueLiteral('3'))

        when:
        def size = array.size()

        then:
        size == 3
    }

}
