package it.fulminazzo.javaparser.tokenizer

import spock.lang.Specification

import static it.fulminazzo.javaparser.tokenizer.TokenType.*

class TokenTypeTest extends Specification {

    def 'test #token between NUMBER_VALUE and LITERAL'() {
        expect:
        token.between(NUMBER_VALUE, LITERAL)

        where:
        token << [
                NUMBER_VALUE, LONG_VALUE, DOUBLE_VALUE,
                FLOAT_VALUE, BOOLEAN_VALUE, CHAR_VALUE,
                STRING_VALUE, LITERAL
        ]
    }

    def 'test fromString: #token should be #expected'() {
        when:
        def tokenType = fromString(token)

        then:
        tokenType == expected

        where:
        expected        | token
        ADD             | '+'
        SUBTRACT        | '-'
        MULTIPLY        | '*'
        NUMBER_VALUE    | '1234567890'
        LONG_VALUE      | '1234567890L'
        LONG_VALUE      | '1234567890l'
        DOUBLE_VALUE    | '1234567890D'
        DOUBLE_VALUE    | '1234567890d'
        DOUBLE_VALUE    | '1234567890.1234567890D'
        DOUBLE_VALUE    | '1234567890.1234567890d'
        DOUBLE_VALUE    | '1234567890.1234567890E1234567890D'
        DOUBLE_VALUE    | '1234567890.1234567890E1234567890d'
        DOUBLE_VALUE    | '1234567890.1234567890E-1234567890D'
        DOUBLE_VALUE    | '1234567890.1234567890E-1234567890d'
        FLOAT_VALUE     | '1234567890F'
        FLOAT_VALUE     | '1234567890f'
        FLOAT_VALUE     | '1234567890.1234567890F'
        FLOAT_VALUE     | '1234567890.1234567890f'
        FLOAT_VALUE     | '1234567890.1234567890E1234567890F'
        FLOAT_VALUE     | '1234567890.1234567890E1234567890f'
        FLOAT_VALUE     | '1234567890.1234567890E-1234567890F'
        FLOAT_VALUE     | '1234567890.1234567890E-1234567890f'
        BOOLEAN_VALUE   | 'true'
        BOOLEAN_VALUE   | 'false'
        CHAR_VALUE      | "'c'"
        CHAR_VALUE      | "'\\r'"
        CHAR_VALUE      | "'\\b'"
        CHAR_VALUE      | "'\\f'"
        CHAR_VALUE      | "'\\t'"
        CHAR_VALUE      | "'\\n'"
        CHAR_VALUE      | "'\\''"
        CHAR_VALUE      | "'\\\"'"
        CHAR_VALUE      | "'\\\\'"
        STRING_VALUE    | '\"\"'
        STRING_VALUE    | '\"Hello\"'
        STRING_VALUE    | '\"Hello \\\"friend\\\"\"'
        SPACE           | '\r'
        SPACE           | '\n'
        SPACE           | '\t'
        SPACE           | ' '
    }

}