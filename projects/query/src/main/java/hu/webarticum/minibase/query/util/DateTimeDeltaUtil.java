package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.LargeInteger;

public final class DateTimeDeltaUtil {

    private static final BigDecimal BIGDECIMAL_SECONDS_PER_DAY = new BigDecimal("86400");

    private static final Pattern MINUTE_SECOND_PATTERN = Pattern.compile("^( *(?:@ *)?)(\\d+:\\d+(?: +ago)? *)$");

    private DateTimeDeltaUtil() {
        // utility class
    }

    public static DateTimeDelta deltaify(Object object) {
        return deltaify(object, null);
    }

    public static DateTimeDelta deltaify(Object object, Integer scale) {
        if (object == null) {
            return null;
        } else if (isNumeric(object)) {
            return deltaifyNumeric(object, scale).normalized();
        } else if (object instanceof CharSequence) {
            return parse(object.toString(), scale);
        } else if (object instanceof ByteString) {
            return parse(((ByteString) object).toString(StandardCharsets.UTF_8), scale);
        } else {
            return applyScale(deltaifyNonNumericUnscaled(object), scale);
        }
    }

    private static DateTimeDelta deltaifyNumeric(Object numeric, Integer scale) {
        Number number = NumberUtil.numberify(numeric);
        if (scale == null || scale >= 0) {
            return deltaifyNumberSeconds(number, scale);
        } else if (scale <= -10) {
            return deltaifyNumberYears(number, scale);
        } else if (scale % 2 == 0) {
            return deltaifyNumberInnerField(number, scale);
        } else {
            throw new IllegalArgumentException("Invalid scale");
        }
    }

    private static DateTimeDelta deltaifyNumberSeconds(Number number, Integer scale) {
        if (number instanceof LargeInteger) {
            return DateTimeDelta.of(Duration.ofSeconds(((LargeInteger) number).longValueExact()));
        }
        BigDecimal bigDecimalSeconds = NumberUtil.bigDecimalify(number);
        long seconds = bigDecimalSeconds.toBigInteger().longValueExact();
        int nanos;
        if (scale == null || scale > 9) {
            nanos = bigDecimalSeconds.setScale(9, RoundingMode.HALF_UP)
                    .remainder(BigDecimal.ONE).unscaledValue().intValue();
        } else if (scale == 9) {
            nanos = bigDecimalSeconds.setScale(9, RoundingMode.FLOOR)
                    .remainder(BigDecimal.ONE).unscaledValue().intValue();
        } else if (scale == 0) {
            nanos = 0;
        } else {
            int fraction = bigDecimalSeconds.setScale(scale, RoundingMode.FLOOR)
                    .remainder(BigDecimal.ONE).unscaledValue().intValue();
            nanos = scaleUp(fraction, 9 - scale);
        }
        return DateTimeDelta.of(Duration.ofSeconds(seconds, nanos)).collapsed();
    }

    private static int scaleUp(int fraction, int trailingZeros) {
        switch (trailingZeros) {
            case 0: return fraction;
            case 1: return fraction * 10;
            case 2: return fraction * 100;
            case 3: return fraction * 1_000;
            case 4: return fraction * 10_000;
            case 5: return fraction * 100_000;
            case 6: return fraction * 1_000_000;
            case 7: return fraction * 10_000_000;
            case 8: return fraction * 100_000_000;
            case 9: return fraction * 1000_000_000;
            default:
                return LargeInteger.TEN.pow(trailingZeros).multiply(fraction).intValueExact();
        }
    }

    private static DateTimeDelta deltaifyNumberYears(Number number, int scale) {
        int years = scaleUp(NumberUtil.floorUnifiedNumber(number).intValueExact(), -scale - 10);
        return DateTimeDelta.of(Period.ofYears(years));
    }

    private static DateTimeDelta deltaifyNumberInnerField(Number number, int scale) {
        int value = NumberUtil.floorUnifiedNumber(number).intValueExact();
        switch (scale) {
            case -2: return DateTimeDelta.of(Duration.ofMinutes(value));
            case -4: return DateTimeDelta.of(Duration.ofHours(value));
            case -6: return DateTimeDelta.of(Period.ofDays(value));
            case -8: return DateTimeDelta.of(Period.ofMonths(value));
            default:
                throw new IllegalArgumentException("Unexpected scale for inner field");
        }
    }

    private static DateTimeDelta parse(String deltaString, Integer scale) {
        // handles the ambigous format '12:34' in case TO SECOND by inserting hours
        if (scale != null && scale >= 0) {
            Matcher matcher = MINUTE_SECOND_PATTERN.matcher(deltaString);
            if (matcher.matches()) {
                String deltaStringWithHoursInserted = matcher.group(1) + "00:" + matcher.group(2);
                return DateTimeDelta.parse(deltaStringWithHoursInserted);
            }
        }

        return applyScale(DateTimeDelta.parse(deltaString), scale);
    }

    private static DateTimeDelta deltaifyNonNumericUnscaled(Object object) {
        if (object instanceof DateTimeDelta) {
            return (DateTimeDelta) object;
        } else if (object instanceof Duration) {
            return DateTimeDelta.of((Duration) object);
        } else if (object instanceof Period) {
            return DateTimeDelta.of((Period) object);
        } else if (object instanceof ZoneOffset) {
            return DateTimeDelta.of(Duration.ofSeconds(((ZoneOffset) object).getTotalSeconds()));
        } else if (object instanceof Temporal) {
            return DateTimeDelta.between(LocalDate.ofEpochDay(0).atStartOfDay(ZoneOffset.UTC), (Temporal) object);
        } else if (object instanceof Boolean) {
            return DateTimeDelta.of(0, 0, 0, (boolean) object ? 1 : 0);
        } else {
            throw new IllegalArgumentException("Unable to convert to temporal delta");
        }
    }

    private static DateTimeDelta applyScale(DateTimeDelta baseDelta, Integer scale) {
        if (scale == null || scale >= 9) {
            return baseDelta;
        } else if (scale > 0) {
            Duration baseDuration = baseDelta.getDuration();
            int nanos = floor(baseDuration.getNano(), scaleUp(1, 9 - scale));
            return DateTimeDelta.of(baseDelta.getPeriod(), baseDuration.withNanos(nanos));
        } else if (scale == 0) {
            return DateTimeDelta.of(baseDelta.getPeriod(), baseDelta.getDuration().withNanos(0));
        } else if (scale == -2) {
            long baseSeconds = baseDelta.getDuration().getSeconds();
            return DateTimeDelta.of(baseDelta.getPeriod(), Duration.ofSeconds(floor(baseSeconds, 60)));
        } else if (scale == -4) {
            long baseSeconds = baseDelta.getDuration().getSeconds();
            return DateTimeDelta.of(baseDelta.getPeriod(), Duration.ofSeconds(floor(baseSeconds, 3600)));
        } else if (scale == -6) {
            return DateTimeDelta.of(baseDelta.getPeriod(), Duration.ZERO);
        } else if (scale == -8) {
            return DateTimeDelta.of(baseDelta.getPeriod().withDays(0), Duration.ZERO);
        } else if (scale == -10) {
            return DateTimeDelta.of(Period.ofYears(baseDelta.getPeriod().getYears()), Duration.ZERO);
        } else {
            int baseYears = baseDelta.getPeriod().getYears();
            int years = floor(baseYears, -scale - 10);
            return DateTimeDelta.of(Period.ofYears(years), Duration.ZERO);
        }
    }

    private static long floor(long number, long mod) {
        return number - (number % mod);
    }

    private static int floor(int number, int mod) {
        return number - (number % mod);
    }

    private static boolean isNumeric(Object object) {
        if (object instanceof Number) {
            return true;
        } else if (object instanceof CharSequence) {
            return NumberUtil.isNumericString(object.toString());
        } else {
            return false;
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
