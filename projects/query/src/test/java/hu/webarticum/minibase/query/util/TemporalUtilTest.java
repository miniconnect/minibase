package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class TemporalUtilTest {

    @Test
    void testTemporalify() {
        assertThat(TemporalUtil.temporalify(null)).isNull();
        assertThat(TemporalUtil.temporalify(LocalTime.of(1, 2, 3))).isEqualTo(LocalTime.of(1, 2, 3));
        assertThat(TemporalUtil.temporalify(LocalDate.of(2026, 1, 2))).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(TemporalUtil.temporalify(LocalDateTime.of(2026, 1, 2, 4, 5, 6)))
                .isEqualTo(LocalDateTime.of(2026, 1, 2, 4, 5, 6));
        assertThat(TemporalUtil.temporalify(LocalTime.of(1, 2, 3).atOffset(ZoneOffset.UTC)))
                .isEqualTo(LocalTime.of(1, 2, 3).atOffset(ZoneOffset.UTC));
        assertThat(TemporalUtil.temporalify(LocalTime.of(1, 2, 3).atOffset(ZoneOffset.UTC)))
                .isEqualTo(LocalTime.of(1, 2, 3).atOffset(ZoneOffset.UTC));
        assertThat(TemporalUtil.temporalify(LocalDateTime.of(2026, 1, 2, 3, 4, 5).atOffset(ZoneOffset.UTC)))
                .isEqualTo(LocalDateTime.of(2026, 1, 2, 3, 4, 5).atOffset(ZoneOffset.UTC));
        assertThat(TemporalUtil.temporalify(LocalDateTime.of(2026, 1, 2, 3, 4, 5).atZone(ZoneOffset.UTC)))
                .isEqualTo(LocalDateTime.of(2026, 1, 2, 3, 4, 5).atZone(ZoneOffset.UTC));
        assertThat(TemporalUtil.temporalify(Instant.ofEpochSecond(1768176123))).isEqualTo(Instant.ofEpochSecond(1768176123));
        assertThat(TemporalUtil.temporalify(1768176123)).isEqualTo(Instant.ofEpochSecond(1768176123));
        assertThat(TemporalUtil.temporalify("01:02:03")).isEqualTo(LocalTime.of(1, 2, 3));
        assertThat(TemporalUtil.temporalify("2026-05-06")).isEqualTo(LocalDate.of(2026, 5, 6));
        assertThat(TemporalUtil.temporalify("2026-05-06T01:02:09")).isEqualTo(LocalDateTime.of(2026, 5, 6, 1, 2, 9));
        assertThat(TemporalUtil.temporalify("2026-02-06 01:02:09")).isEqualTo(LocalDateTime.of(2026, 2, 6, 1, 2, 9));
        assertThat(TemporalUtil.temporalify("01:02:07+01:00")).isEqualTo(LocalTime.of(1, 2, 7).atOffset(ZoneOffset.ofHours(1)));
        assertThat(TemporalUtil.temporalify("2026-04-01T01:00:00+02:00"))
                .isEqualTo(LocalDateTime.of(2026, 4, 1, 1, 0, 0).atOffset(ZoneOffset.ofHours(2)));
        assertThat(TemporalUtil.temporalify("2026-04-12 01:00:00+02:00"))
                .isEqualTo(LocalDateTime.of(2026, 4, 12, 1, 0, 0).atOffset(ZoneOffset.ofHours(2)));
    }

    @Test
    void testUnifyTemporalTypesSupportedOnly() {
        assertThat(TemporalUtil.unifyTemporalTypes(LocalTime.class, LocalTime.class)).isEqualTo(LocalTime.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalTime.class, LocalDate.class)).isEqualTo(LocalDateTime.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalTime.class, LocalDateTime.class)).isEqualTo(LocalDateTime.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalTime.class, Instant.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDate.class, LocalTime.class)).isEqualTo(LocalDateTime.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDate.class, LocalDate.class)).isEqualTo(LocalDate.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDate.class, LocalDateTime.class)).isEqualTo(LocalDateTime.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDate.class, Instant.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDateTime.class, LocalTime.class)).isEqualTo(LocalDateTime.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDateTime.class, LocalDate.class)).isEqualTo(LocalDateTime.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDateTime.class, LocalDateTime.class)).isEqualTo(LocalDateTime.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDateTime.class, Instant.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(Instant.class, LocalTime.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(Instant.class, LocalDate.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(Instant.class, LocalDateTime.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(Instant.class, Instant.class)).isEqualTo(Instant.class);
    }

    @Test
    void testUnifyTemporalTypesIncludingUnsupported() {
        assertThat(TemporalUtil.unifyTemporalTypes(LocalTime.class, OffsetDateTime.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(ZonedDateTime.class, LocalTime.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDate.class, ZonedDateTime.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(OffsetDateTime.class, LocalDate.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(LocalDateTime.class, OffsetDateTime.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(ZonedDateTime.class, LocalDateTime.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(Instant.class, ZonedDateTime.class)).isEqualTo(Instant.class);
        assertThat(TemporalUtil.unifyTemporalTypes(OffsetDateTime.class, Instant.class)).isEqualTo(Instant.class);
    }

    @Test
    void testConvertToLocalTime() {
        assertThat(TemporalUtil.convert(null, LocalTime.class)).isNull();
        assertThat(TemporalUtil.convert(26202000000000L, LocalTime.class)).isEqualTo(LocalTime.of(7, 16, 42));
        assertThat(TemporalUtil.convert(LargeInteger.of(35472384785122L), LocalTime.class)).isEqualTo(LocalTime.of(9, 51, 12, 384785122));
        assertThat(TemporalUtil.convert(LocalTime.of(16, 13, 11, 123456789), LocalTime.class)).isEqualTo(LocalTime.of(16, 13, 11, 123456789));
        assertThat(TemporalUtil.convert(LocalDateTime.of(2025, 12, 6, 2, 30), LocalTime.class)).isEqualTo(LocalTime.of(2, 30, 0));
        assertThat(TemporalUtil.convert(Instant.ofEpochSecond(1763237345), LocalTime.class)).isEqualTo(LocalTime.of(20, 9, 5));
    }

    @Test
    void testConvertToLocalDate() {
        assertThat(TemporalUtil.convert(null, LocalDate.class)).isNull();
        assertThat(TemporalUtil.convert(19437, LocalDate.class)).isEqualTo(LocalDate.of(2023, 3, 21));
        assertThat(TemporalUtil.convert(LargeInteger.of(18943L), LocalDate.class)).isEqualTo(LocalDate.of(2021, 11, 12));
        assertThat(TemporalUtil.convert(LocalDate.of(2025, 3, 4), LocalDate.class)).isEqualTo(LocalDate.of(2025, 3, 4));
        assertThat(TemporalUtil.convert(LocalDateTime.of(2024, 12, 6, 1, 24), LocalDate.class)).isEqualTo(LocalDate.of(2024, 12, 6));
        assertThat(TemporalUtil.convert(Instant.ofEpochSecond(1763238705), LocalDate.class)).isEqualTo(LocalDate.of(2025, 11, 15));
    }

    @Test
    void testConvertToLocalDateTime() {
        assertThat(TemporalUtil.convert(null, LocalDateTime.class)).isNull();
        assertThat(TemporalUtil.convert(1763240428L, LocalDateTime.class))
                .isEqualTo(LocalDateTime.of(2025, 11, 15, 21, 00, 28));
        assertThat(TemporalUtil.convert(new BigDecimal("1763137712.42354"), LocalDateTime.class))
                .isEqualTo(LocalDateTime.of(2025, 11, 14, 16, 28, 32, 423540000));
        assertThat(TemporalUtil.convert(LocalDate.of(2023, 1, 30), LocalDateTime.class))
                .isEqualTo(LocalDateTime.of(2023, 1, 30, 0, 0, 0));
        assertThat(TemporalUtil.convert(Instant.ofEpochSecond(1763241384, 174006389), LocalDateTime.class))
                .isEqualTo(LocalDateTime.of(2025, 11, 15, 21, 16, 24, 174006389));
    }

    @Test
    void testConvertToInstant() {
        assertThat(TemporalUtil.convert(null, Instant.class)).isNull();
        assertThat(TemporalUtil.convert(1763237485L, Instant.class))
                .isEqualTo(Instant.ofEpochSecond(1763237485));
        assertThat(TemporalUtil.convert(new BigDecimal("1763237365.4638172"), Instant.class))
                .isEqualTo(Instant.ofEpochSecond(1763237365, 463817200));
        assertThat(TemporalUtil.convert(LocalDate.of(2025, 11, 15), Instant.class))
                .isEqualTo(Instant.ofEpochSecond(1763164800));
        assertThat(TemporalUtil.convert(LocalDateTime.of(2025, 11, 15, 22, 50, 43, 173826574), Instant.class))
                .isEqualTo(Instant.ofEpochSecond(1763247043, 173826574));
        assertThat(TemporalUtil.convert(Instant.ofEpochSecond(1763241222, 96134385), Instant.class))
                .isEqualTo(Instant.ofEpochSecond(1763241222, 96134385));
    }

    @Test
    void testConvertWithLenientParsing() {
        assertThat(TemporalUtil.convert("12:20", OffsetTime.class)).isEqualTo(LocalTime.of(12, 20).atOffset(ZoneOffset.UTC));
        assertThat(TemporalUtil.convert("12:20+01:00", LocalTime.class)).isEqualTo(LocalTime.of(12, 20));
        assertThat(TemporalUtil.convert("2025-10-12 01:02:03", LocalDateTime.class)).isEqualTo(LocalDateTime.of(2025, 10, 12, 1, 2, 3));
        assertThat(TemporalUtil.convert("2025-10-12 01:02:03", LocalDateTime.class)).isEqualTo(LocalDateTime.of(2025, 10, 12, 1, 2, 3));
        assertThat(TemporalUtil.convert("2025-10-12T01:02:03", LocalDate.class)).isEqualTo(LocalDate.of(2025, 10, 12));
        assertThat(TemporalUtil.convert("2025-10-12 01:02:03Z", LocalDate.class)).isEqualTo(LocalDate.of(2025, 10, 12));
        assertThat(TemporalUtil.convert("2025-07-12", LocalTime.class)).isEqualTo(LocalTime.of(0, 0, 0));
        assertThat(TemporalUtil.convert("2025-10-11T12:12:13+00:00:07", LocalTime.class)).isEqualTo(LocalTime.of(12, 12, 13));
        assertThat(TemporalUtil.convert("2025-10-11T12:12:13", OffsetTime.class))
                .isEqualTo(LocalTime.of(12, 12, 13).atOffset(ZoneOffset.UTC));
        assertThat(TemporalUtil.convert("2025-10-11T12:12:13+01:00", OffsetTime.class))
                .isEqualTo(LocalTime.of(12, 12, 13).atOffset(ZoneOffset.ofHours(1)));
        assertThat(TemporalUtil.convert("2025-07-12", Instant.class))
                .isEqualTo(LocalDateTime.of(2025, 7, 12, 0, 0, 0).toInstant(ZoneOffset.UTC));
    }

}
