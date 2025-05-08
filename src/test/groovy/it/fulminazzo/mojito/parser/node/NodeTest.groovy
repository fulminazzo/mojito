package it.fulminazzo.mojito.parser.node

import spock.lang.Specification

class NodeTest extends Specification {

    def 'test hashCode'() {
        given:
        def node = new MockNode('MockNode', 1)

        expect:
        node.hashCode() == Objects.hash('MockNode', 1)
    }

    def 'test equals'() {
        given:
        def node = new MockNode('MockNode', 1)
        def other = new MockNode('MockNode', 1)

        expect:
        node == other
    }

    @SuppressWarnings('ChangeToOperator')
    def 'test not equals'() {
        given:
        def node = new MockNode('MockNode', 1)

        expect:
        // Necessary for 100% coverage
        !node.equals(other)

        where:
        other << [
                new MockNode('MockNode', 2),
                'Invalid object',
                new NodeImpl() { },
                null,
        ]
    }

    def 'test toString'() {
        given:
        def node = new MockNode('MockNode', 1)

        expect:
        node.toString() == 'MockNode(MockNode, 1)'
    }

    def 'test toString null'() {
        given:
        def node = new MockNode(null, 1)

        expect:
        node.toString() == 'MockNode(null, 1)'
    }

}
