package it.fulminazzo.mojito.executor

import it.fulminazzo.fulmicollection.objects.Refl
import it.fulminazzo.mojito.TestClass
import it.fulminazzo.mojito.environment.MockEnvironment
import it.fulminazzo.mojito.environment.NamedEntity
import it.fulminazzo.mojito.environment.ScopeException
import it.fulminazzo.mojito.environment.scopetypes.ScopeType
import it.fulminazzo.mojito.executor.values.*
import it.fulminazzo.mojito.executor.values.arrays.ArrayClassValue
import it.fulminazzo.mojito.executor.values.arrays.ArrayValue
import it.fulminazzo.mojito.executor.values.objects.ObjectClassValue
import it.fulminazzo.mojito.executor.values.objects.ObjectValue
import it.fulminazzo.mojito.executor.values.primitivevalue.BooleanValue
import it.fulminazzo.mojito.executor.values.primitivevalue.PrimitiveValue
import it.fulminazzo.mojito.parser.node.MethodInvocation
import it.fulminazzo.mojito.parser.node.arrays.StaticArray
import it.fulminazzo.mojito.parser.node.container.CodeBlock
import it.fulminazzo.mojito.parser.node.container.JavaProgram
import it.fulminazzo.mojito.parser.node.literals.ArrayLiteral
import it.fulminazzo.mojito.parser.node.literals.EmptyLiteral
import it.fulminazzo.mojito.parser.node.literals.Literal
import it.fulminazzo.mojito.parser.node.literals.ThisLiteral
import it.fulminazzo.mojito.parser.node.operators.binary.*
import it.fulminazzo.mojito.parser.node.operators.unary.Decrement
import it.fulminazzo.mojito.parser.node.operators.unary.Increment
import it.fulminazzo.mojito.parser.node.statements.*
import it.fulminazzo.mojito.parser.node.values.*
import spock.lang.Specification

import java.util.concurrent.Callable

class ExecutorTest extends Specification {
    private static final BOOL_LIT_TRUE = new BooleanValueLiteral('true')
    private static final BOOL_LIT_FALSE = new BooleanValueLiteral('false')
    private static final CHAR_LIT = new CharValueLiteral('\'a\'')
    private static final NUMBER_LIT = new NumberValueLiteral('1')
    private static final LONG_LIT = new LongValueLiteral('2L')
    private static final FLOAT_LIT = new FloatValueLiteral('3.0f')
    private static final DOUBLE_LIT = new DoubleValueLiteral('4.0d')
    private static final STRING_LIT = new StringValueLiteral('\"Hello, world!\"')

    private static final CODE_BLOCK_EMPTY = new CodeBlock()
    private static final CODE_BLOCK_1 = new CodeBlock(new Return(new NumberValueLiteral('1')))
    private static final CODE_BLOCK_2 = new CodeBlock(new Return(new NumberValueLiteral('2')))
    private static final CODE_BLOCK_3 = new CodeBlock(new Return(new NumberValueLiteral('3')))

    private static final IAEX = Literal.of('IllegalArgumentException')

    private Executor executor
    private MockEnvironment environment

    void setup() {
        this.executor = new Executor(new TestClass())
        this.environment = new MockEnvironment<>()
        new Refl<>(this.executor).setFieldObject('environment', this.environment)
    }

    def 'parse test_program file'() {
        given:
        def cwd = System.getProperty('user.dir')

        and:
        def file = new File(cwd, 'build/resources/test/parsed_test_program.dat')
        JavaProgram program = file.newObjectInputStream().readObject() as JavaProgram

        when:
        program.accept(this.executor)

        then:
        noExceptionThrown()
    }

    def 'test visit program with throw'() {
        given:
        def code = new Throw(new NewObject(Literal.of(IllegalArgumentException.canonicalName),
                new MethodInvocation([new StringValueLiteral('\"Hello, world!\"')])))

        when:
        code.accept(this.executor)

        then:
        def e = thrown(ExceptionWrapper)
        def actual = e.actualException.value
        actual.message == 'Hello, world!'
    }

    def 'test visit try statement: (#expression) #block #catchBlocks #finallyBlock should return #expected'() {
        when:
        def value = this.executor.visitTryStatement(block, catchBlocks, finallyBlock, expression)

        then:
        value == expected

        where:
        expression         | block                               | catchBlocks | finallyBlock    | expected
        new EmptyLiteral() | CODE_BLOCK_1                        | []          | new CodeBlock() | PrimitiveValue.of(1)
        new EmptyLiteral() | CODE_BLOCK_1                        | []          | CODE_BLOCK_3    | PrimitiveValue.of(3)
        new EmptyLiteral() | CODE_BLOCK_1                        | [
                new CatchStatement([IAEX], Literal.of('e'), CODE_BLOCK_2)
        ]                                                                      | new CodeBlock() | PrimitiveValue.of(1)
        new EmptyLiteral() | CODE_BLOCK_1                        | [
                new CatchStatement([IAEX], Literal.of('e'), CODE_BLOCK_2)
        ]                                                                      | CODE_BLOCK_3    | PrimitiveValue.of(3)
        new EmptyLiteral() | new CodeBlock(new Throw(new NewObject(IAEX,
                new MethodInvocation([]))))                      | [
                new CatchStatement([IAEX], Literal.of('e'), CODE_BLOCK_2)
        ]                                                                      | new CodeBlock() | PrimitiveValue.of(2)
        new EmptyLiteral() | new CodeBlock(new Throw(new NewObject(IAEX,
                new MethodInvocation([]))))                      | [
                new CatchStatement([IAEX], Literal.of('e'), CODE_BLOCK_2)
        ]                                                                      | CODE_BLOCK_3    | PrimitiveValue.of(3)
    }

    def 'test visit switch statement of #expression (#cases, #defaultBlock) should return #expected'() {
        when:
        def value = this.executor.visitSwitchStatement(cases, defaultBlock, expression)

        then:
        value == expected

        where:
        expression                  | cases                                                 | defaultBlock | expected
        new NumberValueLiteral('1') | [
                new CaseStatement(new NumberValueLiteral('1'), CODE_BLOCK_1),
                new CaseStatement(new NumberValueLiteral('2'), CODE_BLOCK_2),
        ]                                                                                   | CODE_BLOCK_3 | PrimitiveValue.of(1)
        new NumberValueLiteral('2') | [
                new CaseStatement(new NumberValueLiteral('1'), CODE_BLOCK_1),
                new CaseStatement(new NumberValueLiteral('2'), CODE_BLOCK_2),
        ]                                                                                   | CODE_BLOCK_3 | PrimitiveValue.of(2)
        new NumberValueLiteral('3') | [
                new CaseStatement(new NumberValueLiteral('1'), CODE_BLOCK_1),
                new CaseStatement(new NumberValueLiteral('2'), CODE_BLOCK_2),
        ]                                                                                   | CODE_BLOCK_3 | PrimitiveValue.of(3)
        new NumberValueLiteral('1') | [
                new CaseStatement(new NumberValueLiteral('1'), CODE_BLOCK_EMPTY),
                new CaseStatement(new NumberValueLiteral('2'), CODE_BLOCK_2),
        ]                                                                                   | CODE_BLOCK_3 | PrimitiveValue.of(2)
        new NumberValueLiteral('1') | [
                new CaseStatement(new NumberValueLiteral('1'), new CodeBlock(new Break())),
                new CaseStatement(new NumberValueLiteral('2'), CODE_BLOCK_2),
        ]                                                                                   | CODE_BLOCK_3 | Values.NO_VALUE
    }

    def 'test visit enhanced for statement of #object should return #expected'() {
        given:
        this.environment.declare(PrimitiveClassValue.INT, 'counter', PrimitiveValue.of(0))

        and:
        this.environment.declare(ArrayClassValue.of(PrimitiveClassValue.INT), 'arr',
                ArrayValue.of(PrimitiveClassValue.INT, (1..9).collect { Value.of(it) })
        )
        this.environment.declare(ClassValue.of(List.class), 'list',
                Value.of(1..4))

        when:
        def value = this.executor.visitEnhancedForStatement(
                Literal.of('int'), Literal.of('i'),
                code, object)
        def counter = this.environment.lookup('counter')

        then:
        value == expected
        counter == expectedCounter

        where:
        expected             | expectedCounter       | object             | code
        PrimitiveValue.of(3) | PrimitiveValue.of(3)  | Literal.of('list') |
                new CodeBlock(
                        new IfStatement(new Equal(Literal.of('counter'), new NumberValueLiteral('3')),
                                CODE_BLOCK_3, CODE_BLOCK_EMPTY),
                        new Statement(new ReAssign(Literal.of('counter'),
                                new Add(Literal.of('counter'), Literal.of('i'))))
                )
        Values.NO_VALUE      | PrimitiveValue.of(10) | Literal.of('list') |
                new CodeBlock(new Statement(new ReAssign(Literal.of('counter'),
                        new Add(Literal.of('counter'), Literal.of('i')))))
        PrimitiveValue.of(3) | PrimitiveValue.of(15) | Literal.of('arr')  |
                new CodeBlock(
                        new IfStatement(new Equal(Literal.of('counter'), new NumberValueLiteral('15')),
                                CODE_BLOCK_3, CODE_BLOCK_EMPTY),
                        new Statement(new ReAssign(Literal.of('counter'),
                                new Add(Literal.of('counter'), Literal.of('i'))))
                )
        Values.NO_VALUE      | PrimitiveValue.of(45) | Literal.of('arr')  |
                new CodeBlock(new Statement(new ReAssign(Literal.of('counter'),
                        new Add(Literal.of('counter'), Literal.of('i')))))
    }

    def 'test visit for statement'() {
        given:
        this.environment.declare(PrimitiveClassValue.INT, 'i', PrimitiveValue.of(0))
        this.environment.declare(PrimitiveClassValue.INT, 'j', PrimitiveValue.of(1))

        when:
        def value = this.executor.visitForStatement(
                new ReAssign(Literal.of('i'), new NumberValueLiteral('1')),
                new Increment(Literal.of('i'), false),
                new CodeBlock(block),
                new LessThan(Literal.of('i'), new NumberValueLiteral('10'))
        )
        def counter = this.environment.lookup('i')

        then:
        value == expected
        counter == expectedCounter

        where:
        expected             | expectedCounter       | block
        PrimitiveValue.of(3) | PrimitiveValue.of(5)  | new IfStatement(
                new Equal(Literal.of('i'), new NumberValueLiteral('5')), CODE_BLOCK_3, new CodeBlock()
        )
        Values.NO_VALUE      | PrimitiveValue.of(4)  | new Statement[]{
                new Statement(new ReAssign(Literal.of('j'), new Multiply(Literal.of('j'), Literal.of('i')))),
                new IfStatement(
                        new GreaterThanEqual(Literal.of('j'), new NumberValueLiteral('10')),
                        new CodeBlock(new Break()), new CodeBlock()
                )
        }
        Values.NO_VALUE      | PrimitiveValue.of(5)  | new IfStatement(
                new Equal(Literal.of('i'), new NumberValueLiteral('5')), new CodeBlock(new Break()), new CodeBlock()
        )
        Values.NO_VALUE      | PrimitiveValue.of(10) | new Statement()
    }

    def 'test visit do statement of (#expression) #codeBlock should return #expected'() {
        given:
        this.environment.declare(PrimitiveClassValue.INT, 'i', PrimitiveValue.of(0))

        when:
        def value = this.executor.visitDoStatement(codeBlock, expression)
        def counter = this.environment.lookup('i')

        then:
        value == expected
        counter == expectedCounter

        where:
        expected             | expectedCounter       | expression                                                  | codeBlock
        Values.NO_VALUE      | PrimitiveValue.of(11) | new LessThan(Literal.of('i'), new NumberValueLiteral('10')) |
                new CodeBlock(
                        new Statement(new Increment(Literal.of('i'), false)),
                        new IfStatement(new Equal(new Modulo(Literal.of('i'), new NumberValueLiteral('2')), NUMBER_LIT),
                                new CodeBlock(new Continue()), new CodeBlock()),
                        new Statement(new Increment(Literal.of('i'), false))
                )
        Values.NO_VALUE      | PrimitiveValue.of(5)  | new LessThan(Literal.of('i'), new NumberValueLiteral('10')) |
                new CodeBlock(
                        new IfStatement(new Equal(Literal.of('i'), new NumberValueLiteral('5')),
                                new CodeBlock(new Break()), new CodeBlock()),
                        new Statement(new Increment(Literal.of('i'), false))
                )
        PrimitiveValue.of(3) | PrimitiveValue.of(5)  | new LessThan(Literal.of('i'), new NumberValueLiteral('10')) |
                new CodeBlock(
                        new IfStatement(new Equal(Literal.of('i'), new NumberValueLiteral('5')),
                                CODE_BLOCK_3, new CodeBlock()),
                        new Statement(new Increment(Literal.of('i'), false))
                )
        Values.NO_VALUE      | PrimitiveValue.of(10) | new LessThan(Literal.of('i'), new NumberValueLiteral('10')) |
                new CodeBlock(
                        new Statement(new Increment(Literal.of('i'), false))
                )
        PrimitiveValue.of(1) | PrimitiveValue.of(0)  | BOOL_LIT_TRUE                                               | CODE_BLOCK_1
        Values.NO_VALUE      | PrimitiveValue.of(1)  | BOOL_LIT_FALSE                                              |
                new CodeBlock(
                        new Statement(new Increment(Literal.of('i'), false))
                )
        Values.NO_VALUE      | PrimitiveValue.of(0)  | BOOL_LIT_FALSE                                              | CODE_BLOCK_EMPTY
    }

    def 'test visit while statement of (#expression) #codeBlock should return #expected'() {
        given:
        this.environment.declare(PrimitiveClassValue.INT, 'i', PrimitiveValue.of(0))

        when:
        def value = this.executor.visitWhileStatement(codeBlock, expression)
        def counter = this.environment.lookup('i')

        then:
        value == expected
        counter == expectedCounter

        where:
        expected             | expectedCounter       | expression                                                  | codeBlock
        Values.NO_VALUE      | PrimitiveValue.of(11) | new LessThan(Literal.of('i'), new NumberValueLiteral('10')) |
                new CodeBlock(
                        new Statement(new Increment(Literal.of('i'), false)),
                        new IfStatement(new Equal(new Modulo(Literal.of('i'), new NumberValueLiteral('2')), NUMBER_LIT),
                                new CodeBlock(new Continue()), new CodeBlock()),
                        new Statement(new Increment(Literal.of('i'), false))
                )
        Values.NO_VALUE      | PrimitiveValue.of(5)  | new LessThan(Literal.of('i'), new NumberValueLiteral('10')) |
                new CodeBlock(
                        new IfStatement(new Equal(Literal.of('i'), new NumberValueLiteral('5')),
                                new CodeBlock(new Break()), new CodeBlock()),
                        new Statement(new Increment(Literal.of('i'), false))
                )
        PrimitiveValue.of(3) | PrimitiveValue.of(5)  | new LessThan(Literal.of('i'), new NumberValueLiteral('10')) |
                new CodeBlock(
                        new IfStatement(new Equal(Literal.of('i'), new NumberValueLiteral('5')),
                                CODE_BLOCK_3, new CodeBlock()),
                        new Statement(new Increment(Literal.of('i'), false))
                )
        Values.NO_VALUE      | PrimitiveValue.of(10) | new LessThan(Literal.of('i'), new NumberValueLiteral('10')) |
                new CodeBlock(
                        new Statement(new Increment(Literal.of('i'), false))
                )
        PrimitiveValue.of(1) | PrimitiveValue.of(0)  | BOOL_LIT_TRUE                                               | CODE_BLOCK_1
        Values.NO_VALUE      | PrimitiveValue.of(0)  | BOOL_LIT_FALSE                                              | CODE_BLOCK_EMPTY
    }

    def 'test visit if statement of code "#code" should return #expected'() {
        given:
        def refl = new Refl<>(code)

        and:
        def expression = refl.getFieldObject('expression')
        def then = refl.getFieldObject('then')
        def elseBranch = refl.getFieldObject('elseBranch')

        when:
        def value = this.executor.visitIfStatement(then, elseBranch, expression)

        then:
        value == expected

        where:
        expected             | code
        // one if, no else
        PrimitiveValue.of(1) | new IfStatement(BOOL_LIT_TRUE, CODE_BLOCK_1, CODE_BLOCK_EMPTY)
        Values.NO_VALUE      | new IfStatement(BOOL_LIT_FALSE, CODE_BLOCK_1, CODE_BLOCK_EMPTY)
        // one if, one else
        PrimitiveValue.of(1) | new IfStatement(BOOL_LIT_TRUE, CODE_BLOCK_1, CODE_BLOCK_3)
        PrimitiveValue.of(3) | new IfStatement(BOOL_LIT_FALSE, CODE_BLOCK_1, CODE_BLOCK_3)
        // two if, no else
        PrimitiveValue.of(1) | new IfStatement(BOOL_LIT_TRUE, CODE_BLOCK_1,
                new IfStatement(BOOL_LIT_TRUE, CODE_BLOCK_2, CODE_BLOCK_EMPTY))
        PrimitiveValue.of(2) | new IfStatement(BOOL_LIT_FALSE, CODE_BLOCK_1,
                new IfStatement(BOOL_LIT_TRUE, CODE_BLOCK_2, CODE_BLOCK_EMPTY))
        Values.NO_VALUE      | new IfStatement(BOOL_LIT_FALSE, CODE_BLOCK_1,
                new IfStatement(BOOL_LIT_FALSE, CODE_BLOCK_2, CODE_BLOCK_EMPTY))
        // two if, one else
        PrimitiveValue.of(1) | new IfStatement(BOOL_LIT_TRUE, CODE_BLOCK_1,
                new IfStatement(BOOL_LIT_TRUE, CODE_BLOCK_2, CODE_BLOCK_3))
        PrimitiveValue.of(2) | new IfStatement(BOOL_LIT_FALSE, CODE_BLOCK_1,
                new IfStatement(BOOL_LIT_TRUE, CODE_BLOCK_2, CODE_BLOCK_3))
        PrimitiveValue.of(3) | new IfStatement(BOOL_LIT_FALSE, CODE_BLOCK_1,
                new IfStatement(BOOL_LIT_FALSE, CODE_BLOCK_2, CODE_BLOCK_3))
    }

    def 'test visit new object #parameters'() {
        given:
        def nodeExecutor = Literal.of(TestClass.canonicalName)
        def methodInvocation = new MethodInvocation(parameters)

        when:
        def value = this.executor.visitNewObject(nodeExecutor, methodInvocation)

        then:
        value == ObjectValue.of(expected)

        where:
        parameters                   | expected
        []                           | new TestClass()
        [NUMBER_LIT, BOOL_LIT_TRUE]  | new TestClass(1, true)
        [NUMBER_LIT, BOOL_LIT_FALSE] | new TestClass(1, false)
    }

    def 'test visit method call: #object #method #parameters should return #expected'() {
        given:
        this.environment.declare(
                ObjectClassValue.INTEGER,
                'method_call_val',
                ObjectValue.of(1)
        )
        this.environment.declare(
                PrimitiveClassValue.INT,
                'method_call_val_prim',
                PrimitiveValue.of(1)
        )

        and:
        def nodeExecutor = object.empty ? new EmptyLiteral() : Literal.of(object)
        def methodInvocation = new MethodInvocation(parameters)

        when:
        def value = this.executor.visitMethodCall(nodeExecutor, method, methodInvocation)

        then:
        value == expected

        where:
        object                 | method               | parameters                                   | expected
        ''                     | 'publicMethod'       | []                                           | PrimitiveValue.of(1.0d)
        ''                     | 'publicMethod'       | [DOUBLE_LIT, BOOL_LIT_TRUE]                  | PrimitiveValue.of(4.0d)
        ''                     | 'publicMethod'       | [DOUBLE_LIT, BOOL_LIT_FALSE]                 | PrimitiveValue.of(1.0d)
        ''                     | 'publicStaticMethod' | []                                           | PrimitiveValue.of(1)
        ''                     | 'publicStaticMethod' | [new NumberValueLiteral('7'), BOOL_LIT_TRUE] | PrimitiveValue.of(7)
        ''                     | 'publicStaticMethod' | [NUMBER_LIT, BOOL_LIT_FALSE]                 | PrimitiveValue.of(1)
        'method_call_val'      | 'toString'           | []                                           | ObjectValue.of('1')
        'method_call_val_prim' | 'toString'           | []                                           | ObjectValue.of('1')
    }

    def 'test values mismatch visit method call should throw exception'() {
        given:
        def expected = ValueException.valuesMismatch(ClassValue.of(TestClass),
                TestClass.getMethod('publicMethod', double, Boolean),
                new ParameterValues([Value.of(true), Value.of(true)])).message

        when:
        this.executor.visitMethodCall(new ThisLiteral(), 'publicMethod', new MethodInvocation([
                new BooleanValueLiteral('true'),
                new BooleanValueLiteral('true'),
        ]))

        then:
        def e = thrown(ExecutorException)
        e.message == expected
    }

    def 'test not found visit method call should throw exception'() {
        given:
        def expected = ValueException.methodNotFound(ClassValue.of(TestClass), 'not_existing', new ParameterValues([])).message

        when:
        this.executor.visitMethodCall(new ThisLiteral(), 'not_existing', new MethodInvocation([]))

        then:
        def e = thrown(ExecutorException)
        e.message == expected
    }

    def 'test visit field of #field should return #expected'() {
        given:
        this.environment.declare(
                ObjectClassValue.of(TestClass),
                'field_var',
                ObjectValue.of(new TestClass())
        )

        and:
        def left = Literal.of('field_var')
        def right = Literal.of(field)

        when:
        def value = this.executor.visitField(left, right)

        then:
        value == expected

        where:
        field               | expected
        'publicStaticField' | PrimitiveValue.of(1)
        'publicField'       | ObjectValue.of(1.0d)
    }

    def 'test not found visit field should throw exception'() {
        given:
        def expected = ValueException.fieldNotFound(ClassValue.of(TestClass), 'not_existing').message

        when:
        this.executor.visitField(new ThisLiteral(), Literal.of('not_existing'))

        then:
        def e = thrown(ExecutorException)
        e.message == expected
    }

    def 'test visit assignment: #valueClass #name = #val should return value #expected'() {
        given:
        def literalType = Literal.of(valueClass)
        def literalName = Literal.of(name)

        when:
        this.executor.visitAssignment(literalType, literalName, val)
        def value = this.environment.lookup(name)

        then:
        value == expected

        where:
        valueClass  | name  | val            | expected
        'byte'      | 'bc'  | CHAR_LIT       | Value.of((byte) 97)
        'byte'      | 'b'   | NUMBER_LIT     | Value.of((byte) 1)
        'Byte'      | 'bWc' | CHAR_LIT       | ObjectValue.of((Byte) 97)
        'Byte'      | 'bW'  | NUMBER_LIT     | ObjectValue.of((Byte) 1)
        'short'     | 'sc'  | CHAR_LIT       | Value.of((short) 97)
        'short'     | 's'   | NUMBER_LIT     | Value.of((short) 1)
        'Short'     | 'sWc' | CHAR_LIT       | ObjectValue.of((Short) 97)
        'Short'     | 'sW'  | NUMBER_LIT     | ObjectValue.of((Short) 1)
        'char'      | 'c'   | CHAR_LIT       | Value.of((char) 'a')
        'char'      | 'ci'  | NUMBER_LIT     | Value.of((char) 1)
        'Character' | 'cW'  | CHAR_LIT       | ObjectValue.of(Character.valueOf(97 as char))
        'Character' | 'ciW' | NUMBER_LIT     | ObjectValue.of(Character.valueOf(1 as char))
        'int'       | 'ic'  | CHAR_LIT       | Value.of(97)
        'int'       | 'i'   | NUMBER_LIT     | Value.of(1)
        'Integer'   | 'iW'  | NUMBER_LIT     | ObjectValue.of((Integer) 1)
        'long'      | 'lc'  | CHAR_LIT       | Value.of((long) 97)
        'long'      | 'li'  | NUMBER_LIT     | Value.of((long) 1)
        'long'      | 'l'   | LONG_LIT       | Value.of(2L)
        'Long'      | 'lW'  | LONG_LIT       | ObjectValue.of((Long) 2L)
        'float'     | 'fc'  | CHAR_LIT       | Value.of((float) 97)
        'float'     | 'fi'  | NUMBER_LIT     | Value.of((float) 1)
        'float'     | 'fl'  | LONG_LIT       | Value.of((float) 2L)
        'float'     | 'f'   | FLOAT_LIT      | Value.of(3.0f)
        'Float'     | 'fW'  | FLOAT_LIT      | ObjectValue.of((Float) 3.0f)
        'double'    | 'dc'  | CHAR_LIT       | Value.of((double) 97)
        'double'    | 'di'  | NUMBER_LIT     | Value.of((double) 1)
        'double'    | 'dl'  | LONG_LIT       | Value.of((double) 2L)
        'double'    | 'df'  | FLOAT_LIT      | Value.of((double) 3.0f)
        'double'    | 'd'   | DOUBLE_LIT     | Value.of(4.0d)
        'Double'    | 'dW'  | DOUBLE_LIT     | ObjectValue.of((Double) 4.0d)
        'boolean'   | 'bo'  | BOOL_LIT_FALSE | BooleanValue.FALSE
        'boolean'   | 'bo'  | BOOL_LIT_TRUE  | BooleanValue.TRUE
        'Boolean'   | 'boW' | BOOL_LIT_FALSE | ObjectValue.of(false)
        'Boolean'   | 'boW' | BOOL_LIT_TRUE  | ObjectValue.of(true)
        'String'    | 'st'  | STRING_LIT     | ObjectValue.of('Hello, world!')
        'Object'    | 'o'   | BOOL_LIT_TRUE  | PrimitiveValue.of(true)
    }

    def 'test visit assignment: #valueClass #name should return value #expected'() {
        given:
        def literalType = Literal.of(valueClass)
        def literalName = Literal.of(name)

        when:
        this.executor.visitAssignment(literalType, literalName, new EmptyLiteral())
        def value = this.environment.lookup(name)

        then:
        value == expected

        where:
        valueClass  | name  | expected
        'byte'      | 'b'   | PrimitiveValue.of((byte) 0)
        'Byte'      | 'bW'  | Values.NULL_VALUE
        'short'     | 's'   | PrimitiveValue.of((short) 0)
        'Short'     | 'sW'  | Values.NULL_VALUE
        'char'      | 'c'   | PrimitiveValue.of((char) 0)
        'Character' | 'cW'  | Values.NULL_VALUE
        'int'       | 'i'   | PrimitiveValue.of(0)
        'Integer'   | 'iW'  | Values.NULL_VALUE
        'long'      | 'l'   | PrimitiveValue.of(0L)
        'Long'      | 'lW'  | Values.NULL_VALUE
        'float'     | 'f'   | PrimitiveValue.of(0.0f)
        'Float'     | 'fW'  | Values.NULL_VALUE
        'double'    | 'd'   | PrimitiveValue.of(0.0d)
        'Double'    | 'dW'  | Values.NULL_VALUE
        'boolean'   | 'bo'  | PrimitiveValue.of(false)
        'Boolean'   | 'boW' | Values.NULL_VALUE
        'String'    | 'st'  | Values.NULL_VALUE
        'Object'    | 'o'   | Values.NULL_VALUE
    }

    def 'test visit re-assignment: #name = #val should return value #expected'() {
        given:
        name += '_reassign'
        def literalName = Literal.of(name)

        and:
        this.environment.declare(
                Literal.of(valueClass).accept(this.executor).check(ClassValue),
                name, val.accept(this.executor)
        )

        when:
        def value = this.executor.visitReAssign(literalName, val)

        then:
        value == expected

        where:
        valueClass  | name  | val            | expected
        'byte'      | 'bc'  | CHAR_LIT       | Value.of((byte) 97)
        'byte'      | 'b'   | NUMBER_LIT     | Value.of((byte) 1)
        'Byte'      | 'bWc' | CHAR_LIT       | ObjectValue.of((Byte) 97)
        'Byte'      | 'bW'  | NUMBER_LIT     | ObjectValue.of((Byte) 1)
        'short'     | 'sc'  | CHAR_LIT       | Value.of((short) 97)
        'short'     | 's'   | NUMBER_LIT     | Value.of((short) 1)
        'Short'     | 'sWc' | CHAR_LIT       | ObjectValue.of((Short) 97)
        'Short'     | 'sW'  | NUMBER_LIT     | ObjectValue.of((Short) 1)
        'char'      | 'c'   | CHAR_LIT       | Value.of((char) 'a')
        'char'      | 'ci'  | NUMBER_LIT     | Value.of((char) 1)
        'Character' | 'cW'  | CHAR_LIT       | ObjectValue.of(Character.valueOf(97 as char))
        'Character' | 'ciW' | NUMBER_LIT     | ObjectValue.of(Character.valueOf(1 as char))
        'int'       | 'ic'  | CHAR_LIT       | Value.of(97)
        'int'       | 'i'   | NUMBER_LIT     | Value.of(1)
        'Integer'   | 'iW'  | NUMBER_LIT     | ObjectValue.of((Integer) 1)
        'long'      | 'lc'  | CHAR_LIT       | Value.of((long) 97)
        'long'      | 'li'  | NUMBER_LIT     | Value.of((long) 1)
        'long'      | 'l'   | LONG_LIT       | Value.of(2L)
        'Long'      | 'lW'  | LONG_LIT       | ObjectValue.of((Long) 2L)
        'float'     | 'fc'  | CHAR_LIT       | Value.of((float) 97)
        'float'     | 'fi'  | NUMBER_LIT     | Value.of((float) 1)
        'float'     | 'fl'  | LONG_LIT       | Value.of((float) 2L)
        'float'     | 'f'   | FLOAT_LIT      | Value.of(3.0f)
        'Float'     | 'fW'  | FLOAT_LIT      | ObjectValue.of((Float) 3.0f)
        'double'    | 'dc'  | CHAR_LIT       | Value.of((double) 97)
        'double'    | 'di'  | NUMBER_LIT     | Value.of((double) 1)
        'double'    | 'dl'  | LONG_LIT       | Value.of((double) 2L)
        'double'    | 'df'  | FLOAT_LIT      | Value.of((double) 3.0f)
        'double'    | 'd'   | DOUBLE_LIT     | Value.of(4.0d)
        'Double'    | 'dW'  | DOUBLE_LIT     | ObjectValue.of((Double) 4.0d)
        'boolean'   | 'bo'  | BOOL_LIT_FALSE | BooleanValue.FALSE
        'boolean'   | 'bo'  | BOOL_LIT_TRUE  | BooleanValue.TRUE
        'Boolean'   | 'boW' | BOOL_LIT_FALSE | ObjectValue.of(false)
        'Boolean'   | 'boW' | BOOL_LIT_TRUE  | ObjectValue.of(true)
        'String'    | 'st'  | STRING_LIT     | ObjectValue.of('Hello, world!')
        'Object'    | 'o'   | BOOL_LIT_TRUE  | ObjectValue.of(true)
    }

    def 'test visit re-assignment of field'() {
        given:
        def field = new Field(new ThisLiteral(), Literal.of('publicField'))
        def value = new NumberValueLiteral('3')

        when:
        def actual = this.executor.visitReAssign(field, value)

        then:
        actual == ObjectValue.of(3.0d)
    }

    def 'test visit re-assignment not declared'() {
        given:
        def varName = 'visit_re_assignment_declared'
        def name = Literal.of(varName)
        def value = NUMBER_LIT

        when:
        this.executor.visitReAssign(name, value)

        then:
        def e = thrown(ExecutorException)
        e.message == ScopeException.noSuchVariable(NamedEntity.of(varName)).message
    }

    def 'test visit increment of #variable should return #expected'() {
        given:
        this.environment.declare(PrimitiveClassValue.INT, 'i', PrimitiveValue.of(1))

        when:
        def value = this.executor.visitIncrement(before, variable)

        then:
        value == expected

        where:
        variable                                                | before | expected
        Literal.of('i')                                         | false  | PrimitiveValue.of(1)
        Literal.of('i')                                         | true   | PrimitiveValue.of(2)
        new Field(new ThisLiteral(), Literal.of('publicField')) | false  | ObjectValue.of(1.0d)
        new Field(new ThisLiteral(), Literal.of('publicField')) | true   | ObjectValue.of(2.0d)
    }

    def 'test visit decrement of #variable should return #expected'() {
        given:
        this.environment.declare(PrimitiveClassValue.INT, 'i', PrimitiveValue.of(1))

        when:
        def value = this.executor.visitDecrement(before, variable)

        then:
        value == expected

        where:
        variable                                                | before | expected
        Literal.of('i')                                         | false  | PrimitiveValue.of(1)
        Literal.of('i')                                         | true   | PrimitiveValue.of(0)
        new Field(new ThisLiteral(), Literal.of('publicField')) | false  | ObjectValue.of(1.0d)
        new Field(new ThisLiteral(), Literal.of('publicField')) | true   | ObjectValue.of(0.0d)
    }

    def 'test visit dynamic array'() {
        given:
        def value = this.executor.visitDynamicArray(
                Arrays.asList(BOOL_LIT_TRUE, BOOL_LIT_FALSE),
                new ArrayLiteral(Literal.of('boolean'))
        )

        and:
        def expected = ArrayValue.of(PrimitiveClassValue.BOOLEAN,
                [BooleanValue.TRUE, BooleanValue.FALSE])

        expect:
        value == expected
    }

    def 'test visit static array'() {
        given:
        def value = this.executor.visitStaticArray(
                1,
                Literal.of('boolean')
        )

        and:
        def expected = ArrayValue.of(PrimitiveClassValue.BOOLEAN, 1)

        expect:
        value == expected
    }

    def 'test visit #operation of #literal should return #value and update variable to #expected'() {
        given:
        this.environment.declare(PrimitiveClassValue.INT, 'i', PrimitiveValue.of(1))

        when:
        def actual = this.executor."visit${operation}"(literal.before, literal.operand)
        def declared = this.environment.lookup('i')

        then:
        actual == value
        declared == expected

        where:
        operation   | literal                               | value                | expected
        'Increment' | new Increment(Literal.of('i'), true)  | PrimitiveValue.of(2) | PrimitiveValue.of(2)
        'Increment' | new Increment(Literal.of('i'), false) | PrimitiveValue.of(1) | PrimitiveValue.of(2)
        'Decrement' | new Decrement(Literal.of('i'), true)  | PrimitiveValue.of(0) | PrimitiveValue.of(0)
        'Decrement' | new Decrement(Literal.of('i'), false) | PrimitiveValue.of(1) | PrimitiveValue.of(0)
    }

    def 'test ternary operator'() {
        given:
        def result = this.executor.visitTernaryOperator(expression, first, second)

        expect:
        result == expected

        where:
        expression     | first      | second     | expected
        BOOL_LIT_TRUE  | NUMBER_LIT | STRING_LIT | NUMBER_LIT
        BOOL_LIT_FALSE | NUMBER_LIT | STRING_LIT | STRING_LIT
    }

    def 'test equal'() {
        given:
        def result = this.executor.visitEqual(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        NUMBER_LIT | NUMBER_LIT | BooleanValue.TRUE
        STRING_LIT | STRING_LIT | BooleanValue.TRUE
        CHAR_LIT   | NUMBER_LIT | BooleanValue.FALSE
        STRING_LIT | NUMBER_LIT | BooleanValue.FALSE
        NUMBER_LIT | STRING_LIT | BooleanValue.FALSE
    }

    def 'test not equal'() {
        given:
        def result = this.executor.visitNotEqual(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        NUMBER_LIT | NUMBER_LIT | BooleanValue.FALSE
        CHAR_LIT   | NUMBER_LIT | BooleanValue.TRUE
    }

    def 'test less than'() {
        given:
        def result = this.executor.visitLessThan(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | BooleanValue.FALSE
        NUMBER_LIT | LONG_LIT   | BooleanValue.TRUE
        NUMBER_LIT | NUMBER_LIT | BooleanValue.FALSE
    }

    def 'test less than equal'() {
        given:
        def result = this.executor.visitLessThanEqual(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | BooleanValue.FALSE
        NUMBER_LIT | LONG_LIT   | BooleanValue.TRUE
        NUMBER_LIT | NUMBER_LIT | BooleanValue.TRUE
    }

    def 'test greater than'() {
        given:
        def result = this.executor.visitGreaterThan(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | BooleanValue.TRUE
        NUMBER_LIT | LONG_LIT   | BooleanValue.FALSE
        NUMBER_LIT | NUMBER_LIT | BooleanValue.FALSE
    }

    def 'test greater than equal'() {
        given:
        def result = this.executor.visitGreaterThanEqual(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | BooleanValue.TRUE
        NUMBER_LIT | LONG_LIT   | BooleanValue.FALSE
        NUMBER_LIT | NUMBER_LIT | BooleanValue.TRUE
    }

    def 'test and'() {
        given:
        def result = this.executor.visitAnd(first, second)

        expect:
        result == expected

        where:
        first          | second         | expected
        BOOL_LIT_TRUE  | BOOL_LIT_TRUE  | BooleanValue.TRUE
        BOOL_LIT_FALSE | BOOL_LIT_TRUE  | BooleanValue.FALSE
        BOOL_LIT_TRUE  | BOOL_LIT_FALSE | BooleanValue.FALSE
        BOOL_LIT_FALSE | BOOL_LIT_FALSE | BooleanValue.FALSE
    }

    def 'test or'() {
        given:
        def result = this.executor.visitOr(first, second)

        expect:
        result == expected

        where:
        first          | second         | expected
        BOOL_LIT_TRUE  | BOOL_LIT_TRUE  | BooleanValue.TRUE
        BOOL_LIT_FALSE | BOOL_LIT_TRUE  | BooleanValue.TRUE
        BOOL_LIT_TRUE  | BOOL_LIT_FALSE | BooleanValue.TRUE
        BOOL_LIT_FALSE | BOOL_LIT_FALSE | BooleanValue.FALSE
    }

    def 'test bit and'() {
        given:
        def result = this.executor.visitBitAnd(first, second)

        expect:
        result == expected

        where:
        first         | second         | expected
        BOOL_LIT_TRUE | BOOL_LIT_FALSE | PrimitiveValue.of(true & false)
        NUMBER_LIT    | NUMBER_LIT     | PrimitiveValue.of(1 & 1)
    }

    def 'test bit or'() {
        given:
        def result = this.executor.visitBitOr(first, second)

        expect:
        result == expected

        where:
        first         | second         | expected
        BOOL_LIT_TRUE | BOOL_LIT_FALSE | PrimitiveValue.of(true | false)
        NUMBER_LIT    | NUMBER_LIT     | PrimitiveValue.of(1 | 1)
    }

    def 'test bit xor'() {
        given:
        def result = this.executor.visitBitXor(first, second)

        expect:
        result == expected

        where:
        first         | second         | expected
        BOOL_LIT_TRUE | BOOL_LIT_FALSE | PrimitiveValue.of(true)
        NUMBER_LIT    | NUMBER_LIT     | PrimitiveValue.of(1 ^ 1)
    }

    def 'test lshift'() {
        given:
        def result = this.executor.visitLShift(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | PrimitiveValue.of(2L << 1)
        NUMBER_LIT | LONG_LIT   | PrimitiveValue.of((1 << 2L) as long)
    }

    def 'test rshift'() {
        given:
        def result = this.executor.visitRShift(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | PrimitiveValue.of(2L >> 1)
        NUMBER_LIT | LONG_LIT   | PrimitiveValue.of((1 >> 2L) as long)
    }

    def 'test urshift'() {
        given:
        def result = this.executor.visitURShift(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | PrimitiveValue.of(2L >>> 1)
        NUMBER_LIT | LONG_LIT   | PrimitiveValue.of((1 >>> 2L) as long)
    }

    def 'test #operation of #first, #second should throw exception'() {
        when:
        this.executor."visit${operation}"(first, second)

        then:
        thrown(UnsupportedOperationException)

        where:
        operation | first                          | second
        'LShift'  | new DoubleValueLiteral('2.0d') | new NumberValueLiteral('1')
        'LShift'  | new FloatValueLiteral('3.0f')  | new NumberValueLiteral('1')
        'LShift'  | new NumberValueLiteral('1')    | new DoubleValueLiteral('2.0d')
        'LShift'  | new NumberValueLiteral('1')    | new FloatValueLiteral('3.0f')
        'RShift'  | new DoubleValueLiteral('2.0d') | new NumberValueLiteral('1')
        'RShift'  | new FloatValueLiteral('3.0f')  | new NumberValueLiteral('1')
        'RShift'  | new NumberValueLiteral('1')    | new DoubleValueLiteral('2.0d')
        'RShift'  | new NumberValueLiteral('1')    | new FloatValueLiteral('3.0f')
        'URShift' | new DoubleValueLiteral('2.0d') | new NumberValueLiteral('1')
        'URShift' | new FloatValueLiteral('3.0f')  | new NumberValueLiteral('1')
        'URShift' | new NumberValueLiteral('1')    | new DoubleValueLiteral('2.0d')
        'URShift' | new NumberValueLiteral('1')    | new FloatValueLiteral('3.0f')
    }

    def 'test add'() {
        given:
        def result = this.executor.visitAdd(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | PrimitiveValue.of(2L + 1)
        FLOAT_LIT  | NUMBER_LIT | PrimitiveValue.of((3.0f + 1) as float)
        DOUBLE_LIT | NUMBER_LIT | PrimitiveValue.of(4.0d + 1)
    }

    def 'test subtract'() {
        given:
        def result = this.executor.visitSubtract(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | PrimitiveValue.of(2L - 1)
        FLOAT_LIT  | NUMBER_LIT | PrimitiveValue.of((3.0f - 1) as float)
        DOUBLE_LIT | NUMBER_LIT | PrimitiveValue.of(4.0d - 1)
    }

    def 'test multiply'() {
        given:
        def result = this.executor.visitMultiply(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | PrimitiveValue.of(2L * 1)
        FLOAT_LIT  | NUMBER_LIT | PrimitiveValue.of((3.0f * 1) as float)
        DOUBLE_LIT | NUMBER_LIT | PrimitiveValue.of(4.0d * 1)
    }

    def 'test divide'() {
        given:
        def result = this.executor.visitDivide(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | PrimitiveValue.of(2L)
        FLOAT_LIT  | NUMBER_LIT | PrimitiveValue.of(3.0f)
        DOUBLE_LIT | NUMBER_LIT | PrimitiveValue.of(4.0d)
    }

    def 'test modulo'() {
        given:
        def result = this.executor.visitModulo(first, second)

        expect:
        result == expected

        where:
        first      | second     | expected
        LONG_LIT   | NUMBER_LIT | PrimitiveValue.of(2L % 1)
        FLOAT_LIT  | NUMBER_LIT | PrimitiveValue.of((3.0f % 1) as float)
        DOUBLE_LIT | NUMBER_LIT | PrimitiveValue.of(4.0d % 1)
    }

    def 'test minus'() {
        given:
        def result = this.executor.visitMinus(operand)

        expect:
        result == expected

        where:
        operand    | expected
        NUMBER_LIT | PrimitiveValue.of(-1)
        LONG_LIT   | PrimitiveValue.of(-2L)
        FLOAT_LIT  | PrimitiveValue.of(-3.0f)
        DOUBLE_LIT | PrimitiveValue.of(-4.0d)
    }

    def 'test not'() {
        given:
        def result = this.executor.visitNot(operand)

        expect:
        result == expected

        where:
        operand        | expected
        BOOL_LIT_TRUE  | BooleanValue.FALSE
        BOOL_LIT_FALSE | BooleanValue.TRUE
    }

    def 'test visit this should return this object'() {
        when:
        def value = this.executor.visitThisLiteral()

        then:
        value == ObjectValue.of(new TestClass())
    }

    def 'test visit cast of array'() {
        given:
        def array = new StaticArray(Literal.of('int'), new NumberValueLiteral('0'))

        and:
        def cast = new ArrayLiteral(Literal.of('int'))

        when:
        def value = this.executor.visitCast(cast, array)

        then:
        value == ArrayValue.of(PrimitiveClassValue.INT, 0)
    }

    def 'test visit cast #target to #cast should be #expected'() {
        given:
        def value = this.executor.visitCast(cast, target)

        expect:
        value == expected

        where:
        target                                     | cast                                | expected
        // char
        CHAR_LIT                                   | Literal.of('byte')                  | PrimitiveValue.of((byte) 'a')
        CHAR_LIT                                   | Literal.of('short')                 | PrimitiveValue.of((short) 'a')
        CHAR_LIT                                   | Literal.of('char')                  | PrimitiveValue.of((char) 'a')
        CHAR_LIT                                   | Literal.of('Character')             | ObjectValue.of((Character) 'a')
        CHAR_LIT                                   | Literal.of('int')                   | PrimitiveValue.of((int) 'a')
        CHAR_LIT                                   | Literal.of('long')                  | PrimitiveValue.of((long) 'a')
        CHAR_LIT                                   | Literal.of('float')                 | PrimitiveValue.of((float) 'a')
        CHAR_LIT                                   | Literal.of('double')                | PrimitiveValue.of((double) 'a')
        CHAR_LIT                                   | Literal.of('Object')                | ObjectValue.of((char) 'a')
        // number
        NUMBER_LIT                                 | Literal.of('byte')                  | PrimitiveValue.of((byte) 1)
        NUMBER_LIT                                 | Literal.of('short')                 | PrimitiveValue.of((short) 1)
        NUMBER_LIT                                 | Literal.of('char')                  | PrimitiveValue.of((char) 1)
        NUMBER_LIT                                 | Literal.of('int')                   | PrimitiveValue.of(1)
        NUMBER_LIT                                 | Literal.of('Integer')               | ObjectValue.of((Integer) 1)
        NUMBER_LIT                                 | Literal.of('long')                  | PrimitiveValue.of((long) 1)
        NUMBER_LIT                                 | Literal.of('float')                 | PrimitiveValue.of((float) 1)
        NUMBER_LIT                                 | Literal.of('double')                | PrimitiveValue.of((double) 1)
        NUMBER_LIT                                 | Literal.of('Object')                | ObjectValue.of(1)
        // long
        LONG_LIT                                   | Literal.of('byte')                  | PrimitiveValue.of((byte) 2L)
        LONG_LIT                                   | Literal.of('short')                 | PrimitiveValue.of((short) 2L)
        LONG_LIT                                   | Literal.of('char')                  | PrimitiveValue.of((char) 2L)
        LONG_LIT                                   | Literal.of('int')                   | PrimitiveValue.of((int) 2L)
        LONG_LIT                                   | Literal.of('long')                  | PrimitiveValue.of(2L)
        LONG_LIT                                   | Literal.of('Long')                  | ObjectValue.of(2L)
        LONG_LIT                                   | Literal.of('float')                 | PrimitiveValue.of((float) 2L)
        LONG_LIT                                   | Literal.of('double')                | PrimitiveValue.of((double) 2L)
        LONG_LIT                                   | Literal.of('Object')                | ObjectValue.of(2L)
        // float
        FLOAT_LIT                                  | Literal.of('byte')                  | PrimitiveValue.of((byte) 3.0f)
        FLOAT_LIT                                  | Literal.of('short')                 | PrimitiveValue.of((short) 3.0f)
        FLOAT_LIT                                  | Literal.of('char')                  | PrimitiveValue.of((char) 3.0f)
        FLOAT_LIT                                  | Literal.of('int')                   | PrimitiveValue.of((int) 3.0f)
        FLOAT_LIT                                  | Literal.of('long')                  | PrimitiveValue.of((long) 3.0f)
        FLOAT_LIT                                  | Literal.of('float')                 | PrimitiveValue.of(3.0f)
        FLOAT_LIT                                  | Literal.of('Float')                 | ObjectValue.of(3.0f)
        FLOAT_LIT                                  | Literal.of('double')                | PrimitiveValue.of((double) 3.0f)
        FLOAT_LIT                                  | Literal.of('Object')                | ObjectValue.of(3.0f)
        // double
        DOUBLE_LIT                                 | Literal.of('byte')                  | PrimitiveValue.of((byte) 4.0d)
        DOUBLE_LIT                                 | Literal.of('short')                 | PrimitiveValue.of((short) 4.0d)
        DOUBLE_LIT                                 | Literal.of('char')                  | PrimitiveValue.of((char) 4.0d)
        DOUBLE_LIT                                 | Literal.of('int')                   | PrimitiveValue.of((int) 4.0d)
        DOUBLE_LIT                                 | Literal.of('long')                  | PrimitiveValue.of((long) 4.0d)
        DOUBLE_LIT                                 | Literal.of('float')                 | PrimitiveValue.of((float) 4.0d)
        DOUBLE_LIT                                 | Literal.of('double')                | PrimitiveValue.of(4.0d)
        DOUBLE_LIT                                 | Literal.of('Double')                | ObjectValue.of(4.0d)
        DOUBLE_LIT                                 | Literal.of('Object')                | ObjectValue.of(4.0d)
        // boolean
        BOOL_LIT_TRUE                              | Literal.of('boolean')               | PrimitiveValue.of(true)
        BOOL_LIT_FALSE                             | Literal.of('boolean')               | PrimitiveValue.of(false)
        BOOL_LIT_TRUE                              | Literal.of('Boolean')               | ObjectValue.of(true)
        BOOL_LIT_FALSE                             | Literal.of('Boolean')               | ObjectValue.of(false)
        BOOL_LIT_TRUE                              | Literal.of('Object')                | ObjectValue.of(true)
        BOOL_LIT_FALSE                             | Literal.of('Object')                | ObjectValue.of(false)
        // string
        STRING_LIT                                 | Literal.of('String')                | ObjectValue.of('Hello, world!')
        STRING_LIT                                 | Literal.of('Object')                | ObjectValue.of('Hello, world!')
        // custom class
        new NewObject(Literal.of(TestClass.canonicalName),
                new MethodInvocation([]))          | Literal.of(TestClass.canonicalName) |
                ObjectValue.of((Object) new TestClass())
        new NewObject(Literal.of(TestClass.canonicalName),
                new MethodInvocation([]))          | Literal.of('Object')                |
                ObjectValue.of(new TestClass())
    }

    def 'test conversion of #literal should return #expected'() {
        when:
        def converted = literal.accept(this.executor)

        then:
        converted == expected

        where:
        literal                           | expected
        CHAR_LIT                          | PrimitiveValue.of('a' as char)
        // int
        NUMBER_LIT                        | PrimitiveValue.of(1)
        // long
        new LongValueLiteral('10L')       | PrimitiveValue.of(10L as Long)
        new LongValueLiteral('10l')       | PrimitiveValue.of(10l as Long)
        new LongValueLiteral('10')        | PrimitiveValue.of(10 as Long)
        // float
        new FloatValueLiteral('2.1E2f')   | PrimitiveValue.of(2.1E2f as Float)
        new FloatValueLiteral('2.1E-2f')  | PrimitiveValue.of(2.1E-2f as Float)
        new FloatValueLiteral('2.1E2F')   | PrimitiveValue.of(2.1E2F as Float)
        new FloatValueLiteral('2.1E-2F')  | PrimitiveValue.of(2.1E-2F as Float)
        new FloatValueLiteral('2.1E2')    | PrimitiveValue.of(2.1E2 as Float)
        new FloatValueLiteral('2.1E-2')   | PrimitiveValue.of(2.1E-2 as Float)
        new FloatValueLiteral('2.1f')     | PrimitiveValue.of(2.1f as Float)
        new FloatValueLiteral('2.1f')     | PrimitiveValue.of(2.1f as Float)
        new FloatValueLiteral('2.1F')     | PrimitiveValue.of(2.1F as Float)
        new FloatValueLiteral('2.1F')     | PrimitiveValue.of(2.1F as Float)
        new FloatValueLiteral('2.1')      | PrimitiveValue.of(2.1 as Float)
        new FloatValueLiteral('2.1')      | PrimitiveValue.of(2.1 as Float)
        new FloatValueLiteral('2f')       | PrimitiveValue.of(2f as Float)
        new FloatValueLiteral('2f')       | PrimitiveValue.of(2f as Float)
        new FloatValueLiteral('2F')       | PrimitiveValue.of(2F as Float)
        new FloatValueLiteral('2F')       | PrimitiveValue.of(2F as Float)
        new FloatValueLiteral('2')        | PrimitiveValue.of(2 as Float)
        new FloatValueLiteral('2')        | PrimitiveValue.of(2 as Float)
        // double
        new DoubleValueLiteral('2.1E2d')  | PrimitiveValue.of(2.1E2d as Double)
        new DoubleValueLiteral('2.1E-2d') | PrimitiveValue.of(2.1E-2d as Double)
        new DoubleValueLiteral('2.1E2D')  | PrimitiveValue.of(2.1E2D as Double)
        new DoubleValueLiteral('2.1E-2D') | PrimitiveValue.of(2.1E-2D as Double)
        new DoubleValueLiteral('2.1E2')   | PrimitiveValue.of(2.1E2 as Double)
        new DoubleValueLiteral('2.1E-2')  | PrimitiveValue.of(2.1E-2 as Double)
        new DoubleValueLiteral('2.1d')    | PrimitiveValue.of(2.1d as Double)
        new DoubleValueLiteral('2.1d')    | PrimitiveValue.of(2.1d as Double)
        new DoubleValueLiteral('2.1D')    | PrimitiveValue.of(2.1D as Double)
        new DoubleValueLiteral('2.1D')    | PrimitiveValue.of(2.1D as Double)
        new DoubleValueLiteral('2.1')     | PrimitiveValue.of(2.1 as Double)
        new DoubleValueLiteral('2.1')     | PrimitiveValue.of(2.1 as Double)
        new DoubleValueLiteral('2d')      | PrimitiveValue.of(2d as Double)
        new DoubleValueLiteral('2d')      | PrimitiveValue.of(2d as Double)
        new DoubleValueLiteral('2D')      | PrimitiveValue.of(2D as Double)
        new DoubleValueLiteral('2D')      | PrimitiveValue.of(2D as Double)
        new DoubleValueLiteral('2')       | PrimitiveValue.of(2 as Double)
        new DoubleValueLiteral('2')       | PrimitiveValue.of(2 as Double)
        // boolean
        BOOL_LIT_TRUE                     | BooleanValue.TRUE
        BOOL_LIT_FALSE                    | BooleanValue.FALSE
        // String
        STRING_LIT                        | ObjectValue.of('Hello, world!')
    }

    def 'test visitScoped with exception #exception should throw #expected'() {
        given:
        Callable<Value<?>> function = () -> {
            throw exception.newInstance('')
        }

        when:
        this.executor.visitScoped(ScopeType.CODE_BLOCK, function)

        then:
        thrown(expected)

        where:
        exception                | expected
        IllegalArgumentException | IllegalArgumentException
        IOException              | ExecutorException
    }

}