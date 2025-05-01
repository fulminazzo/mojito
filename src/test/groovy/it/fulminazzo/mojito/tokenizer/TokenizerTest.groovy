package it.fulminazzo.mojito.tokenizer

import spock.lang.Specification

import static it.fulminazzo.mojito.tokenizer.TokenType.*

class TokenizerTest extends Specification {

    Tokenizer generateTokenizer(String input) {
        return new Tokenizer(new ByteArrayInputStream(input.bytes))
    }

    Tokenizer generateExceptionTokenizer() {
        def input = Mock(ByteArrayInputStream)
        input.available() >> {
            throw new IOException('Closed stream')
        }
        return new Tokenizer(input)
    }

    def 'test that tokenizer updates lines after pushback'() {
        given:
        def tokenizer = generateTokenizer('hello\nworld\nfriend')

        and:
        do tokenizer.next()
        while (tokenizer.lastToken() != EOF)

        when:
        tokenizer.pushback('rld\nfriend')

        then:
        tokenizer.line() == 2
        tokenizer.column() == 2
    }

    def 'test that tokenizer updates itself after pushback'() {
        given:
        def tokenizer = generateTokenizer('1 < 2')

        and:
        for (i in 0..2) tokenizer.next()

        and:
        tokenizer.pushback('1', '<', '2')

        when:
        def token = tokenizer.next()

        then:
        token == NUMBER_VALUE
        tokenizer.lastRead() == '1'
    }

    def 'test tokenizer hasNext method exception'() {
        given:
        def tokenizer = generateExceptionTokenizer()

        when:
        tokenizer.hasNext()

        then:
        thrown(TokenizerException)
    }

    def 'simulate reading of empty comment'() {
        given:
        def tokenizer = generateTokenizer('//\n10')

        when:
        def initialToken = tokenizer.nextSpaceless()
        def finalToken = tokenizer.readUntilNextLine()

        then:
        initialToken == COMMENT_INLINE
        finalToken == NUMBER_VALUE
        tokenizer.lastRead() == '10'
    }

    def 'test tokenizer read until next line'() {
        given:
        def tokenizer = generateTokenizer('This should be totally ignored\n10')

        when:
        tokenizer.readUntilNextLine()

        then:
        tokenizer.lastToken() == NUMBER_VALUE
        tokenizer.lastRead() == '10'
    }

    def 'test tokenizer read until next line method exception'() {
        given:
        def tokenizer = generateExceptionTokenizer()

        when:
        tokenizer.readUntilNextLine()

        then:
        thrown(TokenizerException)
    }

    def 'test next until'() {
        given:
        def tokenizer = generateTokenizer('hello.world')

        when:
        tokenizer.nextUntil(DOT)

        then:
        tokenizer.lastToken() == LITERAL
        tokenizer.lastRead() == 'hello'
        tokenizer.nextSpaceless() == DOT
    }

    def 'test tokenizer next spaceless'() {
        given:
        def tokenizer = generateTokenizer('         10')

        when:
        tokenizer.nextSpaceless()

        then:
        tokenizer.lastToken() == NUMBER_VALUE
        tokenizer.lastRead() == '10'
        tokenizer.nextSpaceless() == EOF
    }

    def 'test tokenizer next regex should return none'() {
        given:
        def tokenizer = generateTokenizer('$$')

        when:
        def output = tokenizer.next('\\$')

        then:
        output == NONE
    }

    def 'test tokenizer next regex of previous should return none'() {
        given:
        def tokenizer = generateTokenizer('a$$')

        when:
        tokenizer.next()
        def output = tokenizer.next('\\$')

        then:
        output == NONE
    }

    def 'test tokenizer invalid dot'() {
        given:
        def tokenizer = generateTokenizer('!.')

        when:
        tokenizer.next()
        def output = tokenizer.lastToken()

        then:
        output == NOT
    }

    def 'test tokenizer next'() {
        given:
        def tokenizer = generateTokenizer('10 20.0 \'c\' \"Hello\"')

        when:
        def output = []
        for (TokenType t : tokenizer) output.add(t)

        then:
        output == [NUMBER_VALUE, SPACE, DOUBLE_VALUE, SPACE,
                   CHAR_VALUE, SPACE, STRING_VALUE, EOF]
        tokenizer.lastToken() == EOF
        tokenizer.lastRead() == ''
    }

    def 'test tokenizer next method exception'() {
        given:
        def tokenizer = generateExceptionTokenizer()

        when:
        tokenizer.next()

        then:
        thrown(TokenizerException)
    }

    def 'test line and column methods reading code: #code'() {
        given:
        def tokenizer = generateTokenizer(code)

        when:
        tokenizer.nextSpaceless()

        then:
        tokenizer.line() == line
        tokenizer.column() == column

        where:
        line | column | code
        1    | 0      | ''
        1    | 1      | '1'
        1    | 6      | 'return'
        2    | 5      | '    \nbreak\n'
    }

}
