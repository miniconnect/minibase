package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.Temporal;

import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.LargeInteger;

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
            return convertToNumber(value, targetType, size, scale);
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








    // TODO: move to NumberUtil
    
    private static Number convertToNumber(Object value, Class<?> targetType, Integer size, Integer scale) {
        if (targetType == LargeInteger.class) {
            return convertToLargeInteger(value, size);
        } else if (targetType == BigDecimal.class) {
            return convertToBigDecimal(value, size, scale);
        } else if (targetType == Double.class) {
            return convertToDouble(value);/*
        } else if (targetType == Float.class) {
            return XXXXX;
        } else if (targetType == Long.class) {
            return XXXXX;
        } else if (targetType == Integer.class) {
            return XXXXX;
        } else if (targetType == Short.class) {
            return XXXXX;
        } else if (targetType == Byte.class) {
            return XXXXX;
        } else if (targetType == BigInteger.class) {
            return XXXXX;*/
        } else {
            throw new IllegalArgumentException("Can not convert value to " + targetType);
        }
    }

    private static BigDecimal convertToBigDecimal(Object value, Integer size, Integer scale) {
        BigDecimal bigDecimalValue = NumberUtil.bigDecimalify(value);
        if (scale != null) {
            bigDecimalValue = bigDecimalValue.setScale(scale, RoundingMode.FLOOR);
        }
        int effectiveScale = bigDecimalValue.scale();

        if (size == null) {
            return bigDecimalValue;
        }

        int currentPrecision = bigDecimalValue.precision();
        if (size >= currentPrecision) {
            return bigDecimalValue;
        }

        return createBoundaryBigDecimal(size, effectiveScale, bigDecimalValue.signum() == -1);
    }

    private static BigDecimal createBoundaryBigDecimal(int size, int scale, boolean isNegative) {
        if (size == 0 && scale == 0) {
            return BigDecimal.ZERO;
        }

        String prefix = isNegative ? "-" : "";
        int wholeSize = size - scale;
        String wholePart = wholeSize > 0 ? repeat('9', wholeSize) : "0";
        int interSize = size >= scale ? 0 : scale - size;
        String interPart = repeat('0', interSize);
        int fractionSize = size >= scale ? scale : size;
        String fractionPart = repeat('9', fractionSize);
        String stringValue = prefix + wholePart + "." + interPart + fractionPart;
        return new BigDecimal(stringValue);
    }

    private static Double convertToDouble(Object value) {
        return NumberUtil.numberify(value).doubleValue();
    }

    private static LargeInteger convertToLargeInteger(Object value, Integer size) {
        LargeInteger largeIntegerValue = largeIntegerify(value);
        if (size == null) {
            return largeIntegerValue;
        }

        int currentPrecision = largeIntegerValue.abs().toString().length();
        if (size >= currentPrecision) {
            return largeIntegerValue;
        }

        return createBoundaryLargeInteger(size, largeIntegerValue.isNegative());
    }

    private static LargeInteger largeIntegerify(Object value) {
        Number numericValue = NumberUtil.numberify(value);
        if (numericValue instanceof LargeInteger) {
            return (LargeInteger) numericValue;
        } else if (numericValue instanceof BigDecimal) {
            return LargeInteger.of(((BigDecimal) numericValue).toBigInteger());
        } else {
            return LargeInteger.of(numericValue.longValue());
        }
    }

    private static LargeInteger createBoundaryLargeInteger(int size, boolean isNegative) {
        if (size == 0) {
            return LargeInteger.ZERO;
        }

        String prefix = isNegative ? "-" : "";
        return LargeInteger.of(prefix + repeat('9', size));
    }

    private static String repeat(char c, int n) {
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            resultBuilder.append(c);
        }
        return resultBuilder.toString();
    }

}
