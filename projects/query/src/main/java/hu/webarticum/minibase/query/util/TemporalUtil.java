package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;

public final class TemporalUtil {

    private TemporalUtil() {
        // utility class
    }


    public static boolean isTargetTypeSupported(Class<?> targetType) {
        return (
            targetType == LocalDate.class ||
            targetType == LocalTime.class ||
            targetType == LocalDateTime.class ||
            targetType == Instant.class);
    }
    
    public static Class<?> unifyTemporalTypes(Class<?> type1, Class<?> type2) {
        if (type1 == LocalTime.class) {
            if (type2 == LocalTime.class) {
                return LocalTime.class;
            } else if (type2 == LocalDate.class || type2 == LocalDateTime.class) {
                return LocalDateTime.class;
            } else {
                return Instant.class;
            }
        } else if (type1 == LocalDate.class) {
            if (type2 == LocalDate.class) {
                return LocalDate.class;
            } else if (type2 == LocalTime.class || type2 == LocalDateTime.class) {
                return LocalDateTime.class;
            } else {
                return Instant.class;
            }
        } else if (type1 == LocalDateTime.class) {
            if (type2 == LocalTime.class || type2 == LocalDate.class || type2 == LocalDateTime.class) {
                return LocalDateTime.class;
            } else {
                return Instant.class;
            }
        } else {
            return Instant.class;
        }
    }
    
    /**
     * Converts the given value, considering the hintTargetType, to one of these:
     * 
     * <ul>
     * <li><code>null</code> (if value was null)</li>
     * <li>a <code>LocalTime</code> instance</li>
     * <li>a <code>LocalDate</code> instance</li>
     * <li>a <code>LocalDateTime</code> instance</li>
     * <li>a <code>Instant</code> instance</li>
     * </ul>
     * 
     * @param value the input value to be converted
     * @param targetType expected type of the returning value
     * @throws IllegalArgumentException if targetType is not supported
     * @return a Temporal instance or null
    */
    public static Temporal convert(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        } else if (targetType == LocalTime.class) {
            return convertToLocalTime(value);
        } else if (targetType == LocalDate.class) {
            return convertToLocalDate(value);
        } else if (targetType == LocalDateTime.class) {
            return convertToLocalDateTime(value);
        } else if (targetType == Instant.class) {
            return convertToInstant(value);
        } else {
            throw new IllegalArgumentException("Unsupported targetType: " + targetType);
        }
    }

    private static LocalTime convertToLocalTime(Object value) {
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalTime();
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toLocalTime();
        } else if (value instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) value, ZoneOffset.UTC).toLocalTime();
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = NumberUtil.bigDecimalify(value);
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

    private static LocalDate convertToLocalDate(Object value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        } else if (value instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) value, ZoneOffset.UTC).toLocalDate();
        } else if (value instanceof Number) {
            return LocalDate.ofEpochDay(((Number) value).longValue());
        } else if (value instanceof LocalTime) {
            return LocalDate.ofEpochDay(0);
        } else {
            return LocalDate.parse(value.toString());
        }
    }

    private static LocalDateTime convertToLocalDateTime(Object value) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        } else if (value instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) value, ZoneOffset.UTC);
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = NumberUtil.bigDecimalify(value);
            long secondsSinceEpoch = bigDecimalValue.toBigInteger().longValueExact();
            int nanoOfSecond = bigDecimalValue.remainder(BigDecimal.ONE).movePointRight(9).intValue();
            Instant instantValue = Instant.ofEpochSecond(secondsSinceEpoch, nanoOfSecond);
            return instantValue.atOffset(ZoneOffset.UTC).toLocalDateTime();
        } else if (value instanceof LocalTime) {
            return LocalDate.ofEpochDay(0).atTime((LocalTime) value);
        } else {
            String dateTimeString = value.toString();
            if (dateTimeString.indexOf('Z', 16) >= 0 || dateTimeString.indexOf('+', 16) >= 0 || dateTimeString.indexOf('-', 16) >= 0) {
                return OffsetDateTime.parse(dateTimeString).toLocalDateTime();
            } else {
                return LocalDateTime.parse(dateTimeString);
            }
        }
    }

    private static Instant convertToInstant(Object value) {
        if (value instanceof Instant) {
            return (Instant) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toInstant(ZoneOffset.UTC);
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay(ZoneOffset.UTC).toInstant();
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = NumberUtil.bigDecimalify(value);
            long secondsSinceEpoch = bigDecimalValue.toBigInteger().longValueExact();
            int nanoOfSecond = bigDecimalValue.remainder(BigDecimal.ONE).movePointRight(9).intValue();
            return Instant.ofEpochSecond(secondsSinceEpoch, nanoOfSecond);
        } else if (value instanceof LocalTime) {
            return LocalDate.ofEpochDay(0).atTime((LocalTime) value).toInstant(ZoneOffset.UTC);
        } else {
            return Instant.parse(value.toString());
        }
    }

}
