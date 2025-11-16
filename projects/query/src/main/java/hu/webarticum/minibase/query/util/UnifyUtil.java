package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.Temporal;

public final class UnifyUtil {

    private UnifyUtil() {
        // utility class
    }

    
    public static Class<?> unify(Iterable<Class<?>> types) {
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
            return type1;
        } else if (type1 == Void.class) {
            return type2;
        } else if (type2 == Void.class) {
            return type1;
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
            return mergeTemporalTypes(type1, type2);
        }

        return String.class;
    }

    private static Class<?> mergeStringTypeWith(Class<?> type) {
        if (type == Double.class || type == Float.class) {
            return Double.class;
        } else if (Number.class.isAssignableFrom(type)) {
            return BigDecimal.class;
        } else if (Boolean.class.isAssignableFrom(type)) {
            return Boolean.class;
        } else if (Temporal.class.isAssignableFrom(type)) {
            return TemporalUtil.isTargetTypeSupported(type) ? type : Instant.class;
        } else {
            return String.class;
        }
    }

    private static Class<?> mergeNumericTypeWith(Class<?> numericType, Class<?> otherType) {
        if (Temporal.class.isAssignableFrom(otherType)) {
            return TemporalUtil.isTargetTypeSupported(otherType) ? otherType : Instant.class;
        } else if (Number.class.isAssignableFrom(otherType)) {
            return NumberUtil.commonNumericTypeOf(numericType, otherType);
        } else {
            return Double.class;
        }
    }

    private static Class<?> mergeTemporalTypes(Class<?> type1, Class<?> type2) {

        // TODO
        return null;
        
    }

}
