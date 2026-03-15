package hu.webarticum.minibase.query.util;

import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.LargeInteger;

class BitStringUtilTest {

    @Test
    void testBitStringify() {
        assertThat(BitStringUtil.bitStringify(null)).isNull();
        assertThat(BitStringUtil.bitStringify(BitString.of("10011"))).isEqualTo(BitString.of("10011"));
        assertThat(BitStringUtil.bitStringify("011010")).isEqualTo(BitString.of("011010"));
        assertThat(BitStringUtil.bitStringify(LargeInteger.of(243))).isEqualTo(BitString.of("11110011"));
        assertThat(BitStringUtil.bitStringify(9)).isEqualTo(BitString.of("00000000000000000000000000001001"));
        assertThat(BitStringUtil.bitStringify(true)).isEqualTo(BitString.of("1"));
    }

    @Test
    void testBitStringifyWithSize() {
        assertThat(BitStringUtil.bitStringify(null, null)).isNull();
        assertThat(BitStringUtil.bitStringify(null, 2)).isNull();
        assertThat(BitStringUtil.bitStringify("101", null)).isEqualTo(BitString.of("101"));
        assertThat(BitStringUtil.bitStringify("110101", 4)).isEqualTo(BitString.of("1101"));
        assertThat(BitStringUtil.bitStringify(BitString.of("011010"), 3)).isEqualTo(BitString.of("011"));
        assertThat(BitStringUtil.bitStringify(BitString.of("110"), 10)).isEqualTo(BitString.of("1100000000"));
        assertThat(BitStringUtil.bitStringify(BitString.of("01"), 2)).isEqualTo(BitString.of("01"));
        assertThat(BitStringUtil.bitStringify(LargeInteger.of(243), 5)).isEqualTo(BitString.of("10011"));
        assertThat(BitStringUtil.bitStringify(LargeInteger.of(243), 14)).isEqualTo(BitString.of("00000011110011"));
        assertThat(BitStringUtil.bitStringify(9, 3)).isEqualTo(BitString.of("001"));
        assertThat(BitStringUtil.bitStringify(9, 10)).isEqualTo(BitString.of("0000001001"));
        assertThat(BitStringUtil.bitStringify(9, 32)).isEqualTo(BitString.of("00000000000000000000000000001001"));
        assertThat(BitStringUtil.bitStringify(9, 40)).isEqualTo(BitString.of("0000000000000000000000000000000000001001"));
    }

    @Test
    void testBitStringifyIllegal() {
        assertThatThrownBy(() -> BitStringUtil.bitStringify("021")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BitStringUtil.bitStringify("lorem")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BitStringUtil.bitStringify("021", 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BitStringUtil.bitStringify("lorem", 1)).isInstanceOf(IllegalArgumentException.class);
    }

}
