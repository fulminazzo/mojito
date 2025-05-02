package it.fulminazzo.mojito.tokenizer

import spock.lang.Specification

import static it.fulminazzo.mojito.tokenizer.TokenType.*

class TokenTypeTest extends Specification {
    private static final BETWEEN_TOKENS = [
            THIS, NULL,
            NUMBER_VALUE, LONG_VALUE, DOUBLE_VALUE,
            FLOAT_VALUE, BOOLEAN_VALUE, CHAR_VALUE,
            STRING_VALUE, LITERAL,
    ]

    def 'test #token between NOT and SPACE'() {
        expect:
        token.between(NOT, SPACE)

        where:
        token << BETWEEN_TOKENS
    }

    def 'test #token not between NOT and SPACE'() {
        expect:
        !token.between(NOT, SPACE)

        where:
        token << values().findAll { !(it in BETWEEN_TOKENS) }
    }

    def 'test fromString: #token should be #expected'() {
        when:
        def tokenType = fromString(token)

        then:
        tokenType == expected

        where:
        expected      | token
        QUESTION_MARK | '?'
        ADD           | '+'
        SUBTRACT      | '-'
        MULTIPLY      | '*'
        NUMBER_VALUE  | '1234567890'
        LONG_VALUE    | '1234567890L'
        LONG_VALUE    | '1234567890l'
        DOUBLE_VALUE  | '1234567890D'
        DOUBLE_VALUE  | '1234567890d'
        DOUBLE_VALUE  | '1234567890.1234567890D'
        DOUBLE_VALUE  | '1234567890.1234567890d'
        DOUBLE_VALUE  | '1234567890.1234567890E1234567890D'
        DOUBLE_VALUE  | '1234567890.1234567890E1234567890d'
        DOUBLE_VALUE  | '1234567890.1234567890E-1234567890D'
        DOUBLE_VALUE  | '1234567890.1234567890E-1234567890d'
        FLOAT_VALUE   | '1234567890F'
        FLOAT_VALUE   | '1234567890f'
        FLOAT_VALUE   | '1234567890.1234567890F'
        FLOAT_VALUE   | '1234567890.1234567890f'
        FLOAT_VALUE   | '1234567890.1234567890E1234567890F'
        FLOAT_VALUE   | '1234567890.1234567890E1234567890f'
        FLOAT_VALUE   | '1234567890.1234567890E-1234567890F'
        FLOAT_VALUE   | '1234567890.1234567890E-1234567890f'
        BOOLEAN_VALUE | 'true'
        BOOLEAN_VALUE | 'false'
        CHAR_VALUE    | "'c'"
        CHAR_VALUE    | "'\r'"
        CHAR_VALUE    | "'\b\'"
        CHAR_VALUE    | "'\f'"
        CHAR_VALUE    | "'\t'"
        CHAR_VALUE    | "'\n'"
        CHAR_VALUE    | "'\''"
        CHAR_VALUE    | "'\"'"
        CHAR_VALUE    | "'\\'"
        STRING_VALUE  | '\"\"'
        STRING_VALUE  | '\"Hello\"'
        STRING_VALUE  | '\"Hello \\\"friend\\\"\"'
        SPACE         | '\r'
        SPACE         | '\n'
        SPACE         | '\t'
        SPACE         | ' '
    }

}
