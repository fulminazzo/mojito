package it.fulminazzo.mojito.parser.node.literals

import spock.lang.Specification

class GenericsLiteralTest extends Specification {

    def 'test toLiteral'() {
        given:
        def literal = new GenericsLiteral('List', [
                new LiteralImpl('String')
        ])

        when:
        def newLiteral = literal.toLiteral()

        then:
        newLiteral == new LiteralImpl('List')
    }

    def 'test toLiteral exception for JaCoCo coverage'() {
        given:
        def literal = Spy(GenericsLiteral, constructorArgs: ['List', []])
        literal.getLiteral() >> '!?.'

        when:
        literal.toLiteral()

        then:
        thrown(IllegalStateException)
    }

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
