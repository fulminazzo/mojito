package it.fulminazzo.mojito.typechecker.types.objects.generics

import it.fulminazzo.mojito.typechecker.types.ClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType
import it.fulminazzo.mojito.visitors.visitorobjects.VisitorObject
import spock.lang.Specification

class GenericsObjectTypeTest extends Specification {

    def 'test that getField returns correct generic type'() {
        given:
        def fieldType = ClassType.of(GenericsTestClass.MockTestClass)
        def classType = new GenericsObjectType(GenericsTestClass,
                [fieldType]
        )

        when:
        def container = classType.getField('first')

        then:
        container.type == fieldType
        container.variable == fieldType.toType()
    }

    def 'test that getField returns correct normal type'() {
        given:
        def fieldType = ClassType.of(Integer)
        def classType = new GenericsObjectType(GenericsTestClass,
                [ClassType.of(GenericsTestClass.MockTestClass)]
        )

        when:
        def container = classType.getField('second')

        then:
        container.type == ClassType.of(Integer)
        container.variable == fieldType.toType()
    }

    def 'test that generic type is other type'() {
        given:
        def first = new GenericsObjectType(List, [ClassType.of(String)])
        def second = new GenericsObjectType(List, [ClassType.of(String)])

        expect:
        GenericsObjectType.getMethod('is', VisitorObject[]).invoke(first, new Object[]{new VisitorObject[]{second}})
    }

    def 'test equals correctly works'() {
        given:
        def first = new GenericsObjectType(List, [ClassType.of(String)])
        def second = new GenericsObjectType(List, [ClassType.of(String)])

        expect:
        first.equals(second)
    }

    def 'test that generic type is not equal to #second'() {
        given:
        def type = new GenericsObjectType(List, [ClassType.of(String)])

        expect:
        !type.equals(second)

        where:
        second << [
                null,
                ObjectType.of(List),
                new GenericsObjectType(List, [ClassType.of(Integer)]),
                new GenericsObjectType(Set, [ClassType.of(String)])
        ]
    }

    def 'test hashCode correctly works'() {
        given:
        def type = new GenericsObjectType(List, [ClassType.of(String)])

        when:
        def code = type.hashCode()

        then:
        code == Objects.hash(
                GenericsObjectType.hashCode(),
                List,
                ['E': ClassType.of(String)]
        )
    }

    def 'test toString prints the correct output'() {
        given:
        def type = new GenericsObjectType(Map, [ClassType.of(Integer), ClassType.of(String)])

        when:
        def output = type.toString()

        then:
        output == 'Type(Map<Integer.class, String.class>)'
    }

}
