package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

import hu.webarticum.miniconnect.lang.ByteString;

public final class TemporalUtil {

    private TemporalUtil() {
        // utility class
    }


    public static Temporal convert(Object value, Class<?> targetType) {
        Object nonStringValue;
        if (value instanceof CharSequence) {
            nonStringValue = parse(value.toString());
        } else if (value instanceof ByteString) {
            nonStringValue = parse(((ByteString) value).toString(StandardCharsets.UTF_8));
        } else {
            nonStringValue = value;
        }
        return convertNonString(nonStringValue, targetType);
    }

    public static Temporal temporalify(Object value) {
        if (value instanceof Temporal) {
            return (Temporal) value;
        } else if (value instanceof CharSequence) {
            return parse(value.toString());
        } else if (value instanceof ByteString) {
            return parse(((ByteString) value).toString(StandardCharsets.UTF_8));
        } else if (value == null) {
            return null;
        } else if (value instanceof ZoneOffset) {
            return LocalDate.ofEpochDay(0).atStartOfDay((ZoneOffset) value);
        } else {
            return convertToInstant(value);
        }
    }

    private static Temporal parse(String dateString) {
        int firstColonPos = dateString.lastIndexOf(':');
        if (firstColonPos < 0) {
            return LocalDate.parse(dateString);
        }
        int lastDashPos = dateString.lastIndexOf('-');
        int lastPlusPos = dateString.lastIndexOf('+');
        if (lastDashPos < 0 && lastPlusPos < 0) {
            return LocalTime.parse(dateString);
        }
        if (dateString.lastIndexOf('/') >= 0) {
            return ZonedDateTime.parse(normalizeDateTimeString(dateString));
        } else if (dateString.endsWith("Z") || dateString.endsWith("z")) {
            return OffsetDateTime.parse(normalizeDateTimeString(dateString));
        }
        boolean hasOffset = (lastPlusPos >= 0 || lastDashPos > firstColonPos);
        if (!hasOffset) {
            return LocalDateTime.parse(normalizeDateTimeString(dateString));
        }
        if (lastDashPos < 0 || dateString.indexOf('-') > firstColonPos) {
            return OffsetTime.parse(dateString);
        } else {
            return OffsetDateTime.parse(normalizeDateTimeString(dateString));
        }
    }

    private static String normalizeDateTimeString(String dateTimeString) {
        int pos = dateTimeString.indexOf(' ', 5);
        if (pos < 0) {
            return dateTimeString;
        }

        return dateTimeString.substring(0, pos) + 'T' + dateTimeString.substring(pos + 1);
    }

    public static boolean isTypeUnifiable(Class<?> targetType) {
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

    private static Temporal convertNonString(Object value, Class<?> targetType) {
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
        } else if (targetType == OffsetTime.class) {
            return convertToOffsetTime(value);
        } else if (targetType == OffsetDateTime.class) {
            return convertToOffsetDateTime(value);
        } else if (targetType == ZonedDateTime.class) {
            return convertToZonedDateTime(value);
        } else {
            throw new IllegalArgumentException("Unsupported targetType: " + targetType);
        }
    }

    private static LocalTime convertToLocalTime(Object value) {
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalTime();
        } else if (value instanceof Instant) {
            return LocalDateTime.ofInstant((Instant) value, ZoneOffset.UTC).toLocalTime();
        } else if (value instanceof OffsetTime) {
            return ((OffsetTime) value).toLocalTime();
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toLocalTime();
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toLocalTime();
        } else if (value instanceof LocalDate) {
            return LocalTime.of(0, 0, 0);
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = NumberUtil.bigDecimalify(value);
            long nanosOfDay= bigDecimalValue.unscaledValue().longValue();
            return LocalTime.ofNanoOfDay(nanosOfDay);
        } else if (value instanceof TemporalAmount) {
            return convertToLocalDateTime(value).toLocalTime();
        } else if (value instanceof ZoneOffset) {
            return LocalTime.MIN;
        } else {
            throw new IllegalArgumentException("Cannot convert to LocalTime");
        }
    }

    private static OffsetTime convertToOffsetTime(Object value) {
        if (value instanceof OffsetTime) {
            return (OffsetTime) value;
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toOffsetTime();
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toOffsetDateTime().toOffsetTime();
        } else if (value instanceof LocalTime) {
            return ((LocalTime) value).atOffset(ZoneOffset.UTC);
        } else if (value instanceof ZoneOffset) {
            return LocalTime.MIN.atOffset((ZoneOffset) value);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).atOffset(ZoneOffset.UTC).toOffsetTime();
        } else if (value instanceof LocalDate) {
            return LocalTime.of(0, 0, 0).atOffset(ZoneOffset.UTC);
        } else if (value instanceof Instant) {
            return ((Instant) value).atOffset(ZoneOffset.UTC).toOffsetTime();
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = NumberUtil.bigDecimalify(value);
            long nanosOfDay= bigDecimalValue.unscaledValue().longValue();
            return LocalTime.ofNanoOfDay(nanosOfDay).atOffset(ZoneOffset.UTC);
        } else if (value instanceof TemporalAmount) {
            return convertToLocalDateTime(value).atOffset(ZoneOffset.UTC).toOffsetTime();
        } else {
            throw new IllegalArgumentException("Cannot convert to OffsetTime");
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
        } else if (value instanceof OffsetTime) {
            return LocalDate.ofEpochDay(0);
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toLocalDate();
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toLocalDate();
        } else if (value instanceof TemporalAmount) {
            return convertToLocalDateTime(value).toLocalDate();
        } else if (value instanceof ZoneOffset) {
            return LocalDate.ofEpochDay(0);
        } else {
            throw new IllegalArgumentException("Cannot convert to LocalDate");
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
        } else if (value instanceof OffsetTime) {
            return LocalDate.ofEpochDay(0).atTime(((OffsetTime) value).toLocalTime());
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toLocalDateTime();
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toLocalDateTime();
        } else if (value instanceof TemporalAmount) {
            return LocalDate.ofEpochDay(0).atStartOfDay().plus((TemporalAmount) value);
        } else if (value instanceof ZoneOffset) {
            return LocalDate.ofEpochDay(0).atStartOfDay();
        } else {
            throw new IllegalArgumentException("Cannot convert to LocalDateTime");
        }
    }

    private static OffsetDateTime convertToOffsetDateTime(Object value) {
        if (value instanceof OffsetDateTime) {
            return (OffsetDateTime) value;
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toOffsetDateTime();
        } else if (value instanceof OffsetTime) {
            return LocalDate.ofEpochDay(0).atTime(((OffsetTime) value));
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).atOffset(ZoneOffset.UTC);
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay().atOffset(ZoneOffset.UTC);
        } else if (value instanceof Instant) {
            return ((Instant) value).atOffset(ZoneOffset.UTC);
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = NumberUtil.bigDecimalify(value);
            long secondsSinceEpoch = bigDecimalValue.toBigInteger().longValueExact();
            int nanoOfSecond = bigDecimalValue.remainder(BigDecimal.ONE).movePointRight(9).intValue();
            Instant instantValue = Instant.ofEpochSecond(secondsSinceEpoch, nanoOfSecond);
            return instantValue.atOffset(ZoneOffset.UTC);
        } else if (value instanceof LocalTime) {
            return LocalDate.ofEpochDay(0).atTime((LocalTime) value).atOffset(ZoneOffset.UTC);
        } else if (value instanceof ZoneOffset) {
            return LocalDate.ofEpochDay(0).atStartOfDay().atOffset((ZoneOffset) value);
        } else if (value instanceof TemporalAmount) {
            return LocalDate.ofEpochDay(0).atStartOfDay().plus((TemporalAmount) value).atOffset(ZoneOffset.UTC);
        } else {
            throw new IllegalArgumentException("Cannot convert to OffsetDateTime");
        }
    }

    private static ZonedDateTime convertToZonedDateTime(Object value) {
        if (value instanceof ZonedDateTime) {
            return (ZonedDateTime) value;
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toZonedDateTime();
        } else if (value instanceof OffsetTime) {
            return LocalDate.ofEpochDay(0).atTime(((OffsetTime) value)).toZonedDateTime();
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).atZone(ZoneOffset.UTC);
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay().atZone(ZoneOffset.UTC);
        } else if (value instanceof Instant) {
            return ((Instant) value).atZone(ZoneOffset.UTC);
        } else if (value instanceof Number) {
            BigDecimal bigDecimalValue = NumberUtil.bigDecimalify(value);
            long secondsSinceEpoch = bigDecimalValue.toBigInteger().longValueExact();
            int nanoOfSecond = bigDecimalValue.remainder(BigDecimal.ONE).movePointRight(9).intValue();
            Instant instantValue = Instant.ofEpochSecond(secondsSinceEpoch, nanoOfSecond);
            return instantValue.atZone(ZoneOffset.UTC);
        } else if (value instanceof LocalTime) {
            return LocalDate.ofEpochDay(0).atTime((LocalTime) value).atZone(ZoneOffset.UTC);
        } else if (value instanceof TemporalAmount) {
            return LocalDate.ofEpochDay(0).atStartOfDay().plus((TemporalAmount) value).atZone(ZoneOffset.UTC);
        } else if (value instanceof ZoneId) {
            return LocalDate.ofEpochDay(0).atStartOfDay().atZone((ZoneId) value);
        } else {
            throw new IllegalArgumentException("Cannot convert to ZonedDateTime");
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
        } else if (value instanceof OffsetTime) {
            return LocalDate.ofEpochDay(0).atTime((OffsetTime) value).toInstant();
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toInstant();
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toInstant();
        } else if (value instanceof TemporalAmount) {
            return convertToLocalDateTime(value).toInstant(ZoneOffset.UTC);
        } else if (value instanceof ZoneOffset) {
            return LocalDate.ofEpochDay(0).atStartOfDay((ZoneOffset) value).toInstant();
        } else {
            throw new IllegalArgumentException("Cannot convert to Instant");
        }
    }

}
