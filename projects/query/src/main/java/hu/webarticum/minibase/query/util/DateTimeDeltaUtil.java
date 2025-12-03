package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;

import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.LargeInteger;

public final class DateTimeDeltaUtil {

    private static final BigDecimal BIGDECIMAL_SECONDS_PER_DAY = new BigDecimal("86400");

    private static final BigDecimal BIGDECIMAL_NANOS_PER_SECONDS = new BigDecimal("1000000000");
    
    private DateTimeDeltaUtil() {
        // utility class
    }
    
    public static DateTimeDelta deltaify(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof DateTimeDelta) {
            return (DateTimeDelta) object;
        } else if (object instanceof Duration) {
            return DateTimeDelta.of((Duration) object);
        } else if (object instanceof Period) {
            return DateTimeDelta.of((Period) object);
        } else if (object instanceof Number) {
            Number unifiedNumber = NumberUtil.numberify(object);
            long seconds;
            int nanos;
            if (unifiedNumber instanceof LargeInteger) {
                seconds = ((LargeInteger) unifiedNumber).longValueExact();
                nanos = 0;
            } else if (unifiedNumber instanceof BigDecimal) {
                BigDecimal bigDecimalValue = (BigDecimal) unifiedNumber;
                seconds = bigDecimalValue.toBigInteger().longValueExact();
                nanos = bigDecimalValue.remainder(BigDecimal.ONE).setScale(9).multiply(BIGDECIMAL_NANOS_PER_SECONDS).intValueExact();
            } else {
                double doubleValue = unifiedNumber.doubleValue();
                seconds = (long) doubleValue;
                nanos = (int) ((doubleValue % 1) * 1_000_000_000);
            }
            return DateTimeDelta.of(Duration.ofSeconds(seconds, nanos)).normalized();
        } else if (object instanceof Temporal) {
            return DateTimeDelta.between(LocalDateTime.MIN.atOffset(ZoneOffset.UTC), (Temporal) object);
        } else if (object instanceof CharSequence) {
            return DateTimeDelta.parse(object.toString());
        } else if (object instanceof ByteString) {
            return DateTimeDelta.parse(((ByteString) object).toString(StandardCharsets.UTF_8));
        } else {
            return deltaify(NumberUtil.numberify(object));
        }
    }

    public static DateTimeDelta deltaifyDays(Object days) {
        Number daysNumber = NumberUtil.numberify(days);
        if (daysNumber instanceof LargeInteger) {
            return DateTimeDelta.of(Period.ofDays(((LargeInteger) daysNumber).intValueExact()));
        } else if (daysNumber == null) {
            return null;
        }
        BigDecimal bigDecimalMultiplier = NumberUtil.bigDecimalify(daysNumber);
        BigDecimal bigDecimalSeconds = BIGDECIMAL_SECONDS_PER_DAY.multiply(bigDecimalMultiplier);
        long seconds = bigDecimalSeconds.toBigInteger().longValueExact();
        int nanos = bigDecimalSeconds.setScale(9, RoundingMode.HALF_UP).remainder(BigDecimal.ONE).unscaledValue().intValue();
        return DateTimeDelta.of(Duration.ofSeconds(seconds, nanos)).collapsed();
    }

}
