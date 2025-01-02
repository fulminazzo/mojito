package it.fulminazzo.javaparser.typechecker.types

import it.fulminazzo.javaparser.typechecker.TypeCheckerException
import it.fulminazzo.javaparser.typechecker.types.objects.ObjectClassType
import it.fulminazzo.javaparser.typechecker.types.objects.ObjectType
import spock.lang.Specification

class TypeTest extends Specification {
    private static ParameterTypes NO_PARAMETERS = new ParameterTypes([])

    private Type type
    private ClassType classType

    void setup() {
        this.type = ObjectType.of(TestClass)
        this.classType = ObjectClassType.of(TestClass)
    }

    def 'test check not valid'() {
        when:
        this.type.checkNot(PrimitiveType.NUMBER)

        then:
        noExceptionThrown()
    }

    def 'test check not invalid'() {
        given:
        def type = ObjectType.of(TestClass)

        when:
        this.type.checkNot(type)

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.invalidUnexpectedType(type).message
    }

    def 'test check empty not'() {
        when:
        this.type.checkNot()

        then:
        thrown(IllegalArgumentException)
    }

    def 'test toWrapper should throw exception by default'() {
        when:
        this.type.toWrapper()

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.noWrapper(this.type).message
    }

    def 'test check class method'() {
        when:
        def t = this.type.check(ObjectType)

        then:
        t == ObjectType.of(TestClass)
    }

    def 'test invalid check class method'() {
        when:
        this.type.check(ObjectClassType)

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.invalidType(ObjectClassType, this.type).message
    }

    def 'test is #clazz should return true for #obj'() {
        expect:
        obj.is(clazz)

        where:
        obj << [
                PrimitiveType.values(),
                PrimitiveType.values(),
                PrimitiveClassType.values(),
                PrimitiveClassType.values(),
                PrimitiveClassType.values(),
                ObjectType.OBJECT,
                ObjectType.OBJECT,
                ObjectClassType.values(),
                ObjectClassType.values(),
                ObjectClassType.values(),
                ObjectClassType.of(TestClass),
                ObjectClassType.of(TestClass)
        ].flatten()
        clazz << [
                PrimitiveType.values().collect { PrimitiveType },
                PrimitiveType.values().collect { Type },
                PrimitiveClassType.values().collect { PrimitiveClassType },
                PrimitiveClassType.values().collect { ClassType },
                PrimitiveClassType.values().collect { Type },
                ObjectType,
                Type,
                ObjectClassType.values().collect { ObjectClassType },
                ObjectClassType.values().collect { ClassType },
                ObjectClassType.values().collect { Type },
                ClassType,
                Type
        ].flatten()
    }

    /**
     * GET FIELD
     */

    def 'test valid getField #field'() {
        when:
        def actual = this.type.getField(field)

        then:
        actual == expected

        where:
        field               | expected
        'publicStaticField' | PrimitiveClassType.INT
        'publicField'       | PrimitiveClassType.DOUBLE
    }

    def 'test cannot access field #field from getField'() {
        when:
        this.type.getField(field)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.cannotAccessField(this.type.toClassType(), TestClass.getDeclaredField(field)).message

        where:
        field << [
                'packageStaticField', 'protectedStaticField', 'privateStaticField',
                'packageField', 'protectedField', 'privateField'
        ]
    }

    def 'test field not found'() {
        when:
        this.type.getField('invalid')

        then:
        def e = thrown(TypeException)
        e.message == TypeException.fieldNotFound(this.type.toClassType(), 'invalid').message
    }

    def 'test class valid getField #field'() {
        when:
        def actual = this.classType.getField(field)

        then:
        actual == expected

        where:
        field               | expected
        'publicStaticField' | PrimitiveClassType.INT
    }

    def 'test class cannot access non-static field'() {
        given:
        def field = 'publicField'

        when:
        this.classType.getField(field)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.cannotAccessStaticField(this.classType, field).message
    }

    def 'test class cannot access field #field from getField'() {
        when:
        this.classType.getField(field)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.cannotAccessField(this.classType, TestClass.getDeclaredField(field)).message

        where:
        field << [
                'packageStaticField', 'protectedStaticField', 'privateStaticField',
                'packageField', 'protectedField', 'privateField'
        ]
    }

    def 'test class field not found'() {
        when:
        this.classType.getField('invalid')

        then:
        def e = thrown(TypeException)
        e.message == TypeException.fieldNotFound(this.classType, 'invalid').message
    }

    /**
     * GET METHOD
     */

    def 'test valid getMethod #method(#parameters)'() {
        when:
        def actual = this.type.getMethod(method, parameters)

        then:
        actual == expected

        where:
        method               | expected                  | parameters
        'publicStaticMethod' | PrimitiveClassType.INT    | NO_PARAMETERS
        'publicStaticMethod' | PrimitiveClassType.INT    | new ParameterTypes([PrimitiveClassType.INT, ObjectClassType.BOOLEAN])
        'publicMethod'       | PrimitiveClassType.DOUBLE | NO_PARAMETERS
        'publicMethod'       | PrimitiveClassType.DOUBLE | new ParameterTypes([PrimitiveClassType.DOUBLE, ObjectClassType.BOOLEAN])
    }

    def 'test getMethod #method(#parameters) should throw types mismatch'() {
        when:
        this.type.getMethod(method, parameters)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.typesMismatch(this.type.toClassType(),
                TestClass.getDeclaredMethod(method, methodTypes), parameters).message

        where:
        method               | methodTypes                  | parameters
        'publicStaticMethod' | new Class[]{int, Boolean}    | new ParameterTypes([PrimitiveClassType.DOUBLE, ObjectClassType.BOOLEAN])
        'publicStaticMethod' | new Class[]{int, Boolean}    | new ParameterTypes([PrimitiveClassType.INT, ObjectClassType.STRING])
        'publicStaticMethod' | new Class[]{int, Boolean}    | new ParameterTypes([PrimitiveClassType.DOUBLE, ObjectClassType.STRING])
        'publicMethod'       | new Class[]{double, Boolean} | new ParameterTypes([PrimitiveClassType.INT, ObjectClassType.BOOLEAN])
        'publicMethod'       | new Class[]{double, Boolean} | new ParameterTypes([PrimitiveClassType.DOUBLE, ObjectClassType.STRING])
        'publicMethod'       | new Class[]{double, Boolean} | new ParameterTypes([PrimitiveClassType.INT, ObjectClassType.STRING])
    }

    def 'test cannot access method #method from getMethod'() {
        when:
        this.type.getMethod(method, NO_PARAMETERS)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.cannotAccessMethod(this.type.toClassType(), TestClass.getDeclaredMethod(method)).message

        where:
        method << [
                'packageStaticMethod', 'protectedStaticMethod', 'privateStaticMethod',
                'packageMethod', 'protectedMethod', 'privateMethod'
        ]
    }

    def 'test method not found'() {
        when:
        this.type.getMethod('invalid', NO_PARAMETERS)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.methodNotFound(this.type.toClassType(), 'invalid', NO_PARAMETERS).message
    }

    def 'test class valid getMethod #method(#parameters)'() {
        when:
        def actual = this.classType.getMethod(method, parameters)

        then:
        actual == expected

        where:
        method               | expected               | parameters
        'publicStaticMethod' | PrimitiveClassType.INT | NO_PARAMETERS
        'publicStaticMethod' | PrimitiveClassType.INT | new ParameterTypes([PrimitiveClassType.INT, ObjectClassType.BOOLEAN])
    }

    def 'test class cannot access non-static method #method(#parameters)'() {
        when:
        this.classType.getMethod(method, NO_PARAMETERS)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.cannotAccessStaticMethod(this.classType, method, NO_PARAMETERS).message

        where:
        method         | expected                  | parameters
        'publicMethod' | PrimitiveClassType.DOUBLE | NO_PARAMETERS
        'publicMethod' | PrimitiveClassType.DOUBLE | new ParameterTypes([PrimitiveClassType.DOUBLE, ObjectClassType.BOOLEAN])
    }

    def 'test class cannot access method #method from getMethod'() {
        when:
        this.classType.getMethod(method, NO_PARAMETERS)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.cannotAccessMethod(this.classType, TestClass.getDeclaredMethod(method)).message

        where:
        method << [
                'packageStaticMethod', 'protectedStaticMethod', 'privateStaticMethod',
                'packageMethod', 'protectedMethod', 'privateMethod'
        ]
    }

    def 'test class method not found'() {
        when:
        this.classType.getMethod('invalid', NO_PARAMETERS)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.methodNotFound(this.classType, 'invalid', NO_PARAMETERS).message
    }

}