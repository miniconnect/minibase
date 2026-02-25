package hu.webarticum.minibase.query.util;

import java.util.regex.Pattern;

import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.LargeInteger;

public final class BooleanUtil {

    public static final Pattern FALSE_PATTERN = Pattern.compile(
            "f(?:alse)?|off|no?|disabled|0|0?\\.0+|\\s*",
            Pattern.CASE_INSENSITIVE);

    private BooleanUtil() {
        // utility class
    }

    public static Boolean boolify(Object object) {
        if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object == null) {
            return null;
        } else if (object instanceof LargeInteger) {
            return ((LargeInteger) object).isNonZero();
        } else if (object instanceof CharSequence || object instanceof ByteString || object instanceof Character) {
            return !FALSE_PATTERN.matcher(object.toString()).matches();
        } else {
            return !NumberUtil.isZero(NumberUtil.numberify(object));
        }
    }

}
