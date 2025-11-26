package hu.webarticum.minibase.query.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import hu.webarticum.miniconnect.lang.ByteString;

public final class StringUtil {

    private StringUtil() {
        // utility class
    }

    
    public static String stringify(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else if (value instanceof LocalTime) {
            return ((LocalTime) value).format(DateTimeFormatter.ISO_LOCAL_TIME);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else if (value instanceof Instant) {
            return ((Instant) value).toString();
        } else if (value instanceof OffsetTime) {
            return ((OffsetTime) value).format(DateTimeFormatter.ISO_OFFSET_TIME);
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } else if (value instanceof ByteString) {
            return ((ByteString) value).toString(StandardCharsets.UTF_8);
        } else {
            return value.toString();
        }
    }
    
    public static String stringify(Object value, Integer size) {
        String stringValue = stringify(value);
        if (stringValue == null || size == null) {
            return stringValue;
        }

        int length = stringValue.length();
        if (size >= length) {
            return stringValue;
        }

        return stringValue.substring(0, size);
    }

}
