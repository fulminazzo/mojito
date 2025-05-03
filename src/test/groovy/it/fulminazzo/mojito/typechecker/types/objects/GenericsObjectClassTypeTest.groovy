package it.fulminazzo.mojito.typechecker.types.objects

import it.fulminazzo.mojito.typechecker.TypeCheckerException
import it.fulminazzo.mojito.typechecker.types.ClassType
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

    def 'test that initialization with correct parameters does not throw'() {
        given:
        def type = ObjectType.of(List)

        and:
        def genericTypes = [ObjectClassType.STRING]

        when:
        def classType = new GenericsObjectClassType(type, genericTypes)

        then:
        classType.genericTypes['E'] == genericTypes[0]
    }

    def 'test that initialization with #parameter throws'() {
        given:
        def type = ObjectType.of(List)

        and:
        def genericTypes = (1..parameter).collect { ObjectClassType.STRING }
        if (parameter == 0) genericTypes.clear()

        when:
        new GenericsObjectClassType(type, genericTypes)

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.invalidGenericTypeSize(
                ClassType.of(List), 1, parameter
        ).message

        where:
        parameter << [0, 2, 3]
    }

    def 'test that exception is thrown when parameter with invalid bounds is passed'() {
        given:
        def type = ObjectType.of(TestClass)

        when:
        new GenericsObjectClassType(type, [parameter])

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.invalidType(
                bound, parameter
        ).message

        where:
        parameter                | bound
        ClassType.of(String)     | ClassType.of(Number)
        ClassType.of(Number)     | new GenericsObjectClassType(ObjectType.of(Comparator), [ObjectClassType.STRING])
        ClassType.of(Comparator) | ClassType.of(Number)
    }

    static class TestClass<F extends Number & Comparator<String>> {

    }

}
