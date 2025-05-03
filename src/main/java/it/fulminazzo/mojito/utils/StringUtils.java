package it.fulminazzo.mojito.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Pattern pattern = Pattern.compile("(.*)" + splitter + "$");
        List<String> result = new ArrayList<>();

        String current = "";
        int delimiters = 0;

        for (char c : toSplit.toCharArray()) {
            current += c;
            if (current.matches(".*" + startDelimiter + "$")) delimiters++;
            else if (current.matches(".*" + endDelimiter + "$") && delimiters > 0) delimiters--;
            else if (delimiters == 0) {
                Matcher matcher = pattern.matcher(current);
                if (matcher.matches()) {
                    result.add(matcher.group(1));
                    current = "";
                }
            }
        }
        result.add(current);

        return result.toArray(new String[0]);
    }

}
