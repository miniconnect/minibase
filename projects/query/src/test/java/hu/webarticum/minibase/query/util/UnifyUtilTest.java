package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class UnifyUtilTest {

    @Test
    void testTypeOf() {
        assertThat(UnifyUtil.typeOf(null)).isEqualTo(Void.class);
        assertThat(UnifyUtil.typeOf("lorem")).isEqualTo(String.class);
        assertThat(UnifyUtil.typeOf(12.d)).isEqualTo(Double.class);
        assertThat(UnifyUtil.typeOf(LargeInteger.of(56))).isEqualTo(LargeInteger.class);
    }

    @Test
    void testNormalizeType() {
        assertThat(UnifyUtil.normalizeType(Void.class)).isEqualTo(Void.class);
        assertThat(UnifyUtil.normalizeType(String.class)).isEqualTo(String.class);
        assertThat(UnifyUtil.normalizeType(LargeInteger.of(73).getClass())).isEqualTo(LargeInteger.class);
    }

    @Test
    void testUnifyTypes() {
        assertThat(UnifyUtil.unifyTypes()).isEqualTo(Void.class);
        assertThat(UnifyUtil.unifyTypes(String.class)).isEqualTo(String.class);
        assertThat(UnifyUtil.unifyTypes(LargeInteger.of(73).getClass())).isEqualTo(LargeInteger.class);
        assertThat(UnifyUtil.unifyTypes(Integer.class, Double.class)).isEqualTo(Double.class);
        assertThat(UnifyUtil.unifyTypes(LargeInteger.class, Boolean.class)).isEqualTo(LargeInteger.class);
        assertThat(UnifyUtil.unifyTypes(LocalDateTime.class, String.class)).isEqualTo(LocalDateTime.class);
        assertThat(UnifyUtil.unifyTypes(LocalDate.class, LocalTime.class)).isEqualTo(LocalDateTime.class);
        assertThat(UnifyUtil.unifyTypes(LocalDate.class, LocalTime.class, Instant.class)).isEqualTo(Instant.class);
    }

    @Test
    void testUnifyTypesIterable() {
        assertThat(UnifyUtil.unifyTypes(ImmutableList.empty())).isEqualTo(Void.class);
        assertThat(UnifyUtil.unifyTypes(ImmutableList.of(Integer.class, Double.class))).isEqualTo(Double.class);
    }

}
