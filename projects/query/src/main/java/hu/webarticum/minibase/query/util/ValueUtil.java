package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;

public final class ValueUtil {

    private ValueUtil() {
        // utility class
    }

    public static Boolean evalEquality(Object value1, Object value2) {
        if (value1 == null || value2 == null) {
            return null;
        }

        Class<?> type1 = value1.getClass();
        Class<?> type2 = value2.getClass();
        if (type1 == type2) {
            return evalEqualityForSameType(value1, value2);
        }

        Class<?> commonType = UnifyUtil.unifyTypes(type1, type2);
        if (commonType == null) {
            commonType = String.class;
        }

        Object convertedValue1 = ConvertUtil.convert(value1, commonType);
        Object convertedValue2 = ConvertUtil.convert(value2, commonType);
        return evalEqualityForSameType(convertedValue1, convertedValue2);
    }

    private static Boolean evalEqualityForSameType(Object value1, Object value2) {
        if (value1 instanceof BigDecimal) {
            return ((BigDecimal) value1).compareTo((BigDecimal) value2) == 0;
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
