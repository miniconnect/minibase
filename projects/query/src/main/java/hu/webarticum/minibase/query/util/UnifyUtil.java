package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Arrays;

import hu.webarticum.miniconnect.lang.LargeInteger;

public final class UnifyUtil {

    private UnifyUtil() {
        // utility class
    }


    public static Class<?> typeOf(Object value) {
        if (value == null) {
            return Void.class;
        } else if (value instanceof LargeInteger) {
            return LargeInteger.class;
        } else {
            return value.getClass();
        }
    }

    public static Class<?> normalizeType(Class<?> type) {
        if (LargeInteger.class.isAssignableFrom(type)) {
            return LargeInteger.class;
        } else {
            return type;
        }
    }

    public static Class<?> unifyTypes(Class<?>... types) {
        return unifyTypes(Arrays.asList(types));
    }

    public static Class<?> unifyTypes(Iterable<Class<?>> types) {
        Class<?> unifiedType = Void.class;
        for (Class<?> nextType : types) {
            if (nextType == null) {
                return null;
            }
            unifiedType = mergeTypes(unifiedType, nextType);
        }
        return unifiedType;
    }

    private static Class<?> mergeTypes(Class<?> type1, Class<?> type2) {
        if (type1 == type2) {
            return normalizeType(type1);
        } else if (type1 == Void.class) {
            return normalizeType(type2);
        } else if (type2 == Void.class) {
            return normalizeType(type1);
        }

        if (CharSequence.class.isAssignableFrom(type1)) {
            return mergeStringTypeWith(type2);
        } else if (CharSequence.class.isAssignableFrom(type2)) {
            return mergeStringTypeWith(type1);
        }

        if (Number.class.isAssignableFrom(type1)) {
            return mergeNumericTypeWith(type1, type2);
        } else if (Number.class.isAssignableFrom(type2)) {
            return mergeNumericTypeWith(type2, type1);
        }

        if (type1 == Boolean.class || type2 == Boolean.class) {
            return Boolean.class;
        }

        if (Temporal.class.isAssignableFrom(type1) && Temporal.class.isAssignableFrom(type2)) {
            return TemporalUtil.unifyTemporalTypes(type1, type2);
        }

        return String.class;
    }

    private static Class<?> mergeStringTypeWith(Class<?> type) {
        if (type == Double.class || type == Float.class) {
            return Double.class;
        } else if (Number.class.isAssignableFrom(type)) {
            return BigDecimal.class;
        } else if (type == Boolean.class || type == ZoneOffset.class) {
            return type;
        } else if (Temporal.class.isAssignableFrom(type)) {
            return TemporalUtil.isTypeUnifiable(type) ? type : Instant.class;
        } else {
            return String.class;
        }
    }

    private static Class<?> mergeNumericTypeWith(Class<?> numericType, Class<?> otherType) {
        if (Number.class.isAssignableFrom(otherType)) {
            return NumberUtil.commonNumericTypeOf(numericType, otherType);
        } else if (otherType == Boolean.class) {
            return numericType;
        } else if (Temporal.class.isAssignableFrom(otherType)) {
            return BigDecimal.class;
        } else {
            return Double.class;
        }
    }

}
