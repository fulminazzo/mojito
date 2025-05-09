package it.fulminazzo.mojito.tokenizer;

import org.jetbrains.annotations.NotNull;

/**
 * Contains all the tokens utilized by {@link it.fulminazzo.mojito.parser.JavaParser}.
 */
public enum TokenType {

    // Miscellaneous
    COMMENT_INLINE("\\/\\/"),
    COMMENT_BLOCK_START("\\/\\*"),
    COMMENT_BLOCK_END("\\*\\/"),

    // Statement
    RETURN("return"),
    THROW("throw"),
    BREAK("break"),
    CONTINUE("continue"),
    NEW("new"),
    TRY("try"),
    CATCH("catch"),
    FINALLY("finally"),
    SWITCH("switch"),
    CASE("case"),
    DEFAULT("default"),
    FOR("for"),
    DO("do"),
    WHILE("while"),
    IF("if"),
    ELSE("else"),

    FINAL("final"),
    INSTANCEOF("instanceof"),

    // Expr
    ASSIGN("="),
    OPEN_PAR("\\("),
    CLOSE_PAR("\\)"),
    OPEN_BRACKET("\\["),
    CLOSE_BRACKET("\\]"),
    OPEN_BRACE("\\{"),
    CLOSE_BRACE("\\}"),
    COMMA(","),
    SEMICOLON(";"),
    COLON(":"),
    QUESTION_MARK("\\?"),

    // Binary Comparisons
    AND("&&"),
    OR("\\|\\|"),

    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),

    // Binary Operations
    BIT_AND("&"),
    BIT_OR("\\|"),
    BIT_XOR("\\^"),
    LSHIFT("<<"),
    RSHIFT(">>"),
    URSHIFT(">>>"),

    ADD("\\+"),
    SUBTRACT("-"),
    MULTIPLY("\\*"),
    DIVIDE("/"),
    MODULO("%"),

    // Unary Operations
    NOT("!"),

    // Type values
    NULL("null"),
    THIS("this"),
    NUMBER_VALUE("[0-9]+"),
    LONG_VALUE("([0-9]+)[Ll]?"),
    DOUBLE_VALUE("[0-9]+(?:.[0-9]+)?(?:E[-0-9]+)?[Dd]?"),
    FLOAT_VALUE("[0-9]+(?:.[0-9]+)?(?:E[-0-9]+)?[Ff]?"),
    BOOLEAN_VALUE("true|false"),
    CHAR_VALUE("'(.|[\r\n])'"),
    STRING_VALUE("\"((?:[^\"]|\\\")*)\""),

    // General
    LITERAL("[a-zA-Z_](?:[a-zA-Z0-9._]*[a-zA-Z0-9_])*"),
    SPACE("[\r\t\n ]"),
    DOT("\\."),
    NEW_LINE("\n"),
    EOF(""),
    /**
     * A token returned by {@link Tokenizer#next(String)} upon reading the regex.
     */
    NONE(""),
    ;

    private final @NotNull String regex;

    TokenType(final @NotNull String regex) {
        this.regex = regex;
    }

    /**
     * Gets the regex used by the current type.
     *
     * @return the regex
     */
    public @NotNull String regex() {
        return this.regex;
    }

    /**
     * Checks whether the current token is declared after <b>token</b> (NON-INCLUSIVE).
     *
     * @param token the token
     * @return true if it is
     */
    public boolean after(final @NotNull TokenType token) {
        return ordinal() > token.ordinal();
    }

    /**
     * Checks whether the current token is declared between <b>start</b> and <b>end</b> (NON-INCLUSIVE).
     *
     * @param start the start token
     * @param end   the end token
     * @return true if it is
     */
    public boolean between(final @NotNull TokenType start, final @NotNull TokenType end) {
        return after(start) && end.after(this);
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
