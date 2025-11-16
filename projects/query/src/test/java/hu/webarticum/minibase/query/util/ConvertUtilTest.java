package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.LargeInteger;

class ConvertUtilTest {

    @Test
    void testSomeConversions() {
        assertThat(ConvertUtil.convert(null, Void.class, null, null)).isNull();
        assertThat(ConvertUtil.convert("lorem", Void.class, null, null)).isNull();
        assertThat(ConvertUtil.convert(null, String.class, null, null)).isNull();
        assertThat(ConvertUtil.convert("128", LargeInteger.class, null, null)).isEqualTo(LargeInteger.of(128));
        assertThat(ConvertUtil.convert(4, Boolean.class, null, null)).isEqualTo(true);
        assertThat(ConvertUtil.convert("lorem", String.class, 2, null)).isEqualTo("lo");
        assertThat(ConvertUtil.convert("lorem", String.class, 10, null)).isEqualTo("lorem");
        assertThat(ConvertUtil.convert("lorem", ByteString.class, null, null)).isEqualTo(ByteString.of("lorem"));
        assertThat(ConvertUtil.convert("1.4", BigDecimal.class, 3, 2)).isEqualTo(new BigDecimal("1.40"));
        assertThat(ConvertUtil.convert("2.34", BigDecimal.class, null, null)).isEqualTo(new BigDecimal("2.34"));
        assertThat(ConvertUtil.convert(1024, BigDecimal.class, 2, null)).isEqualTo(new BigDecimal("99"));
        assertThat(ConvertUtil.convert("-4", BigDecimal.class, 2, 5)).isEqualTo(new BigDecimal("-0.00099"));
        assertThat(ConvertUtil.convert(1762976004, LocalDateTime.class, 2, 5)).isEqualTo(LocalDateTime.of(2025, 11, 12, 19, 33, 24));
        assertThat(ConvertUtil.convert(53916000000000L, LocalTime.class, 2, 5)).isEqualTo(LocalTime.of(14, 58, 36));
        assertThat(ConvertUtil.convert("2025-11-12", LocalDate.class, 2, 5)).isEqualTo(LocalDate.of(2025, 11, 12));
        assertThat(ConvertUtil.convert("2025-11-12T01:02:03Z", Instant.class, 2, 5)).isEqualTo(Instant.ofEpochSecond(1762909323));
    }

}
