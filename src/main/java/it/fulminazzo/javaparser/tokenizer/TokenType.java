package it.fulminazzo.javaparser.tokenizer;

import org.jetbrains.annotations.NotNull;

/**
 * Contains all the tokens utilized by {@link it.fulminazzo.javaparser.parser.JavaParser}.
 */
public enum TokenType {

    // Operations
    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),

    AND("&&"),
    OR("||"),

    BIT_AND("&"),
    BIT_OR("|"),
    BIT_XOR("^"),
    LSHIFT("<<"),
    RSHIFT(">>"),
    URSHIFT(">>>"),

    ADD("\\+"),
    SUBTRACT("-"),
    MULTIPLY("\\*"),
    DIVIDE("/"),
    MODULO("%"),

    // Type names
    BYTE("byte"),
    CHAR("char"),
    SHORT("short"),
    INT("int"),
    LONG("long"),
    DOUBLE("double"),
    FLOAT("float"),
    BOOLEAN("boolean"),

    // Type values
    NUMBER_VALUE("[0-9]+"),
    LONG_VALUE("[0-9]+[Ll]?"),
    DOUBLE_VALUE("[0-9]+(?:.[0-9]+)?(?:E[-0-9]+)?[Dd]?"),
    FLOAT_VALUE("[0-9]+(?:.[0-9]+)?(?:E[-0-9]+)?[Ff]?"),
    BOOLEAN_VALUE("true|false"),
    CHAR_VALUE("'([^\\r\\n\\t \\\\]|\\\\[rbnft\\\\\"'])'"),
    STRING_VALUE("\"((?:[^\"]|\\\")*\")"),

    // General
    LITERAL("[a-zA-Z][a-zA-Z0-9_.]*"),
    LITERAL_NO_DOT("[a-zA-Z][a-zA-Z0-9_]*"),
    SPACE("[\r\t\n ]"),
    EOF("")
    ;

    private final @NotNull String regex;

    TokenType(final @NotNull String regex) {
        this.regex = regex;
    }

    /**
     * Verifies if the given token matches with the whole regex.
     *
     * @param token the token
     * @return true if it matches
     */
    public boolean matches(final @NotNull String token) {
        return token.matches(this.regex);
    }

    /**
     * Gets the corresponding {@link TokenType} from the given token.
     * If none is found, an {@link IllegalArgumentException} is thrown.
     *
     * @param token the token
     * @return the token type
     */
    public static @NotNull TokenType fromString(final @NotNull String token) {
        for (final TokenType tokenType : TokenType.values())
            if (tokenType.matches(token)) return tokenType;
        throw new IllegalArgumentException("Unknown token type: " + token);
    }

}