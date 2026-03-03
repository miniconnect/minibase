package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class ValueUtilTest {

    @Test
    void testEvalEquality() {
        assertThat(ValueUtil.evalEquality(null, null)).isNull();
        assertThat(ValueUtil.evalEquality(1, null)).isNull();
        assertThat(ValueUtil.evalEquality(null, 3)).isNull();
        assertThat(ValueUtil.evalEquality(2, 2)).isTrue();
        assertThat(ValueUtil.evalEquality(2, 4)).isFalse();
        assertThat(ValueUtil.evalEquality("2", 2)).isTrue();
        assertThat(ValueUtil.evalEquality(3, "3")).isTrue();
        assertThat(ValueUtil.evalEquality(2, "3")).isFalse();
        assertThat(ValueUtil.evalEquality(true, 1)).isTrue();
        assertThat(ValueUtil.evalEquality(1, true)).isTrue();
        assertThat(ValueUtil.evalEquality(2, true)).isFalse();
        assertThat(ValueUtil.evalEquality(true, 5)).isFalse();
        assertThat(ValueUtil.evalEquality("1.4", new BigDecimal("1.4"))).isTrue();
        assertThat(ValueUtil.evalEquality(new BigDecimal("3.22"), "3.22")).isTrue();
        assertThat(ValueUtil.evalEquality("1.52", new BigDecimal("1.17"))).isFalse();
        assertThat(ValueUtil.evalEquality(new BigDecimal("1.52"), "1.17")).isFalse();
        assertThat(ValueUtil.evalEquality(12d, LargeInteger.of("12"))).isTrue();
        assertThat(ValueUtil.evalEquality("41", LargeInteger.of("41"))).isTrue();
        assertThat(ValueUtil.evalEquality(LargeInteger.of("55"), 55d)).isTrue();
        assertThat(ValueUtil.evalEquality(LargeInteger.of("23"), "23")).isTrue();
        assertThat(ValueUtil.evalEquality(LargeInteger.of("23"), 7)).isFalse();
        assertThat(ValueUtil.evalEquality(9, LargeInteger.of("21"))).isFalse();
        assertThat(ValueUtil.evalEquality(new BigDecimal("2"), LargeInteger.of("2"))).isTrue();
        assertThat(ValueUtil.evalEquality(new BigDecimal("1.4"), new BigDecimal("1.40"))).isTrue();
        assertThat(ValueUtil.evalEquality(new BigDecimal("1.4"), new BigDecimal("1.45"))).isFalse();
    }

    @Test
    void testEvalNonEquality() {
        assertThat(ValueUtil.evalNonEquality(null, null)).isNull();
        assertThat(ValueUtil.evalNonEquality(1, null)).isNull();
        assertThat(ValueUtil.evalNonEquality(null, 3)).isNull();
        assertThat(ValueUtil.evalNonEquality(2, 2)).isFalse();
        assertThat(ValueUtil.evalNonEquality(2, 4)).isTrue();
        assertThat(ValueUtil.evalNonEquality(new BigDecimal("2"), LargeInteger.of("2"))).isFalse();
        assertThat(ValueUtil.evalNonEquality(new BigDecimal("1.4"), new BigDecimal("1.40"))).isFalse();
        assertThat(ValueUtil.evalNonEquality(new BigDecimal("1.4"), new BigDecimal("1.45"))).isTrue();
    }

}
