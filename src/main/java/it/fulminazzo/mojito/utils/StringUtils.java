package it.fulminazzo.mojito.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of utilities to work with strings.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    /**
     * Splits the given string using the provided splitter regex.
     * If during the split a <b>startDelimiter</b> is encountered,
     * the string will not be split until the next <b>endDelimiter</b>
     * is met.
     *
     * @param toSplit        the string to split
     * @param splitter       the splitter regex
     * @param startDelimiter the delimiter after which to start ignoring
     * @param endDelimiter   the delimiter after which to stop ignoring
     * @return the split string
     */
    public static @NotNull String[] quoteSplitter(final @NotNull String toSplit,
                                                  final @NotNull String splitter,
                                                  final @NotNull String startDelimiter,
                                                  final @NotNull String endDelimiter) {
        List<String> result = new ArrayList<>();

        String current = "";
        int delimiters = 0;

        for (char c : toSplit.toCharArray()) {
            current += c;
            if (current.endsWith(startDelimiter)) delimiters++;
            else if (current.endsWith(endDelimiter) && delimiters > 0) delimiters--;
            else if (current.endsWith(splitter) && delimiters == 0) {
                current = current.substring(0, current.length() - splitter.length());
                result.add(current);
                current = "";
            }
        }
        result.add(current);

        return result.toArray(new String[0]);
    }

}
