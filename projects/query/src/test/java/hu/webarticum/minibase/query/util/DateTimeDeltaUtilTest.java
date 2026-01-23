package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.LargeInteger;

class DateTimeDeltaUtilTest {

    @Test
    void testDeltaify() {
        assertThat(DateTimeDeltaUtil.deltaify(null)).isNull();
        assertThat(DateTimeDeltaUtil.deltaify(false)).isEqualTo(DateTimeDelta.ZERO);
        assertThat(DateTimeDeltaUtil.deltaify(true)).isEqualTo(DateTimeDelta.of(0, 0, 0, 1, 0));
        assertThat(DateTimeDeltaUtil.deltaify(0)).isEqualTo(DateTimeDelta.ZERO);
        assertThat(DateTimeDeltaUtil.deltaify(3)).isEqualTo(DateTimeDelta.of(0, 0, 0, 3, 0));
        assertThat(DateTimeDeltaUtil.deltaify(-10)).isEqualTo(DateTimeDelta.of(0, 0, 0, -10, 0));
        assertThat(DateTimeDeltaUtil.deltaify(LargeInteger.of(100000))).isEqualTo(DateTimeDelta.of(0, 0, 1, 13600, 0));
        assertThat(DateTimeDeltaUtil.deltaify(LargeInteger.of(-100000))).isEqualTo(DateTimeDelta.of(0, 0, -1, -13600, 0));
        assertThat(DateTimeDeltaUtil.deltaify(3.5)).isEqualTo(DateTimeDelta.of(0, 0, 0, 3, 500_000_000));
        assertThat(DateTimeDeltaUtil.deltaify(-100000.75)).isEqualTo(DateTimeDelta.of(0, 0, -1, -13600, -750_000_000));
        assertThat(DateTimeDeltaUtil.deltaify(new BigDecimal("42.7103"))).isEqualTo(DateTimeDelta.of(0, 0, 0, 42, 710_300_000));
        assertThat(DateTimeDeltaUtil.deltaify(new BigDecimal("-100000.77"))).isEqualTo(DateTimeDelta.of(0, 0, -1, -13600, -770_000_000));
        assertThat(DateTimeDeltaUtil.deltaify(LocalTime.of(1, 2, 30))).isEqualTo(DateTimeDelta.of(0, 0, 0, 3750, 0));
        assertThat(DateTimeDeltaUtil.deltaify(LocalDate.ofEpochDay(1).atStartOfDay())).isEqualTo(DateTimeDelta.of(0, 0, 1, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaify(Instant.ofEpochSecond(23759))).isEqualTo(DateTimeDelta.of(0, 0, 0, 23759, 0));
        assertThat(DateTimeDeltaUtil.deltaify(ZoneOffset.of("+01:30"))).isEqualTo(DateTimeDelta.of(0, 0, 0, 5400, 0));
        assertThat(DateTimeDeltaUtil.deltaify(ZoneOffset.of("-01:02:03"))).isEqualTo(DateTimeDelta.of(0, 0, 0, -3723, 0));
    }

    @Test
    void testDeltaifyWithScale() {
        assertThat(DateTimeDeltaUtil.deltaify(null, null)).isNull();
        assertThat(DateTimeDeltaUtil.deltaify(null, 2)).isNull();
        assertThat(DateTimeDeltaUtil.deltaify(0, null)).isEqualTo(DateTimeDelta.ZERO);
        assertThat(DateTimeDeltaUtil.deltaify(-100000.75, null)).isEqualTo(DateTimeDelta.of(0, 0, -1, -13600, -750_000_000));
        assertThat(DateTimeDeltaUtil.deltaify(12, 0)).isEqualTo(DateTimeDelta.of(0, 0, 0, 12, 0));
        assertThat(DateTimeDeltaUtil.deltaify(12.34, null)).isEqualTo(DateTimeDelta.of(0, 0, 0, 12, 340_000_000));
        assertThat(DateTimeDeltaUtil.deltaify(12.34, 0)).isEqualTo(DateTimeDelta.of(0, 0, 0, 12, 0));
        assertThat(DateTimeDeltaUtil.deltaify(12.34, 1)).isEqualTo(DateTimeDelta.of(0, 0, 0, 12, 300_000_000));
        assertThat(DateTimeDeltaUtil.deltaify(7, -2)).isEqualTo(DateTimeDelta.of(0, 0, 0, 420, 0));
        assertThat(DateTimeDeltaUtil.deltaify(8.4, -2)).isEqualTo(DateTimeDelta.of(0, 0, 0, 480, 0));
        assertThat(DateTimeDeltaUtil.deltaify(2, -4)).isEqualTo(DateTimeDelta.of(0, 0, 0, 7200, 0));
        assertThat(DateTimeDeltaUtil.deltaify(12, -6)).isEqualTo(DateTimeDelta.of(0, 0, 12, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaify(9, -8)).isEqualTo(DateTimeDelta.of(0, 9, 0, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaify(35, -10)).isEqualTo(DateTimeDelta.of(35, 0, 0, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaify(7, -11)).isEqualTo(DateTimeDelta.of(70, 0, 0, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaify(11, -15)).isEqualTo(DateTimeDelta.of(1_100_000, 0, 0, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaify("45.2999", 2)).isEqualTo(DateTimeDelta.of(0, 0, 0, 45, 290_000_000));
        assertThat(DateTimeDeltaUtil.deltaify("01:23:02.76543", 3)).isEqualTo(DateTimeDelta.of(0, 0, 0, 4982, 765_000_000));
        assertThat(DateTimeDeltaUtil.deltaify("8-3 2 12:30:14", -10)).isEqualTo(DateTimeDelta.of(8, 0, 0, 0, 0));
    }

    @Test
    void testDeltaifyWithScaleSingleColonString() {
        assertThat(DateTimeDeltaUtil.deltaify("12:34", null)).isEqualTo(DateTimeDelta.of(0, 0, 0, 45240, 0));
        assertThat(DateTimeDeltaUtil.deltaify("12:34", 0)).isEqualTo(DateTimeDelta.of(0, 0, 0, 754, 0));
        assertThat(DateTimeDeltaUtil.deltaify("12:34", -2)).isEqualTo(DateTimeDelta.of(0, 0, 0, 45240, 0));
        assertThat(DateTimeDeltaUtil.deltaify("12:34.56", -2)).isEqualTo(DateTimeDelta.of(0, 0, 0, 720, 0));
        assertThat(DateTimeDeltaUtil.deltaify("12:34", -4)).isEqualTo(DateTimeDelta.of(0, 0, 0, 43200, 0));
        assertThat(DateTimeDeltaUtil.deltaify("12:34", -6)).isEqualTo(DateTimeDelta.ZERO);
    }

    @Test
    void testDeltaifyDays() {
        assertThat(DateTimeDeltaUtil.deltaifyDays(null)).isNull();
        assertThat(DateTimeDeltaUtil.deltaifyDays(false)).isEqualTo(DateTimeDelta.ZERO);
        assertThat(DateTimeDeltaUtil.deltaifyDays(true)).isEqualTo(DateTimeDelta.of(0, 0, 1, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaifyDays(0)).isEqualTo(DateTimeDelta.ZERO);
        assertThat(DateTimeDeltaUtil.deltaifyDays(5)).isEqualTo(DateTimeDelta.of(0, 0, 5, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaifyDays(73)).isEqualTo(DateTimeDelta.of(0, 0, 73, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaifyDays(-11)).isEqualTo(DateTimeDelta.of(0, 0, -11, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaifyDays(LargeInteger.of(99))).isEqualTo(DateTimeDelta.of(0, 0, 99, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaifyDays(LargeInteger.of(-99))).isEqualTo(DateTimeDelta.of(0, 0, -99, 0, 0));
        assertThat(DateTimeDeltaUtil.deltaifyDays(3.5)).isEqualTo(DateTimeDelta.of(0, 0, 3, 43200, 0));
        assertThat(DateTimeDeltaUtil.deltaifyDays(-1.5)).isEqualTo(DateTimeDelta.of(0, 0, -1, -43200, 0));
        assertThat(DateTimeDeltaUtil.deltaifyDays(new BigDecimal("7.31"))).isEqualTo(DateTimeDelta.of(0, 0, 7, 26784, 0));
        assertThat(DateTimeDeltaUtil.deltaifyDays(new BigDecimal("-41.7013"))).isEqualTo(DateTimeDelta.of(0, -1, -11, -60592, -320_000_000));
    }

}
