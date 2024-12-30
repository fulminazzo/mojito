package it.fulminazzo.javaparser.typechecker.types.objects

import it.fulminazzo.javaparser.typechecker.TypeCheckerException
import it.fulminazzo.javaparser.typechecker.types.PrimitiveType
import spock.lang.Specification

class CustomClassObjectTypeTest extends Specification {

    def 'test cast of #cast to #type should return #cast'() {
        when:
        def actual = cast.cast(type)

        then:
        actual == cast.toType()

        where:
        cast                                                 | type
        new CustomClassObjectType(ObjectType.of(Collection)) | ObjectType.of(List)
        new CustomClassObjectType(ObjectType.of(List))       | ObjectType.of(List)
        new CustomClassObjectType(ObjectType.of(LinkedList)) | ObjectType.of(List)
        new CustomClassObjectType(ObjectType.of(ArrayList))  | ObjectType.of(List)
    }

    def 'test invalid cast of #cast to #type'() {
        when:
        cast.cast(type)

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.invalidCast(cast, type).message

        where:
        cast                                                 | type
        new CustomClassObjectType(ObjectType.of(Map))        | ObjectType.of(List)
        new CustomClassObjectType(ObjectType.of(HashMap))    | ObjectType.of(List)
        new CustomClassObjectType(ObjectType.of(Exception))  | ObjectType.of(List)
    }

    def 'test conversion of types'() {
        given:
        def type = ObjectType.of(Map)

        when:
        def classType = type.toClassType()
        def newType = classType.toType()

        then:
        newType == type
        classType == new CustomClassObjectType(ObjectType.of(Map))
    }

    def 'test compatibleWith'() {
        given:
        def className = getClass().canonicalName
        def type = ObjectType.of(className)
        def classType = new CustomClassObjectType(type)

        expect:
        classType.compatibleWith(type)
    }

    def 'test incompatible with #type'() {
        given:
        // Bad practice, just for testing purposes
        def classType = new CustomClassObjectType(ObjectType.STRING)

        expect:
        !classType.compatibleWith(type)

        where:
        type << [
                PrimitiveType.values(),
                [ObjectType.BYTE, ObjectType.SHORT, ObjectType.OBJECT],
                ObjectType.of(getClass().canonicalName)
        ].flatten()
    }

    def 'test equality'() {
        given:
        def className = getClass().canonicalName
        def type = ObjectType.of(className)
        def first = new CustomClassObjectType(type)
        def second = new CustomClassObjectType(type)

        expect:
        first == second
        first.hashCode() == second.hashCode()
    }

    def 'test inequality'() {
        given:
        def className = getClass().canonicalName
        def classType = new CustomClassObjectType(ObjectType.of(className))

        expect:
        !classType.equals(type)

        where:
        type << [
                null,
                PrimitiveType.values(),
                ClassObjectType.values(),
                new CustomClassObjectType(ObjectType.STRING)
        ].flatten()
    }

}
