package it.fulminazzo.mojito.typechecker.types.objects.generics

import it.fulminazzo.mojito.typechecker.TypeCheckerException
import it.fulminazzo.mojito.typechecker.types.ClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType
import spock.lang.Specification

class GenericsUtilsTest extends Specification {

    def 'test compatibility'() {
        given:
        def parameters = [ObjectClassType.STRING]

        and:
        def model = new GenericsObjectClassType(ObjectType.of(Iterable), parameters)
        def target = new GenericsObjectType(Collection, parameters)

        expect:
        GenericsUtils.checkCompatibility(model, target)
    }

    def 'test that getClassHierarchy works'() {
        given:
        def start = LinkedList
        def end = Iterable

        and:
        def expected = [
                LinkedList, AbstractSequentialList, AbstractList,
                AbstractCollection, Collection, Iterable
        ]

        when:
        def actual = GenericsUtils.getClassHierarchy(start, end)

        then:
        actual == expected
    }

    def 'test that initialization with correct parameters does not throw'() {
        given:
        def type = List

        and:
        def genericTypes = [ObjectClassType.STRING]

        when:
        def types = GenericsUtils.genericTypesToMap(type, genericTypes)

        then:
        types['E'] == genericTypes[0]
    }

    def 'test that initialization with #parameter throws'() {
        given:
        def type = List

        and:
        def genericTypes = (1..parameter).collect { ObjectClassType.STRING }
        if (parameter == 0) genericTypes.clear()

        when:
        GenericsUtils.genericTypesToMap(type, genericTypes)

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
        def type = GenericsTestClass

        when:
        GenericsUtils.genericTypesToMap(type, [parameter])

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.invalidType(
                bound, parameter
        ).message

        where:
        parameter                               | bound
        ClassType.of(String)                    | ClassType.of(GenericsTestClass.Numeric)
        ClassType.of(GenericsTestClass.Numeric) | ClassType.of(GenericsTestClass.Decimal)
        ClassType.of(GenericsTestClass.Decimal) | ClassType.of(GenericsTestClass.Numeric)
    }

}
