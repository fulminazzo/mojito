package it.fulminazzo.mojito.visitors

import it.fulminazzo.fulmicollection.objects.Refl
import it.fulminazzo.fulmicollection.structures.tuples.Tuple
import it.fulminazzo.fulmicollection.utils.ClassUtils
import it.fulminazzo.fulmicollection.utils.ReflectionUtils
import it.fulminazzo.fulmicollection.utils.StringUtils
import it.fulminazzo.mojito.environment.MockEnvironment
import it.fulminazzo.mojito.environment.NamedEntity
import it.fulminazzo.mojito.environment.ScopeException
import it.fulminazzo.mojito.environment.scopetypes.ScopeType
import it.fulminazzo.mojito.handler.Handler
import it.fulminazzo.mojito.handler.HandlerException
import it.fulminazzo.mojito.handler.elements.ClassElement
import it.fulminazzo.mojito.handler.elements.Element
import it.fulminazzo.mojito.handler.elements.ElementException
import it.fulminazzo.mojito.handler.elements.ParameterElements
import it.fulminazzo.mojito.parser.node.Assignment
import it.fulminazzo.mojito.parser.node.MethodInvocation
import it.fulminazzo.mojito.parser.node.MockNode
import it.fulminazzo.mojito.parser.node.Node
import it.fulminazzo.mojito.parser.node.container.JavaProgram
import it.fulminazzo.mojito.parser.node.literals.EmptyLiteral
import it.fulminazzo.mojito.parser.node.literals.Literal
import it.fulminazzo.mojito.parser.node.literals.NullLiteral
import it.fulminazzo.mojito.parser.node.literals.ThisLiteral
import it.fulminazzo.mojito.parser.node.operations.binary.Field
import it.fulminazzo.mojito.parser.node.statements.Return
import it.fulminazzo.mojito.parser.node.statements.Statement
import it.fulminazzo.mojito.parser.node.values.*
import it.fulminazzo.mojito.tokenizer.TokenType
import it.fulminazzo.mojito.TestClass
import spock.lang.Specification

import java.lang.reflect.Modifier
import java.util.stream.Collectors

class VisitorTest extends Specification {
    private Visitor visitor
    private MockEnvironment environment

    void setup() {
        this.visitor = new Handler(new TestClass())
        this.environment = this.visitor.environment as MockEnvironment
    }

    static writeMethod(def methodName, Object[] fieldParameters) {
        def cwd = System.getProperty('user.dir')
        def path = "${VisitorTest.package.name.replace('.', File.separator)}"
        def file = new File(cwd, "src/main/java/${path}${File.separator}${Visitor.simpleName}.java")

        def lines = file.readLines()
        def toWrite = lines.subList(0, lines.size() - 2)
        def stringParameters = fieldParameters.collect {
            def value = "${it.type.simpleName} ${it.name}"
            if (!ReflectionUtils.isPrimitive(it.type)) value = '@NotNull ' + value
            return value
        }.join(', ')
        toWrite.add("    @NotNull O ${methodName}(${stringParameters});\n")
        toWrite.add('\n}')

        file.delete()
        toWrite.each { file << "${it}\n" }

        println "Updated ${Visitor.simpleName} class with method ${methodName}"
    }

    static nodeClasses() {
        ClassUtils.findClassesInPackage(Node.package.name)
                .findAll { !Modifier.isAbstract(it.modifiers) }
                .findAll { !it.interface }
                .findAll { !it.simpleName.contains('Test') }
                .findAll { !it.simpleName.contains('Exception') }
                .findAll { !it.simpleName.contains('Mock') }
    }

    def "visitor should have method: visit#clazz.simpleName(#parameters.type.simpleName) "() {
        given:
        def methodName = "visit${clazz.simpleName}"

        when:
        def method = Visitor.declaredMethods
                .findAll { it.name == methodName }
                .findAll { it.parameterCount == parameters.length }
                .find { parameters.collect { f -> f.type } == it.parameterTypes.toList() }

        then:
        if (method == null) writeMethod(methodName, parameters)
        method != null

        where:
        clazz << nodeClasses()
        parameters << nodeClasses()
                .collect { new Refl<>(it) }
                .collect { it.nonStaticFields }
                .collect { it.toArray() }
    }


    def 'test visitProgram of #program should return #expected'() {
        when:
        def element = this.visitor.visitProgram(program)

        then:
        if (expected == 'none') !element.present
        else element.get() == expected

        where:
        program                                                                                             | expected
        new JavaProgram([] as LinkedList<Statement>)                                                        | 'none'
        new JavaProgram([new Statement()] as LinkedList<Statement>)                                         | 'none'
        new JavaProgram([new Return(new StringValueLiteral('\"Hello, world!\"'))] as LinkedList<Statement>) | 'Hello, world!'
        new JavaProgram([new Return(new NullLiteral())] as LinkedList<Statement>)                           | null
    }

    def 'test visit#node of #statements should return #expected'() {
        when:
        def element = this.visitor."visit${node}"(new LinkedList<>(statements))

        then:
        element == expected

        where:
        node          | statements                                | expected
        'CodeBlock'   | []                                        | Element.EMPTY
        'JavaProgram' | []                                        | Element.EMPTY
        'CodeBlock'   | [new Statement()]                         | Element.EMPTY
        'JavaProgram' | [new Statement()]                         | Element.EMPTY
        'CodeBlock'   | [new Return(new NumberValueLiteral('1'))] | Element.of(1)
        'JavaProgram' | [new Return(new NumberValueLiteral('1'))] | Element.of(1)
    }

    def 'test visitReturn(#node) should return #expected'() {
        when:
        def element = this.visitor.visitReturn(node)

        then:
        element == expected

        where:
        node                                        | expected
        new CharValueLiteral('\'a\'')               | Element.of('a' as char)
        new NumberValueLiteral('1')                 | Element.of(1)
        new LongValueLiteral('2L')                  | Element.of(2L)
        new FloatValueLiteral('3.0f')               | Element.of(3.0f)
        new DoubleValueLiteral('4.0d')              | Element.of(4.0d)
        new BooleanValueLiteral('true')             | Element.of(true)
        new BooleanValueLiteral('false')            | Element.of(false)
        new StringValueLiteral('\"Hello, world!\"') | Element.of('Hello, world!')
        new NullLiteral()                           | Element.of(null)
        new ThisLiteral()                           | Element.of(new TestClass())
    }

    def 'test visitStatement should not return anything'() {
        when:
        def element = this.visitor.visitStatement(new NumberValueLiteral('1'))

        then:
        element == this.visitor.visitEmptyLiteral()
    }

    def 'test visitAssignmentBlock'() {
        given:
        def assignments = [
                new Assignment(Literal.of('Integer'), Literal.of('i'), new NumberValueLiteral('1')),
                new Assignment(Literal.of('Integer'), Literal.of('j'), new NumberValueLiteral('2')),
                new Assignment(Literal.of('Integer'), Literal.of('k'), new NumberValueLiteral('3')),
        ]

        when:
        ParameterElements element = this.visitor.visitAssignmentBlock(assignments) as ParameterElements
        def actual = element.stream().collect(Collectors.toList())

        then:
        actual.get(0) == Element.of(1)
        this.environment.lookup('i').element == 1
        actual.get(1) == Element.of(2)
        this.environment.lookup('j').element == 2
        actual.get(2) == Element.of(3)
        this.environment.lookup('k').element == 3
    }

    def 'test visitFinalAssignment'() {
        given:
        def type = Literal.of(Integer.simpleName)
        def name = 'i'
        def value = new NumberValueLiteral('1')

        when:
        def element = this.visitor.visitFinalAssignment(new Assignment(type, Literal.of(name), value))

        then:
        element == Element.of(1)
        this.environment.lookupInfo(name) == ClassElement.of(Integer)
        this.environment.lookup(name) == Element.of(1)
    }

    def 'test visitFinalAssignment exception for JaCoCo'() {
        given:
        def type = Literal.of(Integer.simpleName)
        def name = 'i'
        def value = new NumberValueLiteral('1')

        and:
        this.environment.disableConstants()

        when:
        this.visitor.visitFinalAssignment(new Assignment(type, Literal.of(name), value))

        then:
        notThrown(ScopeException)
    }

    def 'test visitAssignment'() {
        given:
        def type = Literal.of(Integer.simpleName)
        def name = 'i'
        def value = new NumberValueLiteral('1')

        when:
        def element = this.visitor.visitAssignment(type, Literal.of(name), value)

        then:
        element == Element.of(1)
        this.environment.lookupInfo(name) == ClassElement.of(Integer)
        this.environment.lookup(name) == Element.of(1)
    }

    def 'test visitAssignment already declared'() {
        given:
        def type = Literal.of(Integer.simpleName)
        def name = 'i'
        def value = new NumberValueLiteral('1')

        and:
        this.environment.declare(ClassElement.of(Integer), name, Element.of(1))

        when:
        this.visitor.visitAssignment(type, Literal.of(name), value)

        then:
        def e = thrown(HandlerException)
        e.message == ScopeException.alreadyDeclaredVariable(NamedEntity.of(name)).message
    }

    def 'test visitReAssign #literal = #newValue should return #expected'() {
        given:
        this.environment.declare(ClassElement.of(Double), 'i', Element.of(1.0d))

        when:
        def element = this.visitor.visitReAssign(literal, newValue)
        def actual = closure(this.visitor)

        then:
        element == Element.of(2.0d)
        actual == expected

        where:
        literal                                                 | newValue                       | expected         | closure
        Literal.of('i')                                         | new DoubleValueLiteral('2.0d') | Element.of(2.0d) | { v ->
            return v.environment.lookup('i')
        }
        new Field(new ThisLiteral(), Literal.of('publicField')) | new DoubleValueLiteral('2.0d') | Element.of(2.0d) | { v ->
            return Element.of(v.executingObject.publicField)
        }
    }

    def 'test visitReAssign invalid type'() {
        given:
        this.environment.declare(ClassElement.of(Double), 'i', Element.of(1.0d))

        and:
        def name = 'i'
        def value = new StringValueLiteral('\"Hello, world!\"')

        when:
        this.visitor.visitReAssign(Literal.of(name), value)

        then:
        def e = thrown(HandlerException)
        e.message == ScopeException.cannotAssignValue(Element.of('Hello, world!'), ClassElement.of(Double)).message
    }

    def 'test visitReAssign of undeclared'() {
        given:
        def name = 'i'
        def value = new NumberValueLiteral('1')

        when:
        this.visitor.visitReAssign(Literal.of(name), value)

        then:
        def e = thrown(HandlerException)
        e.message == ScopeException.noSuchVariable(NamedEntity.of(name)).message
    }

    def 'test visitNewObject TestClass(#parameters) should have fields #i, #b'() {
        given:
        def type = Literal.of(TestClass.canonicalName)
        def methodInvocation = new MethodInvocation(parameters)

        when:
        def element = this.visitor.visitNewObject(type, methodInvocation)
        def o = element.element

        then:
        o.i == i
        o.b == b

        where:
        parameters                                                      | i | b
        []                                                              | 0 | null
        [new NumberValueLiteral('1'), new BooleanValueLiteral('true')]  | 1 | true
        [new NumberValueLiteral('2'), new BooleanValueLiteral('false')] | 2 | false
    }

    def 'test visitNewObject invalid parameters'() {
        given:
        def parameters = new ParameterElements([Element.of(1.0d), Element.of(true)])

        and:
        def expected = Element.of(null).typesMismatch(ClassElement.of(TestClass),
                TestClass.getConstructor(int, Boolean), parameters).message

        when:
        this.visitor.visitNewObject(Literal.of(TestClass.canonicalName),
                new MethodInvocation([new DoubleValueLiteral('1.0d'), new BooleanValueLiteral('true')]))

        then:
        def e = thrown(HandlerException)
        e.message == expected
    }

    def 'test visitNewObject constructor not found'() {
        given:
        def type = Literal.of(TestClass.canonicalName)
        def parameters = new MethodInvocation([
                new DoubleValueLiteral('1.0d'),
                new NumberValueLiteral('2'),
                new LongValueLiteral('3L'),
        ])

        and:
        def expected = Element.of(null).methodNotFound(ClassElement.of(TestClass),
                '<init>', new ParameterElements([
                Element.of(1.0d), Element.of(2), Element.of(3L),
        ])).message

        when:
        this.visitor.visitNewObject(type, parameters)

        then:
        def e = thrown(HandlerException)
        e.message == expected
    }

    def 'test visit#operation (#before) of #operand should return #expected'() {
        given:
        this.environment.declare(ClassElement.of(Double), 'i', Element.of(1.0d))

        when:
        def value = this.visitor."visit${operation}"(before, operand)

        then:
        value.element == expected

        where:
        operation   | before | operand                                                 | expected
        'Increment' | true   | Literal.of('i')                                         | 2.0d
        'Increment' | false  | Literal.of('i')                                         | 1.0d
        'Increment' | true   | new Field(new ThisLiteral(), Literal.of('publicField')) | 2.0d
        'Increment' | false  | new Field(new ThisLiteral(), Literal.of('publicField')) | 1.0d
        'Decrement' | true   | Literal.of('i')                                         | 0.0d
        'Decrement' | false  | Literal.of('i')                                         | 1.0d
        'Decrement' | true   | new Field(new ThisLiteral(), Literal.of('publicField')) | 0.0d
        'Decrement' | false  | new Field(new ThisLiteral(), Literal.of('publicField')) | 1.0d
    }

    def 'test visitMethodCall #executor #methodName(#parameters) should return #expected'() {
        given:
        def methodInvocation = new MethodInvocation(parameters)

        when:
        def element = this.visitor.visitMethodCall(executor, methodName, methodInvocation)

        then:
        element == expected

        where:
        executor                            | methodName           | parameters                                                       | expected
        Literal.of(TestClass.canonicalName) | 'publicStaticMethod' | []                                                               | Element.of(1)
        Literal.of(TestClass.canonicalName) | 'publicStaticMethod' | [new NumberValueLiteral('2'), new BooleanValueLiteral('true')]   | Element.of(2)
        Literal.of(TestClass.canonicalName) | 'publicStaticMethod' | [new NumberValueLiteral('1'), new BooleanValueLiteral('true')]   | Element.of(1)
        new EmptyLiteral()                  | 'publicMethod'       | []                                                               | Element.of(1.0d)
        new EmptyLiteral()                  | 'publicMethod'       | [new DoubleValueLiteral('2.0'), new BooleanValueLiteral('true')] | Element.of(2.0d)
        new EmptyLiteral()                  | 'publicMethod'       | [new DoubleValueLiteral('1.0'), new BooleanValueLiteral('true')] | Element.of(1.0d)
        new ThisLiteral()                   | 'publicMethod'       | []                                                               | Element.of(1.0d)
        new ThisLiteral()                   | 'publicMethod'       | [new DoubleValueLiteral('2.0'), new BooleanValueLiteral('true')] | Element.of(2.0d)
        new ThisLiteral()                   | 'publicMethod'       | [new DoubleValueLiteral('1.0'), new BooleanValueLiteral('true')] | Element.of(1.0d)
        new NumberValueLiteral('1')         | 'toString'           | []                                                               | Element.of('1')
    }

    def 'test visitMethodCall invalid parameters'() {
        given:
        def executor = new ThisLiteral()
        def methodName = 'publicMethod'
        def parameters = new ParameterElements([Element.of(1), Element.of(true)])

        and:
        def expected = Element.of(null).typesMismatch(ClassElement.of(TestClass),
                TestClass.getMethod(methodName, double, Boolean), parameters).message

        when:
        this.visitor.visitMethodCall(executor, methodName,
                new MethodInvocation([new NumberValueLiteral('1'), new BooleanValueLiteral('true')]))

        then:
        def e = thrown(HandlerException)
        e.message == expected
    }

    def 'test visitMethodCall not found'() {
        given:
        def executor = new ThisLiteral()
        def methodName = 'invalid'
        def parameters = new ParameterElements([])

        and:
        def expected = Element.of(null).methodNotFound(ClassElement.of(TestClass),
                methodName, parameters).message

        when:
        this.visitor.visitMethodCall(executor, methodName, new MethodInvocation([]))

        then:
        def e = thrown(HandlerException)
        e.message == expected
    }

    def 'test visitField'() {
        given:
        def parent = Element.of(this.visitor.executingObject)
        def type = ClassElement.of(Double)
        def fieldName = 'publicField'
        def value = Element.of(1.0d)

        and:
        def executor = new ThisLiteral()
        def fieldLiteral = Literal.of(fieldName)

        when:
        def field = this.visitor.visitField(executor, fieldLiteral)

        then:
        field.container == parent
        field.type == type
        field.name == fieldName
        field.variable == value
    }

    def 'test visitField not found'() {
        given:
        def executor = new ThisLiteral()
        def fieldLiteral = Literal.of('invalid')

        and:
        def expected = Element.of(null).fieldNotFound(ClassElement.of(TestClass), 'invalid').message

        when:
        this.visitor.visitField(executor, fieldLiteral)

        then:
        def e = thrown(HandlerException)
        e.message == expected
    }

    def 'test visit#token(#parameters) should return #expected'() {
        given:
        def methodName = StringUtils.capitalize(token.toString())

        when:
        def e = this.visitor."visit${methodName}"(parameters)

        then:
        e.element == expected

        where:
        token              | parameters                                                       | expected
        TokenType.ADD      | [new DoubleValueLiteral('4.0d'), new DoubleValueLiteral('2.0d')] | 6.0d
        TokenType.SUBTRACT | [new DoubleValueLiteral('4.0d'), new DoubleValueLiteral('2.0d')] | 2.0d
    }

    def 'test visit#token(#parameters) should throw unsupported operation exception'() {
        given:
        def methodName = StringUtils.capitalize(token.toString())
                .replace('_', '')
                .replace('shift', 'Shift')
                .replace('rShift', 'RShift')
        if (parameters.size() < 2 && token == TokenType.SUBTRACT) methodName = 'Minus'

        and:
        def message = Element.of(null).unsupportedOperation(*[token, operands].flatten()).message

        when:
        def e = this.visitor."visit${methodName}"(parameters)

        then:
        e.element.message == message

        where:
        token                        | parameters                                                                 | operands
        // Comparisons
        TokenType.AND                | [new BooleanValueLiteral('true'), new BooleanValueLiteral('false')]        | [Element.of(true), Element.of(false)]
        TokenType.OR                 | [new BooleanValueLiteral('true'), new BooleanValueLiteral('false')]        | [Element.of(true), Element.of(false)]
        TokenType.EQUAL              | [new NumberValueLiteral('1'), new StringValueLiteral('\"Hello, world!\"')] | [Element.of(1), Element.of('Hello, world!')]
        TokenType.NOT_EQUAL          | [new DoubleValueLiteral('1.0d'), new BooleanValueLiteral('false')]         | [Element.of(1.0d), Element.of(false)]
        TokenType.LESS_THAN          | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        TokenType.LESS_THAN_EQUAL    | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        TokenType.GREATER_THAN       | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        TokenType.GREATER_THAN_EQUAL | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        // Bit operations
        TokenType.BIT_AND            | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        TokenType.BIT_OR             | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        TokenType.BIT_XOR            | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        TokenType.LSHIFT             | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        TokenType.RSHIFT             | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        TokenType.URSHIFT            | [new NumberValueLiteral('1'), new NumberValueLiteral('2')]                 | [Element.of(1), Element.of(2)]
        // Operations
        TokenType.MULTIPLY           | [new DoubleValueLiteral('4.0d'), new NumberValueLiteral('2')]              | [Element.of(4.0d), Element.of(2)]
        TokenType.DIVIDE             | [new DoubleValueLiteral('4.0d'), new NumberValueLiteral('2')]              | [Element.of(4.0d), Element.of(2)]
        TokenType.MODULO             | [new DoubleValueLiteral('4.0d'), new NumberValueLiteral('2')]              | [Element.of(4.0d), Element.of(2)]
        // Unary
        TokenType.SUBTRACT           | [new NumberValueLiteral('1')]                                              | [Element.of(1)]
        TokenType.NOT                | [new BooleanValueLiteral('true')]                                          | [Element.of(true)]
    }

    def 'test visitCast of #node should return #expected'() {
        given:
        def cast = Literal.of('Integer')

        when:
        def element = this.visitor.visitCast(cast, node)

        then:
        element == expected

        where:
        node                                                          | expected
        new NumberValueLiteral('2')                                   | Element.of(2)
        new Field(new ThisLiteral(), Literal.of('publicStaticField')) | Element.of(1)
    }

    def 'test visitScoped of #scopeType'() {
        given:
        def expected = Element.of('Hello, world!')

        when:
        def element = this.visitor.visitScoped(scopeType, () -> expected)

        then:
        element == expected
        this.environment.enteredScope(scopeType)
        this.environment.scopeType() != scopeType

        where:
        scopeType << ScopeType.values().findAll { it != ScopeType.MAIN }
    }

    def 'test visitScoped of #tuple.key should throw #tuple.value.simpleName'() {
        given:
        def scopeType = tuple.key
        def exception = tuple.value

        and:
        def message = 'this is the message'

        when:
        this.visitor.visitScoped(scopeType, () -> {
            throw exception.newInstance(message)
        })

        then:
        def e = thrown(HandlerException)
        e.message == message
        this.environment.enteredScope(scopeType)
        this.environment.scopeType() != scopeType

        where:
        tuple << ScopeType.values()
                .findAll { it != ScopeType.MAIN }
                .collectMany {
                    [ElementException, HandlerException]
                            .collect { ex -> new Tuple<>(it, ex) }
                }
    }

    def 'test accept mock node'() {
        given:
        def node = new MockNode('mock', 1)
        def visitor = new Handler(this)

        when:
        def converted = node.accept(visitor).element

        then:
        converted == "${node.name}${node.version}"
    }

}
