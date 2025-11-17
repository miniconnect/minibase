package hu.webarticum.minibase.query.util;

import java.time.temporal.Temporal;

import hu.webarticum.miniconnect.lang.ByteString;

public final class ConvertUtil {

    private ConvertUtil() {
        // utility class
    }

    
    public static Object convert(Object value, Class<?> targetType) {
        return convert(value, targetType, null, null);
    }

    public static Object convert(Object value, Class<?> targetType, Integer size, Integer scale) {
        if (value == null || targetType == Void.class) {
            return null;
        } else if (targetType == Boolean.class) {
            return BooleanUtil.boolify(value);
        } else if (targetType == String.class) {
            return StringUtil.stringify(value, size);
        } else if (Number.class.isAssignableFrom(targetType)) {
            return NumberUtil.convertToNumber(value, targetType, size, scale);
        } else if (targetType == ByteString.class) {
            return ByteStringUtil.byteStringify(value, size);
        } else if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        } else if (Temporal.class.isAssignableFrom(targetType)) {
            return TemporalUtil.convert(value, targetType);
        } else {
            throw new IllegalArgumentException("Can not convert value to " + targetType);
        }
    }

}
