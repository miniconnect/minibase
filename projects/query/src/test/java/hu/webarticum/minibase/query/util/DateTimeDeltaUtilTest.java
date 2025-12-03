package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

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
