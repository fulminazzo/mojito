package it.fulminazzo.mojito.typechecker.types.objects

import it.fulminazzo.mojito.typechecker.types.ClassType
import it.fulminazzo.mojito.typechecker.types.objects.generics.GenericsObjectClassType
import it.fulminazzo.mojito.typechecker.types.objects.generics.GenericsObjectType
import spock.lang.Specification

class GenericsObjectClassTypeTest extends Specification {

    def 'test equals method'() {
        given:
        def first = new GenericsObjectClassType(
                ObjectType.of('List'),
                [ObjectClassType.STRING]
        )

        and:
        def second = new GenericsObjectClassType(
                ObjectType.of('List'),
                [ObjectClassType.STRING]
        )

        expect:
        first == second
    }

    def 'test #second should not equal'() {
        given:
        def first = new GenericsObjectClassType(
                ObjectType.of('List'),
                [ObjectClassType.STRING]
        )

        expect:
        first != second

        where:
        second << [
                new GenericsObjectClassType(
                        ObjectType.of('List'),
                        [ObjectClassType.INTEGER]
                ),
                new GenericsObjectClassType(
                        ObjectType.of('Map'),
                        [ObjectClassType.STRING, ObjectClassType.INTEGER]
                ),
                ObjectClassType.of('List')
        ]
    }

    def 'test hashCode correctly works'() {
        given:
        def type = new GenericsObjectClassType(ObjectType.of(List), [ClassType.of(String)])

        when:
        def code = type.hashCode()

        then:
        code == Objects.hash(
                GenericsObjectClassType.hashCode(),
                List,
                ['E': ClassType.of(String)]
        )
    }

    def 'test toString method'() {
        given:
        def type = ObjectType.of(List)

        and:
        def genericTypes = [ObjectClassType.STRING]

        when:
        def classType = new GenericsObjectClassType(type, genericTypes)

        and:
        def output = classType.toString()

        then:
        output == 'List<String.class>.class'
    }

}
