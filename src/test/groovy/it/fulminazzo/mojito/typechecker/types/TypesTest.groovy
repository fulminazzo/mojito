package it.fulminazzo.mojito.typechecker.types

import it.fulminazzo.mojito.typechecker.TypeCheckerException
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType
import spock.lang.Specification

class TypesTest extends Specification {
    private ClassType type

    void setup() {
        this.type = new Types.SingletonType('TEST_TYPE')
    }

    def 'test null type should be compatible with class type #classType'() {
        given:
        def type = Types.NULL_TYPE

        expect:
        type.isAssignableFrom(classType)

        where:
        classType << ObjectClassType.values()
    }

    def 'test null type should not be compatible with class type #classType'() {
        given:
        def type = Types.NULL_TYPE

        expect:
        !type.isAssignableFrom(classType)

        where:
        classType << PrimitiveClassType.values()
    }

    def 'test SingletonType should not have any class associated'() {
        when:
        this.type.toJavaClass()

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.noClassType(Types.SingletonType).message
    }

    def 'test SingletonType absorption'() {
        given:
        def type = PrimitiveType.BOOLEAN

        when:
        def cast = this.type.cast(type)

        then:
        cast == this.type
    }

    def 'test SingletonType compatibleWith #other should be #expected'() {
        when:
        def actual = this.type.compatibleWith(other)

        then:
        actual == expected

        where:
        other                                | expected
        new Types.SingletonType('TEST_TYPE') | true
        Types.NULL_TYPE                      | false
        null                                 | false
        PrimitiveType.BOOLEAN                | false
        ObjectType.BOOLEAN                   | false
    }

    def 'test SingletonType toType'() {
        expect:
        this.type == this.type.toType()
    }

    def 'test SingletonType toClass'() {
        expect:
        this.type == this.type.toClass()
    }

    def 'test SingletonType hashCode'() {
        given:
        def code = this.type.hashCode()
        int expected = Objects.hash(Types.SingletonType, 'TEST_TYPE')

        expect:
        code == expected
    }

    def 'test SingletonType equality'() {
        given:
        def second = new Types.SingletonType('TEST_TYPE')

        expect:
        this.type == second
    }

    def 'test SingletonType should not be equal to #other'() {
        expect:
        !this.type.equals(other)

        where:
        other << [PrimitiveClassType.BOOLEAN, ObjectType.STRING, null,
                  new Types.SingletonType('MOCK'), 'TEST_TYPE']
    }

    def 'test SingletonType toString'() {
        given:
        def string = this.type.toString()

        expect:
        string == 'TEST_TYPE'
    }

}
