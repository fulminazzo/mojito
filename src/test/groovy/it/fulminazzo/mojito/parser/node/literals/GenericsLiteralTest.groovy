package it.fulminazzo.mojito.parser.node.literals

import spock.lang.Specification

class GenericsLiteralTest extends Specification {

    def 'test that toString prints internal types'() {
        given:
        def literal = new GenericsLiteral('List', [
                new LiteralImpl('String')
        ])

        when:
        def output = literal.toString()

        then:
        output == 'GenericsLiteral(List, [Literal(String)])'
    }

}
