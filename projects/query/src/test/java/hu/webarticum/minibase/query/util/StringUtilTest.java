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

class StringUtilTest {

    @Test
    void testStringifyNull() {
        assertThat(StringUtil.stringify(null)).isNull();
    }

    @Test
    void testStringifyString() {
        assertThat(StringUtil.stringify("")).isEqualTo("");
        assertThat(StringUtil.stringify("lorem")).isEqualTo("lorem");
    }

    @Test
    void testStringifyLocalDate() {
        assertThat(StringUtil.stringify(LocalDate.of(1926, 1, 4))).isEqualTo("1926-01-04");
        assertThat(StringUtil.stringify(LocalDate.of(2025, 2, 12))).isEqualTo("2025-02-12");
    }

    @Test
    void testStringifyLocalTime() {
        assertThat(StringUtil.stringify(LocalTime.of(13, 4))).isEqualTo("13:04:00");
        assertThat(StringUtil.stringify(LocalTime.of(20, 7, 16))).isEqualTo("20:07:16");
        assertThat(StringUtil.stringify(LocalTime.of(15, 25, 34, 106356473))).isEqualTo("15:25:34.106356473");
    }

    @Test
    void testStringifyLocalDateTime() {
        assertThat(StringUtil.stringify(LocalDateTime.of(1910, 4, 5, 0, 1))).isEqualTo("1910-04-05T00:01:00");
        assertThat(StringUtil.stringify(LocalDateTime.of(1999, 12, 31, 12, 0, 12))).isEqualTo("1999-12-31T12:00:12");
        assertThat(StringUtil.stringify(LocalDateTime.of(2025, 10, 20, 10, 35, 22, 648801627))).isEqualTo("2025-10-20T10:35:22.648801627");
    }

    @Test
    void testStringifyInstant() {
        assertThat(StringUtil.stringify(Instant.ofEpochSecond(1763141529))).isEqualTo("2025-11-14T17:32:09Z");
        assertThat(StringUtil.stringify(Instant.ofEpochSecond(1763141522, 622703525))).isEqualTo("2025-11-14T17:32:02.622703525Z");
        assertThat(StringUtil.stringify(Instant.ofEpochMilli(1763141522321L))).isEqualTo("2025-11-14T17:32:02.321Z");
    }

    @Test
    void testStringifyByteString() {
        assertThat(StringUtil.stringify(ByteString.of("lorem"))).isEqualTo("lorem");
        assertThat(StringUtil.stringify(ByteString.of(new byte[] { 116, -59, -79 }))).isEqualTo("tű");
    }

    @Test
    void testStringifyOther() {
        assertThat(StringUtil.stringify(12L)).isEqualTo("12");
        assertThat(StringUtil.stringify(LargeInteger.of("436780617018127680712"))).isEqualTo("436780617018127680712");
        assertThat(StringUtil.stringify(new Object() { @Override public String toString() { return "ipsum"; } })).isEqualTo("ipsum");
    }

    @Test
    void testStringifyWithSize() {
        assertThat(StringUtil.stringify(null, null)).isNull();
        assertThat(StringUtil.stringify(null, 3)).isNull();
        assertThat(StringUtil.stringify(123, null)).isEqualTo("123");
        assertThat(StringUtil.stringify("lorem", null)).isEqualTo("lorem");
        assertThat(StringUtil.stringify(4321, 3)).isEqualTo("432");
        assertThat(StringUtil.stringify("ipsum", 3)).isEqualTo("ips");
        assertThat(StringUtil.stringify("ipsum", 10)).isEqualTo("ipsum");
        assertThat(StringUtil.stringify(new BigDecimal("12.34567"), 4)).isEqualTo("12.3");
        assertThat(StringUtil.stringify(new BigDecimal("12.34567"), 20)).isEqualTo("12.34567");
        assertThat(StringUtil.stringify(new Object() { @Override public String toString() { return "dolor"; } }, null)).isEqualTo("dolor");
        assertThat(StringUtil.stringify(new Object() { @Override public String toString() { return "sit"; } }, 2)).isEqualTo("si");
        assertThat(StringUtil.stringify(new Object() { @Override public String toString() { return "amet"; } }, 15)).isEqualTo("amet");
    }

}
