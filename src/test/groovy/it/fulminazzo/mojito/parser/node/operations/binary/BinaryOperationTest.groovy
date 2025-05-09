package it.fulminazzo.mojito.parser.node.operations.binary

import it.fulminazzo.mojito.parser.node.literals.Literal
import it.fulminazzo.mojito.parser.node.values.NumberValueLiteral
import spock.lang.Specification

class BinaryOperationTest extends Specification {

    def 'test array index literal'() {
        given:
        def array = Literal.of('arr')
        def index = new NumberValueLiteral('1')

        and:
        def arrayIndex = new ArrayIndex(array, index)
        def expected = "${array}[${index}]"

        when:
        def literal = arrayIndex.literal

        then:
        literal == expected
    }

    def 'test field literal'() {
        given:
        def object = Literal.of('object')
        def fieldName = Literal.of('field')

        and:
        def field = new Field(object, fieldName)
        def expected = "${object}.${fieldName.literal}"

        when:
        def literal = field.literal

        then:
        literal == expected
    }

}
