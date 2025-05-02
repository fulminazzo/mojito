package it.fulminazzo.mojito.parser

import it.fulminazzo.mojito.parser.node.*
import it.fulminazzo.mojito.parser.node.arrays.DynamicArray
import it.fulminazzo.mojito.parser.node.arrays.StaticArray
import it.fulminazzo.mojito.parser.node.container.CodeBlock
import it.fulminazzo.mojito.parser.node.literals.*
import it.fulminazzo.mojito.parser.node.operators.binary.*
import it.fulminazzo.mojito.parser.node.operators.ternary.TernaryOperator
import it.fulminazzo.mojito.parser.node.operators.unary.Decrement
import it.fulminazzo.mojito.parser.node.operators.unary.Increment
import it.fulminazzo.mojito.parser.node.operators.unary.Minus
import it.fulminazzo.mojito.parser.node.operators.unary.Not
import it.fulminazzo.mojito.parser.node.statements.*
import it.fulminazzo.mojito.parser.node.values.*
import it.fulminazzo.mojito.tokenizer.TokenType
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class JavaParserTest extends Specification {
    private JavaParser parser

    void setup() {
        this.parser = new JavaParser()
    }

    void startReading(final String code) {
        this.parser.input = code
        this.parser.tokenizer.nextSpaceless()
    }

    def 'parse test_program file'() {
        given:
        def cwd = System.getProperty('user.dir')

        and:
        def file = new File(cwd, 'build/resources/test/parser_test_program.java')
        def parser = new JavaParser(file.newInputStream())

        and:
        def nextTestFile = new File(cwd, 'src/test/resources/parsed_test_program.dat')
        if (nextTestFile.file) nextTestFile.delete()

        when:
        def parsed = parser.parseProgram()
        nextTestFile.withObjectOutputStream {
            it.writeObject(parsed)
        }
        def other = nextTestFile.newObjectInputStream().readObject()
        other == parsed

        then:
        noExceptionThrown()
    }

    def 'test parseBlock: #code'() {
        when:
        startReading(code)
        def output = this.parser.parseBlock()

        then:
        output == expected

        where:
        code              | expected
        '{\ncontinue;\n}' | new CodeBlock(new Continue())
        'continue;'       | new CodeBlock(new Continue())
    }

    def 'test parseSingleStatement: #code'() {
        when:
        startReading(code)
        def output = this.parser.parseSingleStatement()

        then:
        output == expected

        where:
        code        | expected
        'return 1;' | new Return(new NumberValueLiteral('1'))
        'return 1'  | new Return(new NumberValueLiteral('1'))
        'throw 1;'  | new Throw(new NumberValueLiteral('1'))
        'break;'    | new Break()
        ';'         | new Statement()
    }

    def 'test parse comments'() {
        given:
        def code = "${comment}return 1;"

        when:
        startReading(code)
        def output = this.parser.parseSingleStatement()

        then:
        output == new Return(new NumberValueLiteral('1'))

        where:
        comment << [
                '//First line\n',
                '//First line\n//Second line\n//Third line\n',
                '/*\nComment block\n*/',
                '/**\n *Javadoc block\n */',
        ]
    }

    def 'test endless comments'() {
        when:
        startReading(code)
        this.parser.parseSingleStatement()

        then:
        def e = thrown(ParserException)
        e.message == ParserException.unexpectedToken(this.parser, TokenType.EOF).message
        e.message == ParserException.unexpectedEndOfInput(this.parser).message

        where:
        code << [
                '//First line',
                '//First line\n//Second line\n//Third line',
                '/*\nComment block\n',
                '/**\n *Javadoc block\n ',
        ]
    }

    def 'test parse try statement of code: #code'() {
        when:
        startReading(code)
        def block = this.parser.parseTryStatement()

        then:
        block == expected

        where:
        code                                                                              | expected
        'try (int i = 1) {return 1;} catch (Exception e) {return 2;} finally {return 3;}' | new TryStatement(
                new AssignmentBlock([new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1'))]),
                new CodeBlock(new Return(new NumberValueLiteral('1'))),
                [new CatchStatement([Literal.of('Exception')], Literal.of('e'),
                        new CodeBlock(new Return(new NumberValueLiteral('2'))))],
                new CodeBlock(new Return(new NumberValueLiteral('3'))))
        'try {return 1;} catch (Exception e) {return 2;} finally {return 3;}'             | new TryStatement(
                new AssignmentBlock([]),
                new CodeBlock(new Return(new NumberValueLiteral('1'))),
                [new CatchStatement([Literal.of('Exception')], Literal.of('e'),
                        new CodeBlock(new Return(new NumberValueLiteral('2'))))],
                new CodeBlock(new Return(new NumberValueLiteral('3'))))
        'try (int i = 1) {return 1;} catch (Exception e) {return 2;}'                     | new TryStatement(
                new AssignmentBlock([new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1'))]),
                new CodeBlock(new Return(new NumberValueLiteral('1'))),
                [new CatchStatement([Literal.of('Exception')], Literal.of('e'),
                        new CodeBlock(new Return(new NumberValueLiteral('2'))))],
                new CodeBlock())
        'try {return 1;} catch (Exception e) {return 2;}'                                 | new TryStatement(
                new AssignmentBlock([]),
                new CodeBlock(new Return(new NumberValueLiteral('1'))),
                [new CatchStatement([Literal.of('Exception')], Literal.of('e'),
                        new CodeBlock(new Return(new NumberValueLiteral('2'))))],
                new CodeBlock())
        'try (int i = 1) {return 1;} finally {return 3;}'                                 | new TryStatement(
                new AssignmentBlock([new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1'))]),
                new CodeBlock(new Return(new NumberValueLiteral('1'))),
                [],
                new CodeBlock(new Return(new NumberValueLiteral('3'))))
        'try {return 1;} finally {return 3;}'                                             | new TryStatement(
                new AssignmentBlock([]),
                new CodeBlock(new Return(new NumberValueLiteral('1'))),
                [],
                new CodeBlock(new Return(new NumberValueLiteral('3'))))
    }

    def 'test invalid parse try statement'() {
        given:
        def code = 'try {return 1;}'

        when:
        startReading(code)
        this.parser.parseTryStatement()

        then:
        def e = thrown(ParserException)
        e.message == ParserException.invalidTryStatement(this.parser).message
    }

    def 'test parse assignment block of code: #code'() {
        when:
        startReading(code)
        def block = this.parser.parseAssignmentBlock()

        then:
        block == expected

        where:
        code                               | expected
        'int i = 1; int j = 2; int k = 3;' | new AssignmentBlock([
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1')),
                new Assignment(Literal.of('int'), Literal.of('j'), new NumberValueLiteral('2')),
                new Assignment(Literal.of('int'), Literal.of('k'), new NumberValueLiteral('3')),
        ])
        'int i = 1; int j = 2; int k = 3'  | new AssignmentBlock([
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1')),
                new Assignment(Literal.of('int'), Literal.of('j'), new NumberValueLiteral('2')),
                new Assignment(Literal.of('int'), Literal.of('k'), new NumberValueLiteral('3')),
        ])
        'int i = 1; int j = 2;'            | new AssignmentBlock([
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1')),
                new Assignment(Literal.of('int'), Literal.of('j'), new NumberValueLiteral('2')),
        ])
        'int i = 1; int j = 2'             | new AssignmentBlock([
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1')),
                new Assignment(Literal.of('int'), Literal.of('j'), new NumberValueLiteral('2')),
        ])
        'int i = 1;'                       | new AssignmentBlock([
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1')),
        ])
        'int i = 1'                        | new AssignmentBlock([
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1')),
        ])
    }

    def 'test invalid parse assignment block of code: #code'() {
        when:
        startReading(code)
        this.parser.parseAssignmentBlock()

        then:
        def e = thrown(ParserException)
        e.message == expected

        where:
        code            | expected
        'int i = 1;i++' | "At line 1, column 13: Expecting '${Assignment.simpleName}' but got " +
                "${new Increment(Literal.of('i'), false)} instead."
        'i++'           | "At line 1, column 3: Expecting '${Assignment.simpleName}' but got " +
                "${new Increment(Literal.of('i'), false)} instead."
        'i = 1'         | "At line 1, column 5: Expecting '${Assignment.simpleName}' but got " +
                "${new ReAssign(Literal.of('i'), new NumberValueLiteral('1'))} instead."
        ';'             | "At line 1, column 1: Unexpected token: ${TokenType.SEMICOLON}"
        ''              | 'At line 0, column 0: Unexpected end of input. Last read token: EOF ()'
    }

    def 'test parse catch statement of code: #code'() {
        when:
        startReading(code)
        def block = this.parser.parseCatchStatement()

        then:
        block == expected

        where:
        code                                                                      | expected
        'catch (FirstException | SecondException | ThirdException e) {return 1;}' | new CatchStatement(
                [Literal.of('FirstException'), Literal.of('SecondException'), Literal.of('ThirdException')],
                Literal.of('e'), new CodeBlock(new Return(new NumberValueLiteral('1')))
        )
        'catch (FirstException | SecondException e) {return 1;}'                  | new CatchStatement(
                [Literal.of('FirstException'), Literal.of('SecondException')],
                Literal.of('e'), new CodeBlock(new Return(new NumberValueLiteral('1')))
        )
        'catch (FirstException e) {return 1;}'                                    | new CatchStatement(
                [Literal.of('FirstException')],
                Literal.of('e'), new CodeBlock(new Return(new NumberValueLiteral('1')))
        )
    }

    def 'test parse invalid catch statement'() {
        given:
        def code = 'catch(Exception | 1)'

        when:
        startReading(code)
        this.parser.parseCatchStatement()

        then:
        thrown(ParserException)
    }

    def 'test parse switch statement of code: #code'() {
        when:
        startReading(code)
        def block = this.parser.parseSwitchStatement()

        then:
        block == expected

        where:
        code                                                                  | expected
        'switch (1) {case 1: return 1; case 2: return 2; default: return 3;}' |
                new SwitchStatement(new NumberValueLiteral('1'),
                        [
                                new CaseStatement(new NumberValueLiteral('1'), new CodeBlock(new Return(new NumberValueLiteral('1')))),
                                new CaseStatement(new NumberValueLiteral('2'), new CodeBlock(new Return(new NumberValueLiteral('2')))),
                        ],
                        new CodeBlock(new Return(new NumberValueLiteral('3'))))
        'switch (1) {case 1: return 1; default: return 3; case 2: return 2;}' |
                new SwitchStatement(new NumberValueLiteral('1'),
                        [
                                new CaseStatement(new NumberValueLiteral('1'), new CodeBlock(new Return(new NumberValueLiteral('1')))),
                                new CaseStatement(new NumberValueLiteral('2'), new CodeBlock(new Return(new NumberValueLiteral('2')))),
                        ],
                        new CodeBlock(new Return(new NumberValueLiteral('3'))))
        'switch (1) {case 1: return 1; default: return 3;}'                   |
                new SwitchStatement(new NumberValueLiteral('1'),
                        [
                                new CaseStatement(new NumberValueLiteral('1'), new CodeBlock(new Return(new NumberValueLiteral('1')))),
                        ],
                        new CodeBlock(new Return(new NumberValueLiteral('3'))))
        'switch (1) {case 1: return 1;}'                                      |
                new SwitchStatement(new NumberValueLiteral('1'),
                        [
                                new CaseStatement(new NumberValueLiteral('1'), new CodeBlock(new Return(new NumberValueLiteral('1')))),
                        ],
                        new CodeBlock())
        'switch (1) {default: return 3;}'                                     |
                new SwitchStatement(new NumberValueLiteral('1'),
                        [],
                        new CodeBlock(new Return(new NumberValueLiteral('3'))))
        'switch (1) {}'                                                       |
                new SwitchStatement(new NumberValueLiteral('1'),
                        [],
                        new CodeBlock())
    }

    def 'test parse switch of same cases should throw exception'() {
        given:
        def code = 'switch(1) { case 1: return 1; case 1: return 2; }'

        when:
        startReading(code)
        this.parser.parseSwitchStatement()

        then:
        def e = thrown(ParserException)
        e.message == ParserException.caseBlockAlreadyDefined(this.parser,
                new CaseStatement(new NumberValueLiteral('1'),
                        new CodeBlock(new Return(new NumberValueLiteral('1'))))).message
    }

    def 'test parse switch of dual defaults should throw exception'() {
        given:
        def code = 'switch(1) { default: return 1; default: return 2; }'

        when:
        startReading(code)
        this.parser.parseSwitchStatement()

        then:
        def e = thrown(ParserException)
        e.message == ParserException.defaultBlockAlreadyDefined(this.parser).message
    }

    def 'test parse case block of code: #code'() {
        when:
        startReading(code)
        def block = this.parser.parseCaseBlock()

        then:
        block == expected

        where:
        expected                                                                                                   | code
        new CaseStatement(new BooleanValueLiteral('true'), new CodeBlock(new Return(new NumberValueLiteral('1')))) |
                'case true: return 1;}'
        new CaseStatement(new BooleanValueLiteral('true'), new CodeBlock(new Return(new NumberValueLiteral('1')))) |
                'case true: {return 1;}}'
    }

    def 'test parse default block of code: #code'() {
        when:
        startReading(code)
        def block = this.parser.parseDefaultBlock()

        then:
        block == expected

        where:
        expected                                               | code
        new CodeBlock(new Return(new NumberValueLiteral('1'))) | 'default: return 1;}'
        new CodeBlock(new Return(new NumberValueLiteral('1'))) | 'default: {return 1;}}'
    }

    def 'test invalid for statement'() {
        given:
        def code = 'for (int i; i < 10; i++)'

        when:
        startReading(code)
        this.parser.parseForStatement()

        then:
        def e = thrown(ParserException)
        e.message == ParserException.unexpectedToken(this.parser, TokenType.EOF).message
        e.message == ParserException.unexpectedEndOfInput(this.parser).message
    }

    def 'test for statements'() {
        when:
        startReading(code)
        def output = this.parser.parseSingleStatement()

        then:
        output == expected

        where:
        code                                   | expected
        'for (int i = 0; true; i++) continue;' | new ForStatement(
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('0')),
                new BooleanValueLiteral('true'),
                new Increment(Literal.of('i'), false),
                new CodeBlock(new Continue())
        )
        'for (i = 0; true; i++) continue;'     | new ForStatement(
                new ReAssign(Literal.of('i'), new NumberValueLiteral('0')),
                new BooleanValueLiteral('true'),
                new Increment(Literal.of('i'), false),
                new CodeBlock(new Continue())
        )
        'for (; true; i++) continue;'          | new ForStatement(
                new EmptyLiteral(),
                new BooleanValueLiteral('true'),
                new Increment(Literal.of('i'), false),
                new CodeBlock(new Continue())
        )
        'for (int i = 0; ; i++) continue;'     | new ForStatement(
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('0')),
                new EmptyLiteral(),
                new Increment(Literal.of('i'), false),
                new CodeBlock(new Continue())
        )
        'for (int i = 0; true; ) continue;'    | new ForStatement(
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('0')),
                new BooleanValueLiteral('true'),
                new EmptyLiteral(),
                new CodeBlock(new Continue())
        )
        'for (; ; i++) continue;'              | new ForStatement(
                new EmptyLiteral(),
                new EmptyLiteral(),
                new Increment(Literal.of('i'), false),
                new CodeBlock(new Continue())
        )
        'for (int i = 0; ; ) continue;'        | new ForStatement(
                new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('0')),
                new EmptyLiteral(),
                new EmptyLiteral(),
                new CodeBlock(new Continue())
        )
        'for (int i : arr) continue;'          | new EnhancedForStatement(
                Literal.of('int'),
                Literal.of('i'),
                Literal.of('arr'),
                new CodeBlock(new Continue())
        )
    }

    def 'test flow control statement: #expected.class.simpleName'() {
        when:
        startReading(code)
        def output = this.parser.parseSingleStatement()

        then:
        output == expected

        where:
        code                         | expected
        'while (true) continue;'     | new WhileStatement(new BooleanValueLiteral('true'), new CodeBlock(new Continue()))
        'do continue; while (true);' | new DoStatement(new BooleanValueLiteral('true'), new CodeBlock(new Continue()))
    }

    def 'test if statement: #code'() {
        when:
        startReading(code)
        def output = this.parser.parseSingleStatement()

        then:
        output == expected

        where:
        code                                                        | expected
        'if (true) continue;'                                       | new IfStatement(
                new BooleanValueLiteral('true'),
                new CodeBlock(new Continue()),
                new EmptyLiteral()
        )
        'if (true) continue; else if (false) break;'                | new IfStatement(
                new BooleanValueLiteral('true'),
                new CodeBlock(new Continue()),
                new IfStatement(
                        new BooleanValueLiteral('false'),
                        new CodeBlock(new Break()),
                        new EmptyLiteral()
                )
        )
        'if (true) continue; else if (false) break; else return 1;' | new IfStatement(
                new BooleanValueLiteral('true'),
                new CodeBlock(new Continue()),
                new IfStatement(
                        new BooleanValueLiteral('false'),
                        new CodeBlock(new Break()),
                        new CodeBlock(
                                new Return(new NumberValueLiteral('1'))
                        )
                )
        )
    }

    def 'test invalid array assignment'() {
        given:
        def code = 'int[] 1 = new int[0]'

        when:
        startReading(code)
        this.parser.parseAssignment()

        then:
        def e = thrown(ParserException)
        e.message == ParserException.invalidValueProvided(this.parser, '1').message
    }

    def 'test array assignment'() {
        given:
        def expected = new Assignment(
                new ArrayLiteral(Literal.of('int')),
                Literal.of('arr'),
                new StaticArray(Literal.of('int'), new NumberValueLiteral('0'))
        )
        def code = 'int[] arr = new int[0]'

        when:
        startReading(code)
        def output = this.parser.parseAssignment()

        then:
        output == expected
    }

    def 'test static array initialization'() {
        given:
        def expected = new StaticArray(
                new StaticArray(
                        new StaticArray(
                                new StaticArray(
                                        new StaticArray(Literal.of('int'), new NumberValueLiteral('2')),
                                        new NumberValueLiteral('0')),
                                new NumberValueLiteral('1')
                        ), new NumberValueLiteral('0')
                ), new NumberValueLiteral('0'))
        def code = 'new int[][][1][][2]'

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected
    }

    def 'test dynamic array initialization'() {
        given:
        def expected = new DynamicArray(new ArrayLiteral(new ArrayLiteral(new ArrayLiteral(
                Literal.of('int')
        ))), [
                new NumberValueLiteral('1'),
                new NumberValueLiteral('2'),
        ])
        def code = 'new int[][][]{1, 2}'

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected
    }

    def 'test array index'() {
        given:
        def expected = new ArrayIndex(new ArrayIndex(
                new ArrayIndex(
                        Literal.of('i'), new NumberValueLiteral('0')
                ), new NumberValueLiteral('1')
        ), new NumberValueLiteral('2'))
        def code = 'i[0][1][2]'

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected
    }

    def 'test parse new object'() {
        given:
        def expected = new NewObject(
                Literal.of('String'),
                new MethodInvocation([new StringValueLiteral('\"Hello"')])
        )
        def code = 'new String(\"Hello\")'

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected
    }

    def 'test execution of printf'() {
        given:
        def expected = new Statement(new MethodCall(
                Literal.of('System.out'),
                'printf',
                new MethodInvocation([
                        new StringValueLiteral('\"%s, %s!\"'),
                        new StringValueLiteral('\"Hello\"'),
                        new StringValueLiteral('\"world\"'),
                ])
        ))
        def code = 'System.out.printf(\"%s, %s!\", \"Hello\", \"world\");'

        when:
        startReading(code)
        def output = this.parser.parseStatement()

        then:
        output == expected
    }

    def 'test chained method call'() {
        given:
        def expected = new MethodCall(
                new MethodCall(
                        new EmptyLiteral(),
                        'method',
                        new MethodInvocation([
                                Literal.of('a'),
                                new NumberValueLiteral('1'),
                                new BooleanValueLiteral('true')
                        ])
                ),
                'toString',
                new MethodInvocation([])
        )
        def code = 'method(a, 1, true).toString()'

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected
    }

    def 'test field retrieval from method call'() {
        given:
        def expected = new Field(
                new Field(
                        new MethodCall(
                                new ThisLiteral(),
                                'toString',
                                new MethodInvocation([])
                        ), Literal.of('char_array')
                ), Literal.of('internal')
        )
        def code = 'this.toString().char_array.internal'

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected
    }

    def 'test method call'() {
        given:
        def expected = new MethodCall(
                new EmptyLiteral(),
                'method',
                new MethodInvocation([
                        Literal.of('a'),
                        new NumberValueLiteral('1'),
                        new BooleanValueLiteral('true')
                ])
        )
        def code = 'method(a, 1, true)'

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected
    }

    def 'test parse assignment: #code'() {
        when:
        startReading(code)
        def output = this.parser.parseAssignment()

        then:
        output == expected

        where:
        code         | expected
        'int i = 1;' | new Assignment(Literal.of('int'), Literal.of('i'), new NumberValueLiteral('1'))
        'int i;'     | new Assignment(Literal.of('int'), Literal.of('i'), new EmptyLiteral())
    }

    def 'test increment or decrement: #code'() {
        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected
        output.before == before

        where:
        code    | expected                                | before
        'var++' | new Increment(Literal.of('var'), false) | false
        '++var' | new Increment(Literal.of('var'), true)  | true
        'var--' | new Decrement(Literal.of('var'), false) | false
        '--var' | new Decrement(Literal.of('var'), true)  | true
    }

    def 'test parseReAssign with operation: #operation'() {
        given:
        def code = "var ${operation}= 2"
        def expected = Literal.of('var')
        def operationNode = expectedClass.newInstance(expected, new NumberValueLiteral('2'))
        expected = new ReAssign(expected, operationNode)

        when:
        startReading(code)
        def output = this.parser.parseAssignment()

        then:
        output == expected

        where:
        operation | expectedClass
        '+'       | Add
        '-'       | Subtract
        '*'       | Multiply
        '/'       | Divide
        '%'       | Modulo
        '&'       | BitAnd
        '|'       | BitOr
        '^'       | BitXor
        '<<'      | LShift
        '>>'      | RShift
        '>>>'     | URShift
    }

    def 'test parseReAssign with no operation'() {
        given:
        def code = 'var = 1'
        def expected = new ReAssign(Literal.of('var'), new NumberValueLiteral('1'))

        when:
        startReading(code)
        def output = this.parser.parseAssignment()

        then:
        output == expected
    }

    def 'test parseTernaryOperator'() {
        given:
        def code = 'true ? 1 : 2'

        when:
        startReading(code)
        def output = this.parser.parseTernaryOperator()

        then:
        output == new TernaryOperator(
                new BooleanValueLiteral('true'),
                new NumberValueLiteral('1'),
                new NumberValueLiteral('2')
        )
    }

    def 'test complex parseBinaryOperation'() {
        given:
        def code = '18 % 17 / 16 * 15 - 14 + 13 ' +
                '>>> 12 >> 11 << 10 ' +
                '^ 9 | 8 & 7 || 6 && 5 ' +
                '>= 4 > 3 <= 2 < 1 ' +
                '!= false == true'
        def expected = new NumberValueLiteral('18')
        expected = new Modulo(expected, new NumberValueLiteral('17'))
        expected = new Divide(expected, new NumberValueLiteral('16'))
        expected = new Multiply(expected, new NumberValueLiteral('15'))
        expected = new Subtract(expected, new NumberValueLiteral('14'))
        expected = new Add(expected, new NumberValueLiteral('13'))
        expected = new URShift(expected, new NumberValueLiteral('12'))
        expected = new RShift(expected, new NumberValueLiteral('11'))
        expected = new LShift(expected, new NumberValueLiteral('10'))
        expected = new BitXor(expected, new NumberValueLiteral('9'))
        expected = new BitOr(expected, new NumberValueLiteral('8'))
        expected = new BitAnd(expected, new NumberValueLiteral('7'))
        expected = new Or(expected, new NumberValueLiteral('6'))
        def second = new GreaterThanEqual(new NumberValueLiteral('5'), new NumberValueLiteral('4'))
        second = new GreaterThan(second, new NumberValueLiteral('3'))
        second = new LessThanEqual(second, new NumberValueLiteral('2'))
        second = new LessThan(second, new NumberValueLiteral('1'))
        second = new NotEqual(second, new BooleanValueLiteral('false'))
        second = new Equal(second, new BooleanValueLiteral('true'))
        expected = new And(expected, second)

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected
    }

    def 'test parenthesized operation: #operation'() {
        given:
        def expected = clazz.newInstance(
                new Add(
                        new NumberValueLiteral('1'),
                        new NumberValueLiteral('1')),
                new Subtract(
                        new NumberValueLiteral('1'),
                        new NumberValueLiteral('1')
                ))
        def code = "(1 + 1) ${operation} (1 - 1)"

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected

        where:
        operation | clazz
        '/'       | Divide
        '+'       | Add
        '-'       | Subtract
    }

    def 'test simple parseBinaryOperation (#operation)'() {
        given:
        def code = "1 ${operation} 2"
        def expected = expectedClass.newInstance(
                new NumberValueLiteral('1'),
                new NumberValueLiteral('2')
        )

        when:
        startReading(code)
        def output = this.parser.parseExpression()

        then:
        output == expected

        where:
        operation | expectedClass
        '+'       | Add
        '-'       | Subtract
        '*'       | Multiply
        '/'       | Divide
        '%'       | Modulo
        '&'       | BitAnd
        '|'       | BitOr
        '^'       | BitXor
        '<<'      | LShift
        '>>'      | RShift
        '>>>'     | URShift
    }

    def 'test parseAtom: #code'() {
        when:
        startReading(code)
        def parsed = this.parser.parseExpression()

        then:
        parsed == expected

        where:
        code      | expected
        '-1'      | new Minus(new NumberValueLiteral('1'))
        '!true'   | new Not(new BooleanValueLiteral('true'))
        '(1 + 1)' | new Add(new NumberValueLiteral('1'), new NumberValueLiteral('1'))
        'false'   | new BooleanValueLiteral('false')
        'int'     | Literal.of('int')
    }

    def 'test null cast'() {
        given:
        def expected = new Cast(
                Literal.of('Double'),
                new NullLiteral()
        )
        def code = '(Double) null'

        when:
        startReading(code)
        def parsed = this.parser.parseCast()

        then:
        parsed == expected
    }

    def 'test invalid cast'() {
        given:
        def code = '(int) -+1'

        when:
        startReading(code)
        this.parser.parseCast()

        then:
        def e = thrown(ParserException)
        e.message == ParserException.unexpectedToken(this.parser,
                TokenType.ADD, TokenType.NUMBER_VALUE).message
    }

    def 'test cast of #object'() {
        given:
        def expected = new Cast(
                Literal.of('double'),
                new Cast(
                        Literal.of('float'),
                        new Cast(Literal.of('int'), objectLiteral)
                )
        )
        def code = "(double) (float) ((int) ${object})"

        when:
        startReading(code)
        def parsed = this.parser.parseCast()

        then:
        parsed == expected

        where:
        object | objectLiteral
        '1'    | new NumberValueLiteral('1')
        '-1'   | new Minus(new NumberValueLiteral('1'))
        '-(1)' | new Minus(new NumberValueLiteral('1'))
    }

    def 'test minus'() {
        given:
        def expected = new Minus(new NumberValueLiteral('1'))

        when:
        startReading('-1')
        def parsed = this.parser.parseMinus()

        then:
        parsed == expected
    }

    def 'test not'() {
        given:
        def expected = new Not(new BooleanValueLiteral('true'))

        when:
        startReading('!true')
        def parsed = this.parser.parseNot()

        then:
        parsed == expected
    }

    def 'test parse type value of literal #literal'() {
        when:
        startReading(literal)
        def parsed = this.parser.parseTypeValue()

        then:
        parsed == expected

        where:
        literal           | expected
        '1'               | new NumberValueLiteral('1')
        '1L'              | new LongValueLiteral('1L')
        '1D'              | new DoubleValueLiteral('1D')
        '1F'              | new FloatValueLiteral('1F')
        'true'            | new BooleanValueLiteral('true')
        'false'           | new BooleanValueLiteral('false')
        '\'a\''           | new CharValueLiteral('\'a\'')
        '\"Hello world\"' | new StringValueLiteral('\"Hello world\"')
    }

    def 'test invalid literal'() {
        given:
        this.parser.input = '$$$'

        and:
        def exceptionMessage = ParserException.invalidValueProvided(this.parser, '').message

        when:
        this.parser.parseLiteral()

        then:
        def e = thrown(ParserException)
        e.message == exceptionMessage
    }

    def 'test parse type of invalid'() {
        when:
        startReading('invalid')
        this.parser.parseTypeValue()

        then:
        def e = thrown(ParserException)
        e.message == ParserException.unexpectedToken(this.parser, TokenType.LITERAL).message
    }

    def 'test parse literal'() {
        when:
        def literal = this.parser.createLiteral(BooleanValueLiteral, 'true')

        then:
        literal == new BooleanValueLiteral('true')
    }

    def 'test parse literal LiteralException'() {
        given:
        this.parser.input = 'true'
        this.parser.tokenizer.next()

        when:
        this.parser.createLiteral(BooleanValueLiteral, 'a')

        then:
        def e = thrown(ParserException)
        e.message == ParserException.invalidValueProvided(this.parser, 'a').message
    }

    def 'test parse literal RuntimeException'() {
        when:
        this.parser.createLiteral(MockLiteral, 'a')

        then:
        thrown(IllegalArgumentException)
    }

    def 'test parse EOF'() {
        when:
        this.parser.tokenizer

        then:
        def e = thrown(ParserException)
        e.message == ParserException.noInputProvided().message
    }

    static class MockLiteral extends ValueLiteral {

        MockLiteral(@NotNull String rawValue, @NotNull TokenType type) throws NodeException {
            super(rawValue, type)
            throw new IllegalArgumentException()
        }

    }

}
