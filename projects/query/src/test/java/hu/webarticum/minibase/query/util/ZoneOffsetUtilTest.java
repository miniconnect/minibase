package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class ZoneOffsetUtilTest {

    @Test
    void testZoneify() {
        assertThat(ZoneOffsetUtil.zoneify(null)).isNull();
        assertThat(ZoneOffsetUtil.zoneify("+02:00")).isEqualTo(ZoneOffset.of("+02:00"));
        assertThat(ZoneOffsetUtil.zoneify("-01:00:00")).isEqualTo(ZoneOffset.of("-01:00"));
        assertThat(ZoneOffsetUtil.zoneify("+01:02:03")).isEqualTo(ZoneOffset.of("+01:02:03"));
        assertThat(ZoneOffsetUtil.zoneify(LocalDate.ofEpochDay(0))).isEqualTo(ZoneOffset.UTC);
        assertThat(ZoneOffsetUtil.zoneify(LocalDate.ofEpochDay(10000).atStartOfDay().atOffset(ZoneOffset.ofHours(1))))
                .isEqualTo(ZoneOffset.ofHours(1));
        assertThat(ZoneOffsetUtil.zoneify(LocalDate.ofEpochDay(10000).atStartOfDay(ZoneOffset.ofHours(2))))
                .isEqualTo(ZoneOffset.ofHours(2));
        assertThat(ZoneOffsetUtil.zoneify(Instant.ofEpochSecond(987654321L))).isEqualTo(ZoneOffset.UTC);
        assertThat(ZoneOffsetUtil.zoneify(Duration.ofHours(-2))).isEqualTo(ZoneOffset.ofHours(-2));
        assertThat(ZoneOffsetUtil.zoneify(1800L)).isEqualTo(ZoneOffset.of("+00:30"));
        assertThat(ZoneOffsetUtil.zoneify(LargeInteger.of(-5400))).isEqualTo(ZoneOffset.of("-01:30"));
    }

}
