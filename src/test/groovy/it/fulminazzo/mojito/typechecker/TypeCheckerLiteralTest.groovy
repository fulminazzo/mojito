package it.fulminazzo.mojito.typechecker

import it.fulminazzo.fulmicollection.objects.Refl
import it.fulminazzo.fulmicollection.structures.tuples.Tuple
import it.fulminazzo.mojito.TestClass
import it.fulminazzo.mojito.environment.MockEnvironment
import it.fulminazzo.mojito.typechecker.types.*
import it.fulminazzo.mojito.typechecker.types.objects.ObjectClassType
import it.fulminazzo.mojito.typechecker.types.objects.ObjectType
import it.fulminazzo.mojito.typechecker.types.variables.TypeLiteralVariableContainer
import spock.lang.Specification

class TypeCheckerLiteralTest extends Specification {
    private TypeChecker typeChecker
    private MockEnvironment environment

    void setup() {
        this.typeChecker = new TypeChecker(new TestClass())
        this.environment = new MockEnvironment<>()
        new Refl<>(this.typeChecker).setFieldObject('environment', this.environment)
    }

    def 'test visit literal from code #code should return #expected'() {
        given:
        this.environment.declare(ObjectClassType.INTEGER, 'var', PrimitiveType.INT)

        when:
        def read = this.typeChecker.visitLiteralImpl(code)

        then:
        read == expected

        where:
        code                                                        | expected
        'val'                                                       | new TypeLiteralVariableContainer(this.environment, 'val')
        'int'                                                       | PrimitiveClassType.INT
        'String'                                                    | ObjectClassType.STRING
        'System'                                                    | ClassType.of(System)
        'System.class'                                              | ObjectType.of(Class, [ClassType.of(System)])
        'System.out'                                                | ObjectType.of(PrintStream.canonicalName)
        'var'                                                       | PrimitiveType.INT
        'var.TYPE'                                                  | ObjectType.of('Class')
        "${FirstInnerClass.canonicalName}.second"                   | ObjectType.of(FirstInnerClass.SecondInnerClass.canonicalName)
        "${FirstInnerClass.canonicalName}.second.version"           | PrimitiveType.INT
        "${FirstInnerClass.SecondInnerClass.canonicalName}"         | ClassType.of(FirstInnerClass.SecondInnerClass.canonicalName)
        "${FirstInnerClass.SecondInnerClass.canonicalName}.version" | PrimitiveType.INT
    }

    def 'test visit literal invalid field'() {
        given:
        def typeException = TypeException.fieldNotFound(ClassType.of(System), 'non_existent')

        when:
        this.typeChecker.visitLiteralImpl('System.non_existent')

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.of(typeException).message
    }

    def 'test visit literal invalid literal'() {
        given:
        def literal = 'invalid.class'

        when:
        this.typeChecker.visitLiteralImpl(literal)

        then:
        def e = thrown(TypeCheckerException)
        e.message == TypeCheckerException.cannotResolveSymbol(literal).message
    }

    def 'test getObjectFromLiteral #literal'() {
        given:
        this.environment.declare(PrimitiveClassType.INT, 'i', PrimitiveType.INT)

        when:
        def tuple = this.typeChecker.getObjectFromLiteral(literal)

        then:
        tuple == expected

        where:
        literal   | expected
        'int'     | new Tuple<>(PrimitiveClassType.INT, PrimitiveClassType.INT)
        'i'       | new Tuple<>(PrimitiveClassType.INT, PrimitiveType.INT)
        'invalid' | new Tuple<>()
    }

    static class FirstInnerClass {
        public static SecondInnerClass second = new SecondInnerClass()

        static class SecondInnerClass {
            public static int version = 2
        }

    }

}
