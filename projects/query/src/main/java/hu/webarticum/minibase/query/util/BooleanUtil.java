package hu.webarticum.minibase.query.util;

public final class BooleanUtil {

    private BooleanUtil() {
        // utility class
    }

    public static Boolean boolify(Object object) {
        if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object == null) {
            return null;
        } else {
            return !NumberUtil.isZero(NumberUtil.numberify(object));
        }
    }

}
