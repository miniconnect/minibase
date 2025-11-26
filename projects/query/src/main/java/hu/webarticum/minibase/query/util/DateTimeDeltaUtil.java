package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
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
        } else if (object instanceof ByteString) {
            return DateTimeDelta.parse(((ByteString) object).toString(StandardCharsets.UTF_8));
        } else {
            return DateTimeDelta.parse(object.toString());
        }
    }

}
