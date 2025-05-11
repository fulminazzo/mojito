package it.fulminazzo.mojito.executor

import it.fulminazzo.fulmicollection.objects.Refl
import it.fulminazzo.fulmicollection.structures.tuples.Tuple
import it.fulminazzo.mojito.environment.MockEnvironment
import it.fulminazzo.mojito.executor.values.ClassValue
import it.fulminazzo.mojito.executor.values.PrimitiveClassValue
import it.fulminazzo.mojito.TestClass
import it.fulminazzo.mojito.executor.values.objects.ObjectClassValue
import it.fulminazzo.mojito.executor.values.objects.ObjectValue
import it.fulminazzo.mojito.executor.values.primitivevalue.PrimitiveValue
import it.fulminazzo.mojito.executor.values.variables.ValueLiteralVariableContainer
import spock.lang.Specification

class ExecutorLiteralTest extends Specification {
    private Executor executor
    private MockEnvironment environment

    void setup() {
        this.executor = new Executor(new TestClass())
        this.environment = new MockEnvironment<>()
        new Refl<>(this.executor).setFieldObject('environment', this.environment)
    }

    def 'test visit literal from code #code should return #expected'() {
        given:
        this.environment.declare(PrimitiveClassValue.INT, 'var', PrimitiveValue.of(1))

        when:
        def read = this.executor.visitLiteralImpl(code)

        then:
        read == expected

        where:
        code                                                        | expected
        'val'                                                       | new ValueLiteralVariableContainer<>(this.environment, 'val')
        'int'                                                       | PrimitiveClassValue.INT
        'String'                                                    | ObjectClassValue.STRING
        'System'                                                    | ClassValue.of(System)
        'System.class'                                              | ObjectValue.of(System)
        'System.out'                                                | ObjectValue.of(System.out)
        'var'                                                       | PrimitiveValue.of(1)
        'var.TYPE'                                                  | ObjectValue.of(int)
        "${FirstInnerClass.canonicalName}.second"                   | ObjectValue.of(FirstInnerClass.second)
        "${FirstInnerClass.canonicalName}.second.version"           | PrimitiveValue.of(2)
        "${FirstInnerClass.SecondInnerClass.canonicalName}"         | ClassValue.of(FirstInnerClass.SecondInnerClass)
        "${FirstInnerClass.SecondInnerClass.canonicalName}.version" | PrimitiveValue.of(2)
    }

    def 'test visitLiteralImpl of invalid field'() {
        given:
        def code = 'not.existing'

        when:
        this.executor.visitLiteralImpl(code)

        then:
        def e = thrown(ExecutorException)
        e.message == ExecutorException.cannotResolveSymbol(code).message
    }

    def 'test getObjectFromLiteral #literal'() {
        given:
        this.environment.declare(PrimitiveClassValue.INT, 'i', PrimitiveValue.of(1))

        when:
        def tuple = this.executor.getObjectFromLiteral(literal)

        then:
        tuple == expected

        where:
        literal   | expected
        'int'     | new Tuple<>(PrimitiveClassValue.INT, PrimitiveClassValue.INT)
        'i'       | new Tuple<>(PrimitiveClassValue.INT, PrimitiveValue.of(1))
        'invalid' | new Tuple<>()
    }

    static class FirstInnerClass {
        public static SecondInnerClass second = new SecondInnerClass()

        static class SecondInnerClass {
            public static int version = 2
        }

    }

}
