package hu.webarticum.minibase.query.util;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

public final class ZoneOffsetUtil {

    public static ZoneOffset zoneify(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof ZoneOffset) {
            return (ZoneOffset) value;
        } else if (value instanceof OffsetTime) {
            return ((OffsetTime) value).getOffset();
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).getOffset();
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).getOffset();
        } else if (value instanceof CharSequence) {
            return ZoneOffset.of(value.toString());
        } else if (value instanceof Temporal) {
            Temporal temporalValue = (Temporal) value;
            if (temporalValue.isSupported(ChronoField.OFFSET_SECONDS)) {
                int seconds = temporalValue.get(ChronoField.OFFSET_SECONDS);
                return ZoneOffset.ofTotalSeconds(seconds);
            } else {
                return ZoneOffset.UTC;
            }
        } else if (value instanceof TemporalAmount) {
            int seconds = (int) (DateTimeDeltaUtil.deltaify(value).toCollapsedDuration().getSeconds() % 86400L);
            return ZoneOffset.ofTotalSeconds(seconds);
        } else if (value instanceof Number) {
            int seconds = (int) NumberUtil.convertToNumber(value, Integer.class, null, null);
            return ZoneOffset.ofTotalSeconds(seconds);
        } else {
            throw new IllegalArgumentException("Cannot convert to OffsetTime");
        }
    }

}