package it.fulminazzo.mojito.typechecker.types

import it.fulminazzo.fulmicollection.objects.Refl
import it.fulminazzo.mojito.TestClass
import it.fulminazzo.mojito.typechecker.types.objects.GenericsObjectClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType
import it.fulminazzo.mojito.visitors.visitorobjects.ClassVisitorObject
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.lang.reflect.Method

class ClassTypeTest extends Specification {
    private ClassType classType

    void setup() {
        this.classType = ClassType.of(TestClass)
    }

    def 'test that generic class is correctly recognized'() {
        given:
        def className = 'Map'
        def parameterTypes = 'Map<Integer, List<String>>, TreeMap<Number, Double>'

        when:
        def classType = ClassType.of("${className}<${parameterTypes}>")

        then:
        classType == new GenericsObjectClassType(
                ClassType.of(className),
                [
                        new GenericsObjectClassType(
                                ClassType.of('Integer'),
                                [new GenericsObjectClassType(
                                        ClassType.of('List'),
                                        [ClassType.of('String')]
                                )]
                        ),
                        new GenericsObjectClassType(
                                ClassType.of('Tree'),
                                [ClassType.of('Number')]
                        )
                ]
        )
    }

    def 'test method #toClass should always return a wrapper for java.lang.Class'() {
        given:
        def classType = new MockClassType().toClass()

        expect:
        classType == ObjectClassType.of(Class)
    }

    def 'test of #className should return #expected'() {
        given:
        def type = ClassType.of(className)

        expect:
        type == expected

        where:
        className << [
                PrimitiveClassType.values()*.name()*.toLowerCase(),
                ObjectClassType.values()*.name().collect {
                    "${it[0]}${it.substring(1).toLowerCase()}"
                },
                Map.simpleName,
        ].flatten()
        expected << [
                PrimitiveClassType.values(),
                ObjectClassType.values(),
                new Refl<>("${ObjectClassType.package.name}.CustomObjectClassType",
                        ObjectType.of('Map')).object,
        ].flatten()
    }

    def 'test compatibleWith type called from Object method'() {
        given:
        def type = new MockType()
        def classType = new MockClassType()

        when:
        Method method = ClassVisitorObject.getDeclaredMethod('compatibleWith', Object)

        then:
        type.isAssignableFrom(classType)
        method.invoke(classType, type)
    }

    def 'test not compatibleWith #object'() {
        given:
        def classType = new MockClassType()

        when:
        Method method = ClassVisitorObject.getDeclaredMethod('compatibleWith', Object)

        then:
        !method.invoke(classType, object)

        where:
        object << [
                new Type() {

                    @Override
                    @NotNull
                    ClassType toClass() {
                        return null
                    }

                },
                new Object(),
        ]
    }

    /**
     * NEW OBJECT
     */
    def 'test valid newObject (#parameters)'() {
        when:
        def actual = this.classType.newObject(parameters)

        then:
        actual == expected

        where:
        expected                 | parameters
        ObjectType.of(TestClass) | new ParameterTypes([])
        ObjectType.of(TestClass) | new ParameterTypes([PrimitiveType.INT, ObjectType.BOOLEAN])
    }

    def 'test newObject (#parameters) should throw types mismatch'() {
        when:
        this.classType.newObject(parameters)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.typesMismatch(this.classType,
                TestClass.getDeclaredConstructor(types), parameters).message

        where:
        types                     | parameters
        new Class[]{int, Boolean} | new ParameterTypes([PrimitiveType.DOUBLE, ObjectType.BOOLEAN])
        new Class[]{int, Boolean} | new ParameterTypes([PrimitiveType.INT, ObjectType.STRING])
        new Class[]{int, Boolean} | new ParameterTypes([PrimitiveType.DOUBLE, ObjectType.STRING])
    }

    def 'test cannot access constructor (#type)'() {
        when:
        this.classType.newObject(new ParameterTypes([type]))

        then:
        def e = thrown(TypeException)
        e.message == TypeException.cannotAccessMethod(this.classType, TestClass.getDeclaredConstructor(clazz)).message

        where:
        type                  | clazz
        PrimitiveType.FLOAT   | float
        PrimitiveType.BOOLEAN | boolean
    }

    def 'test constructor not found'() {
        given:
        def mockParameters = new ParameterTypes([PrimitiveType.INT, PrimitiveType.DOUBLE, PrimitiveType.SHORT])
        when:
        this.classType.newObject(mockParameters)

        then:
        def e = thrown(TypeException)
        e.message == TypeException.methodNotFound(this.classType, '<init>', mockParameters).message
    }

}
