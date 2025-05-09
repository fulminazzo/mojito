package it.fulminazzo.mojito.typechecker.types.objects.generics

import it.fulminazzo.mojito.typechecker.types.ClassType
import it.fulminazzo.mojito.typechecker.types.Types
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType
import spock.lang.Specification

class GenericsObjectClassTypeTest extends Specification {

    def 'test cast of #cast to #type should return #cast'() {
        when:
        def actual = cast.cast(type)

        then:
        actual == cast.toType()

        where:
        cast                                                                             | type
        new GenericsObjectClassType(ObjectType.of(Collection), [ObjectClassType.STRING]) | new GenericsObjectType(List, [ObjectClassType.STRING])
        new GenericsObjectClassType(ObjectType.of(List), [ObjectClassType.STRING])       | new GenericsObjectType(List, [ObjectClassType.STRING])
        new GenericsObjectClassType(ObjectType.of(LinkedList), [ObjectClassType.STRING]) | new GenericsObjectType(List, [ObjectClassType.STRING])
        new GenericsObjectClassType(ObjectType.of(ArrayList), [ObjectClassType.STRING])  | new GenericsObjectType(List, [ObjectClassType.STRING])
        new GenericsObjectClassType(ObjectType.of(ArrayList), [ObjectClassType.STRING])  | Types.NULL_TYPE
    }

    def 'test that class type is compatible with #type'() {
        given:
        def classType = new GenericsObjectClassType(ObjectType.of(List),
                [ClassType.of(String)]
        )

        expect:
        classType.compatibleWith(type)

        where:
        type << [
                Types.NULL_TYPE,
                ObjectType.of(List, [ClassType.of(String)]),
                ObjectType.of(List)
        ]
    }

    def 'test that class type is not compatible with #type'() {
        given:
        def classType = new GenericsObjectClassType(ObjectType.of(List),
                [ClassType.of(String)]
        )

        expect:
        !classType.compatibleWith(type)

        where:
        type << [
                ObjectType.of(List, [ClassType.of(Integer)]),
                ObjectType.of(Set, [ClassType.of(String)]),
                {
                    GenericsObjectType type = ObjectType.of(List, [ClassType.of(Integer)])
                    type.genericTypes.remove('E')
                    type.genericTypes.put('D', ClassType.of(Integer))
                    return type
                }
        ]
    }

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
                ObjectType.of(List),
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
