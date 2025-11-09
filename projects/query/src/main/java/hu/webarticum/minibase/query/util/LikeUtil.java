package hu.webarticum.minibase.query.util;

import java.util.Objects;

public final class LikeUtil {

    private static final char ANY_CHAR_WILDCARD = '_';

    private static final char ANY_STRING_WILDCARD = '%';

    private static final String ANY_CHAR_REGEX = ".";

    private static final String ANY_STRING_REGEX = ".*";

    private static final String REGEX_SPEC_CHARS = "\\^$.|?*+()[]{}";

    private static final char REGEX_ESCAPE_CHAR = '\\';

    
    private LikeUtil() {
        // utility class
    }
    

    public static String buildRegexString(String patternString, Character escapeCharacter) {
        StringBuilder resultBuilder = new StringBuilder("^");
        int length = patternString.length();
        boolean wasEscape = false;
        for (int i = 0; i < length; i++) {
            char c = patternString.charAt(i);
            if (wasEscape) {
                appendCharLiteral(resultBuilder, c);
                wasEscape = false;
            } else if (Objects.equals(escapeCharacter, c)) {
                wasEscape = true;
            } else if (c == ANY_CHAR_WILDCARD) {
                appendAnyChar(resultBuilder);
            } else if (c == ANY_STRING_WILDCARD) {
                appendAnyString(resultBuilder);
            } else {
                appendCharLiteral(resultBuilder, c);
            }
        }
        resultBuilder.append('$');
        return resultBuilder.toString();
    }

    private static void appendAnyChar(StringBuilder resultBuilder) {
        resultBuilder.append(ANY_CHAR_REGEX);
    }

    private static void appendAnyString(StringBuilder resultBuilder) {
        resultBuilder.append(ANY_STRING_REGEX);
    }

    private static void appendCharLiteral(StringBuilder resultBuilder, char c) {
        if (REGEX_SPEC_CHARS.indexOf(c) != -1) {
            resultBuilder.append(REGEX_ESCAPE_CHAR);
        }
        resultBuilder.append(c);
    }

}
