package hu.webarticum.minibase.query.util;

public final class ValueUtil {
    
    private ValueUtil() {
        // utility class
    }
    
    public static Boolean evalEquality(Object value1, Object value2) {
        if (value1 == null || value2 == null) {
            return null;
        } else if (value1 instanceof Number || value2 instanceof Number) {
            Number[] unifiedValues = NumberUtil.unify(value1, value2);
            return unifiedValues[0].equals(unifiedValues[1]);
        } else if (value1 instanceof Boolean || value2 instanceof Boolean) {
            boolean leftBool = BooleanUtil.boolify(value1);
            boolean rightBool = BooleanUtil.boolify(value1);
            return leftBool == rightBool;
        } else {
            return value1.equals(value2);
        }
    }

    public static Boolean evalNonEquality(Object value1, Object value2) {
        Boolean equality = evalEquality(value1, value2);
        if (equality == null) {
            return null;
        } else {
            return !equality;
        }
    }

}
