package it.fulminazzo.mojito.tokenizer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * A converter from raw {@link InputStream} to {@link TokenType}.
 */
public class Tokenizer implements Iterable<TokenType>, Iterator<TokenType> {
    private static final TokenType[] IGNORED_TYPES = new TokenType[]{
            TokenType.EOF, TokenType.NONE
    };

    private final @NotNull TokenizerInputStream input;
    private @NotNull TokenType lastToken = TokenType.EOF;
    private @NotNull String lastRead = "";
    private final @NotNull TreeMap<Integer, Integer> lines;

    /**
     * Instantiates a new Tokenizer.
     *
     * @param input the input stream
     */
    public Tokenizer(final @NotNull InputStream input) {
        this.input = new TokenizerInputStream(input);
        this.lines = new TreeMap<>();
        this.lines.put(1, 0);
    }

    /**
     * Verifies that input still has available data.
     *
     * @return true if it does
     */
    @Override
    public boolean hasNext() {
        try {
            return this.input.available() > 0 || this.lastToken != TokenType.EOF;
        } catch (IOException e) {
            throw new TokenizerException(e);
        }
    }

    /**
     * Reads until the character <code>\n</code> is met.
     * Then, it invokes {@link #nextSpaceless()}.
     *
     * @return the newly read token type.
     */
    public @NotNull TokenType readUntilNextLine() {
        return readUntil(TokenType.NEW_LINE);
    }

    /**
     * Reads until the given {@link TokenType} is met.
     * Then, it invokes {@link #nextSpaceless()}.
     *
     * @param tokenType the token type
     * @return the newly read token type.
     */
    public @NotNull TokenType readUntil(final @NotNull TokenType tokenType) {
        try {
            StringBuilder read = new StringBuilder();
            while (this.input.available() > 0 && !read.toString().matches("(.|\n)*" + tokenType.regex() + "$"))
                read.append(updateLineCount(this.input.read()));
            this.lastRead = read.toString();
            return nextSpaceless();
        } catch (IOException e) {
            throw new TokenizerException(e);
        }
    }

    /**
     * Reads from the input the next {@link TokenType}.
     * Repeats readings until a token different from {@link TokenType#SPACE} is found.
     *
     * @return the token type
     */
    public @NotNull TokenType nextSpaceless() {
        do {
            next();
        } while (this.lastToken == TokenType.SPACE);
        return this.lastToken;
    }

    /**
     * Reads from the input the next {@link TokenType} until the specified {@link TokenType} is met.
     *
     * @param tokenType the token type
     * @return the token type.
     * Might return {@link TokenType#NONE} in case the {@link TokenType} was met,
     * but no valid {@link TokenType} was found.
     */
    public @NotNull TokenType nextUntil(final @NotNull TokenType tokenType) {
        return next("(.|\n)*" + tokenType.regex() + "$");
    }

    /**
     * Reads from the input the next {@link TokenType}.
     *
     * @return the token type
     */
    @Override
    public @NotNull TokenType next() {
        return next("a^");
    }

    /**
     * Reads from the input the next {@link TokenType} or the regex is met.
     *
     * @param regex the regex
     * @return the token type
     * Might return {@link TokenType#NONE} in case the regex was met,
     * but no valid {@link TokenType} was found.
     */
    public @NotNull TokenType next(final @NotNull String regex) {
        try {
            String read = "";
            while (this.input.available() > 0) {
                read += updateLineCount(this.input.read());
                if (isTokenType(read) || regexMatches(regex, read)) return readTokenType(read, regex);
            }
            return eof();
        } catch (IOException e) {
            throw new TokenizerException(e);
        }
    }

    private @NotNull TokenType readTokenType(@NotNull String read,
                                             final @NotNull String regex) throws IOException {
        while (this.input.available() > 0) {
            int previousLine = line();
            int previousColumn = column();
            char c = updateLineCount(this.input.read());
            read += c;
            String subString = read.substring(0, read.length() - 1);
            boolean regexMatch = regexMatches(regex, read);
            if (regexMatch || !isTokenType(read)) {
                // Line necessary to properly read LITERAL, DOUBLE_VALUE and FLOAT_VALUE
                if (!regexMatch && c == '.') {
                    TokenType previous = TokenType.fromString(subString);
                    if (previous == TokenType.NUMBER_VALUE || previous == TokenType.LITERAL)
                        continue;
                }
                resetLines(previousLine);
                this.lines.put(previousLine, previousColumn);
                this.input.push(read.substring(read.length() - 1));
                return isTokenType(subString) ? updateTokenType(subString) : TokenType.NONE;
            }
        }
        return updateTokenType(read);
    }

    private boolean regexMatches(final @NotNull String regex,
                                 final @NotNull String read) {
        return Pattern.compile(regex).matcher(read).matches();
    }

    /**
     * Pushes the given data back into the internal {@link InputStream}.
     * It will then be read with the next {@link #next()} method call.
     * <br>
     * WARNING: this might cause inconsistencies with the expected input.
     * Use this method only to push back previously read data.
     *
     * @param data the data
     */
    public void pushback(final String @NotNull ... data) {
        String string = String.join("", data);
        this.input.push(string);

        String[] tmp = string.split("\n");
        int finalLine = line() - tmp.length + 1;
        int finalColumn = column() - tmp[0].length();
        resetLines(finalLine);
        this.lines.put(finalLine, finalColumn);
    }

    private char updateLineCount(int c) {
        if (c == '\n') {
            this.lines.put(line() + 1, 0);
        } else this.lines.put(line(), column() + 1);
        return (char) c;
    }

    private void resetLines(int line) {
        int lastLine = line();
        for (int i = line; i <= lastLine; i++) this.lines.remove(i);
        if (this.lines.isEmpty()) this.lines.put(1, 0);
    }

    private @NotNull TokenType updateTokenType(final @NotNull String read) {
        this.lastRead = read;
        this.lastToken = TokenType.fromString(read);
        return this.lastToken;
    }

    private @NotNull TokenType eof() {
        this.lastRead = "";
        this.lastToken = TokenType.EOF;
        return this.lastToken;
    }

    @Override
    public @NotNull Iterator<TokenType> iterator() {
        return this;
    }

    private boolean isTokenType(final @NotNull String read) {
        try {
            TokenType tokenType = TokenType.fromString(read);
            for (TokenType type : IGNORED_TYPES)
                if (type == tokenType) return false;
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets the last token type.
     *
     * @return the token type
     */
    public @NotNull TokenType lastToken() {
        return this.lastToken;
    }

    /**
     * Gets the last read string.
     *
     * @return the string
     */
    public @NotNull String lastRead() {
        return this.lastRead;
    }

    /**
     * Gets the last line read.
     * <code>-1</code> in case of {@link TokenType#EOF}.
     *
     * @return the line
     */
    public int line() {
        return this.lines.lastEntry().getKey();
    }

    /**
     * Gets the last column read
     * <code>-1</code> in case of {@link TokenType#EOF}.
     *
     * @return the column
     */
    public int column() {
        return this.lines.lastEntry().getValue();
    }

}
