package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import hu.webarticum.minibase.query.expression.TypeConstruct;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.LargeInteger;

// TODO: unify the converter framework from miniconnect:record and use it
public final class SimpleConversionUtil {

    private SimpleConversionUtil() {
        // utility class
    }

    
    public static Object convert(Object value, TypeConstruct targetTypeConstruct) {
        if (value == null) {
            return null;
        }

        Class<?> targetType = targetTypeConstruct.symbol().type();
        if (targetType == Boolean.class) {
            return BooleanUtil.boolify(value);
        } else if (targetType == String.class) {
            return convertToString(value, targetTypeConstruct);
        } else if (targetType == ByteString.class) {
            return convertToByteString(value, targetTypeConstruct);
        } else if (Number.class.isAssignableFrom(targetType)) {
            return convertToNumber(value, targetTypeConstruct);
        } else if (Temporal.class.isAssignableFrom(targetType)) {
            return convertToTemporal(value, targetTypeConstruct);
        } else if (targetType == Void.class) {
            return null;
        } else {
            throw new IllegalArgumentException("Can not convert " + value + " to " + targetTypeConstruct.symbol().name() + " :: " + targetTypeConstruct.symbol().type());
        }
    }

    private static String convertToString(Object value, TypeConstruct targetTypeConstruct) {
        String stringValue = stringify(value);
        Integer size = targetTypeConstruct.size();
        if (size == null) {
            return stringValue;
        }

        int length = stringValue.length();
        if (size >= length) {
            return stringValue;
        }

        return stringValue.substring(0, size);
    }

    private static ByteString convertToByteString(Object value, TypeConstruct targetTypeConstruct) {
        ByteString byteStringValue = byteStringify(value);
        Integer size = targetTypeConstruct.size();
        if (size == null) {
            return byteStringValue;
        }

        int length = byteStringValue.length();
        if (size >= length) {
            return byteStringValue;
        }

        return byteStringValue.substring(0, size);
    }

    private static ByteString byteStringify(Object value) {
        if (value instanceof ByteString) {
            return (ByteString) value;
        } else {
            return ByteString.of(stringify(value));
        }
    }

    private static String stringify(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else if (value instanceof LocalTime) {
            return ((LocalTime) value).format(DateTimeFormatter.ISO_LOCAL_TIME);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else if (value instanceof Instant) {
            return ((Instant) value).toString();
        } else if (value instanceof ByteString) {
            return ((ByteString) value).toString(StandardCharsets.UTF_8);
        } else {
            return value.toString();
        }
    }
    
    private static Number convertToNumber(Object value, TypeConstruct targetTypeConstruct) {
        Class<?> targetType = targetTypeConstruct.symbol().type();
        if (targetType == BigDecimal.class) {
            return convertToBigDecimal(value, targetTypeConstruct.size(), targetTypeConstruct.scale());
        } else if (targetType == Double.class) {
            return convertToDouble(value);
        } else {
            return convertToLargeInteger(value, targetTypeConstruct.size());
        }
    }

    private static BigDecimal convertToBigDecimal(Object value, Integer size, Integer scale) {
        int effectiveScale = scale != null ? scale : 0;
        BigDecimal bigDecimalValue = bigDecimalify(value).setScale(effectiveScale);

        if (size == null) {
            return bigDecimalValue;
        }

        int currentPrecision = bigDecimalValue.precision();
        if (size >= currentPrecision) {
            return bigDecimalValue;
        }

        return createBoundaryBigDecimal(size, effectiveScale, bigDecimalValue.signum() == -1);
    }

    private static BigDecimal bigDecimalify(Object value) {
        Number numericValue = NumberUtil.numberify(value);
        if (numericValue instanceof BigDecimal) {
            return (BigDecimal) numericValue;
        } else if (numericValue instanceof LargeInteger) {
            return ((LargeInteger) numericValue).bigDecimalValue();
        } else {
            return new BigDecimal(numericValue.doubleValue());
        }
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

    private static Temporal convertToTemporal(Object value, TypeConstruct targetTypeConstruct) {
        Class<?> targetType = targetTypeConstruct.symbol().type();
        if (targetType == LocalDate.class) {
            return convertToLocalDate(value, targetTypeConstruct);
        } else if (targetType == LocalTime.class) {
            return convertToLocalTime(value, targetTypeConstruct);
        } else if (targetType == LocalDateTime.class) {
            return convertToLocalDateTime(value, targetTypeConstruct);
        } else {
            return convertToInstant(value, targetTypeConstruct);
        }
    }

    private static LocalDate convertToLocalDate(Object value, TypeConstruct targetTypeConstruct) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        } else if (value instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) value, ZoneOffset.UTC).toLocalDate();
        } else if (value instanceof Number) {
            return LocalDate.ofEpochDay(((Number) value).longValue());
        } else {
            return LocalDate.parse(value.toString());
        }
    }

    private static LocalTime convertToLocalTime(Object value, TypeConstruct targetTypeConstruct) {
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalTime();
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toLocalTime();
        } else if (value instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) value, ZoneOffset.UTC).toLocalTime();
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = bigDecimalify(value);
            long nanosOfDay= bigDecimalValue.unscaledValue().longValue();
            return LocalTime.ofNanoOfDay(nanosOfDay);
        } else {
            String timeString = value.toString();
            if (timeString.indexOf('Z', 5) >= 0 || timeString.indexOf('+', 5) >= 0 || timeString.indexOf('-', 5) >= 0) {
                return OffsetTime.parse(timeString).toLocalTime();
            } else {
                return LocalTime.parse(timeString);
            }
        }
    }

    private static LocalDateTime convertToLocalDateTime(Object value, TypeConstruct targetTypeConstruct) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        } else if (value instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) value, ZoneOffset.UTC);
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = bigDecimalify(value);
            long secondsSinceEpoch = bigDecimalValue.toBigInteger().longValueExact();
            int nanoOfSecond = bigDecimalValue.remainder(BigDecimal.ONE).unscaledValue().intValue();
            Instant instantValue = Instant.ofEpochSecond(secondsSinceEpoch, nanoOfSecond);
            return instantValue.atOffset(ZoneOffset.UTC).toLocalDateTime();
        } else {
            String dateTimeString = value.toString();
            if (dateTimeString.indexOf('Z', 16) >= 0 || dateTimeString.indexOf('+', 16) >= 0 || dateTimeString.indexOf('-', 16) >= 0) {
                return OffsetDateTime.parse(dateTimeString).toLocalDateTime();
            } else {
                return LocalDateTime.parse(dateTimeString);
            }
        }
    }

    private static Instant convertToInstant(Object value, TypeConstruct targetTypeConstruct) {
        if (value instanceof Instant) {
            return (Instant) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toInstant(ZoneOffset.UTC);
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay().toInstant(ZoneOffset.UTC);
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = bigDecimalify(value);
            long secondsSinceEpoch = bigDecimalValue.toBigInteger().longValueExact();
            int nanoOfSecond = bigDecimalValue.remainder(BigDecimal.ONE).unscaledValue().intValue();
            return Instant.ofEpochSecond(secondsSinceEpoch, nanoOfSecond);
        } else {
            return Instant.parse(value.toString());
        }
    }

}
