package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import hu.webarticum.minibase.query.expression.TypeConstruct;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.LargeInteger;

class SimpleConversionUtilTest {

    // FIXME: this is just a smoke test
    // TODO: unify the converter framework from miniconnect:record and use it
    @Test
    void testSomeConversions() {
        assertThat(SimpleConversionUtil.convert(
                null, new TypeConstruct(TypeConstruct.Symbol.NULL, null, null)))
                .isNull();
        assertThat(SimpleConversionUtil.convert(
                "lorem", new TypeConstruct(TypeConstruct.Symbol.NULL, null, null)))
                .isNull();
        assertThat(SimpleConversionUtil.convert(
                null, new TypeConstruct(TypeConstruct.Symbol.NVARCHAR, null, null)))
                .isNull();
        assertThat(SimpleConversionUtil.convert(
                "128", new TypeConstruct(TypeConstruct.Symbol.BIGINT, null, null)))
                .isEqualTo(LargeInteger.of(128));
        assertThat(SimpleConversionUtil.convert(
                4, new TypeConstruct(TypeConstruct.Symbol.BOOLEAN, null, null)))
                .isEqualTo(true);
        assertThat(SimpleConversionUtil.convert(
                "lorem", new TypeConstruct(TypeConstruct.Symbol.NVARCHAR, 2, null)))
                .isEqualTo("lo");
        assertThat(SimpleConversionUtil.convert(
                "lorem", new TypeConstruct(TypeConstruct.Symbol.NVARCHAR, 10, null)))
                .isEqualTo("lorem");
        assertThat(SimpleConversionUtil.convert(
                "lorem", new TypeConstruct(TypeConstruct.Symbol.BINARY, null, null)))
                .isEqualTo(ByteString.of("lorem"));
        assertThat(SimpleConversionUtil.convert(
                "1.4", new TypeConstruct(TypeConstruct.Symbol.DECIMAL, 3, 2)))
                .isEqualTo(new BigDecimal("1.40"));
        assertThat(SimpleConversionUtil.convert(
                1024, new TypeConstruct(TypeConstruct.Symbol.DECIMAL, 2, null)))
                .isEqualTo(new BigDecimal("99"));
        assertThat(SimpleConversionUtil.convert(
                "-4", new TypeConstruct(TypeConstruct.Symbol.DECIMAL, 2, 5)))
                .isEqualTo(new BigDecimal("-0.00099"));
        assertThat(SimpleConversionUtil.convert(
            1762976004, new TypeConstruct(TypeConstruct.Symbol.DATETIME, 2, 5)))
                .isEqualTo(LocalDateTime.of(2025, 11, 12, 19, 33, 24));
        assertThat(SimpleConversionUtil.convert(
            53916000000000L, new TypeConstruct(TypeConstruct.Symbol.TIME, 2, 5)))
                .isEqualTo(LocalTime.of(14, 58, 36));
        assertThat(SimpleConversionUtil.convert(
            "2025-11-12", new TypeConstruct(TypeConstruct.Symbol.DATE, 2, 5)))
                .isEqualTo(LocalDate.of(2025, 11, 12));
        assertThat(SimpleConversionUtil.convert(
            "2025-11-12T01:02:03Z", new TypeConstruct(TypeConstruct.Symbol.TIMESTAMP_WITH_TIME_ZONE, 2, 5)))
                .isEqualTo(Instant.ofEpochSecond(1762909323));
    }

}
