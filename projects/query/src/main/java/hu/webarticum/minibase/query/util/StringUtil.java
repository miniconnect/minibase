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

    public static String toTitleCase(String input) {
        int length = input.length();
        StringBuilder resultBuilder = new StringBuilder();
        boolean wasLetter = false;
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            boolean isLetter = Character.isLetter(c);
            if (!isLetter) {
                resultBuilder.append(c);
                wasLetter = false;
            } else if (wasLetter) {
                resultBuilder.append(Character.toLowerCase(c));
            } else {
                resultBuilder.append(Character.toUpperCase(c));
                wasLetter = true;
            }
        }
        return resultBuilder.toString();
    }

    public static String extractSlot(String context, String delimiter, int slot) {
        if (slot < 0) {
            return "";
        }

        int length = context.length();
        int delimiterLength = delimiter.length();

        if (delimiterLength == 0) {
            return slot < length ? "" + context.charAt(slot) : "";
        }

        int currentSlot = 0;
        int pos = 0;
        while (true) {
            int foundIndex = context.indexOf(delimiter, pos);
            if (foundIndex == -1) {
                return currentSlot == slot ? context.substring(pos, length) : "";
            } else if (currentSlot == slot) {
                return context.substring(pos, foundIndex);
            }
            pos = foundIndex + delimiterLength;
            currentSlot++;
        }
    }

    public static String replace(String context, String from, String to) {
        int length = context.length();
        if (length == 0) {
            return "";
        }
        int fromLength = from.length();
        if (fromLength == 0) {
            return context;
        }

        StringBuilder resultBuilder = new StringBuilder();
        int pos = 0;
        while (true) {
            int foundIndex = context.indexOf(from, pos);
            if (foundIndex >= 0) {
                resultBuilder.append(context.substring(pos, foundIndex));
                resultBuilder.append(to);
                pos = foundIndex + fromLength;
            } else {
                resultBuilder.append(context.substring(pos));
                break;
            }
        }
        return resultBuilder.toString();
    }

}
