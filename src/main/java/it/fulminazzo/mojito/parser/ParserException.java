package it.fulminazzo.mojito.parser;

import it.fulminazzo.mojito.exceptions.FormatRuntimeException;
import it.fulminazzo.mojito.parser.node.Assignment;
import it.fulminazzo.mojito.parser.node.Node;
import it.fulminazzo.mojito.parser.node.statements.CaseStatement;
import it.fulminazzo.mojito.tokenizer.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception used to notify about errors coming from {@link JavaParser}.
 */
public final class ParserException extends FormatRuntimeException {

    /**
     * Instantiates a new Parser exception.
     *
     * @param parser  the parser that generated the exception
     * @param message the message of the exception
     * @param args    the arguments to format
     */
    private ParserException(final @Nullable Parser parser, final @NotNull String message,
                            final Object @NotNull ... args) {
        super("At line %s, column %s: %s",
                parser == null ? "0" : parser.getTokenizer().line(),
                parser == null ? "0" : parser.getTokenizer().column(),
                String.format(message, args));
    }

    /**
     * Generates a {@link ParserException} with two types of messages:
     * <ul>
     *     <li>if the token is {@link TokenType#EOF}, {@link #unexpectedEndOfInput(Parser)} is returned;</li>
     *     <li>else, <i>Unexpected token: %token%</i>.</li>
     * </ul>
     *
     * @param parser the parser
     * @param token  the token
     * @return the parser exception
     */
    public static @NotNull ParserException unexpectedToken(final @NotNull Parser parser,
                                                           final @NotNull TokenType token) {
        if (token == TokenType.EOF) return unexpectedEndOfInput(parser);
        else return new ParserException(parser, "Unexpected token: %s", token);
    }

    /**
     * Generates a {@link ParserException} with two types of messages:
     * <ul>
     *     <li>if the token is {@link TokenType#EOF}, {@link #unexpectedEndOfInput(Parser)} is returned;</li>
     *     <li>else, <i>Expected token %expected% but found %actual%</i>.</li>
     * </ul>
     *
     * @param parser   the parser
     * @param expected the expected type
     * @param actual   the actual token
     * @return the parser exception
     */
    public static @NotNull ParserException unexpectedToken(final @NotNull Parser parser,
                                                           final @NotNull TokenType expected,
                                                           final @NotNull TokenType actual) {
        if (actual == TokenType.EOF) return unexpectedEndOfInput(parser);
        else return new ParserException(parser, "Expected token %s but found %s", expected, actual);
    }

    /**
     * Generates a {@link ParserException} with message:
     * <i>Case block with expression '%expression%' already defined</i>
     *
     * @param parser        the parser
     * @param caseStatement the case block
     * @return the parser exception
     */
    public static @NotNull ParserException caseBlockAlreadyDefined(final @NotNull Parser parser,
                                                                   final @NotNull CaseStatement caseStatement) {
        return new ParserException(parser, "Case block with expression '%s' already defined", caseStatement.getExpression());
    }

    /**
     * Generates a {@link ParserException} with message:
     * <i>Default block already defined</i>
     *
     * @param parser the parser
     * @return the parser exception
     */
    public static @NotNull ParserException defaultBlockAlreadyDefined(final @NotNull Parser parser) {
        return new ParserException(parser, "Default block already defined");
    }

    /**
     * Generates a {@link ParserException} with message:
     * <i>Expecting '%expected%' but got %actual% instead.</i>
     *
     * @param parser   the parser
     * @param expected the expected
     * @param actual   the actual
     * @return the parser exception
     */
    public static @NotNull ParserException invalidNodeProvided(final @NotNull Parser parser,
                                                               final @NotNull Class<? extends Node> expected,
                                                               final @NotNull Node actual) {
        return new ParserException(parser, "Expecting '%s' but got %s instead.", expected.getSimpleName(), actual);
    }

    /**
     * Generates a {@link ParserException} with message:
     * <i>Invalid value '%value%' provided for value type '%parser#lastToken%'</i>
     *
     * @param parser the parser
     * @param value  the value
     * @return the parser exception
     */
    public static @NotNull ParserException invalidValueProvided(final @NotNull Parser parser,
                                                                final @NotNull String value) {
        return new ParserException(parser, "Invalid value '%s' provided for value type %s", value, parser.lastToken().name());
    }

    /**
     * Generates a {@link ParserException} with message:
     * <i>No input provided</i>
     *
     * @return the parser exception
     */
    public static @NotNull ParserException noInputProvided() {
        return new ParserException(null, "No input provided");
    }

    /**
     * Generates a {@link ParserException} with message:
     * <i>Unexpected end of input. Last read token: %token% (%last_read%)</i>
     *
     * @param parser the parser
     * @return the parser exception
     */
    public static @NotNull ParserException unexpectedEndOfInput(final @NotNull Parser parser) {
        return new ParserException(null, "Unexpected end of input. Last read token: %s (%s)",
                parser.lastToken(), parser.getTokenizer().lastRead());
    }

    /**
     * Generates a {@link ParserException} with message:
     * <i>Invalid try statement: no catch or finally block specified</i>
     *
     * @param parser the parser
     * @return the parser exception
     */
    public static @NotNull ParserException invalidTryStatement(final @NotNull Parser parser) {
        return new ParserException(parser, "Invalid try statement: no catch or finally block specified");
    }

    /**
     * Generates a {@link ParserException} with message:
     * <i>Final variables should be initialized, but "%assignment_name%" was not</i>
     *
     * @param parser     the parser
     * @param assignment the assignment
     * @return the parser exception
     */
    public static @NotNull ParserException finalVariableNotInitialized(final @NotNull Parser parser,
                                                                       final @NotNull Assignment assignment) {
        return new ParserException(parser, String.format("Final variables should be initialized, but \"%s\" was not",
                assignment.getName()));
    }

    /**
     * Generates a {@link ParserException} with message:
     * <i>The final keyword is only allowed for assignments, not expressions like: %expression%</i>
     *
     * @param parser     the parser
     * @param expression the expression
     * @return the parser exception
     */
    public static @NotNull ParserException finalNotAllowed(final @NotNull Parser parser,
                                                           final @NotNull Node expression) {
        return new ParserException(parser, "The final keyword is only allowed for assignments, not expressions like: "
                + expression);
    }

}
