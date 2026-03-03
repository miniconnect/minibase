package hu.webarticum.minibase.query.util;

import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ByteString;

class ByteStringUtilTest {

    @Test
    void testByteStringify() {
        assertThat(ByteStringUtil.byteStringify(null)).isNull();
        assertThat(ByteStringUtil.byteStringify(ByteString.of("lorem"))).isEqualTo(ByteString.of("lorem"));
        assertThat(ByteStringUtil.byteStringify("ipsum")).isEqualTo(ByteString.of("ipsum"));
    }

    @Test
    void testByteStringifyWithSize() {
        assertThat(ByteStringUtil.byteStringify(null, null)).isNull();
        assertThat(ByteStringUtil.byteStringify(null, 2)).isNull();
        assertThat(ByteStringUtil.byteStringify("lorem", null)).isEqualTo(ByteString.of("lorem"));
        assertThat(ByteStringUtil.byteStringify(ByteString.of("lorem"), 3)).isEqualTo(ByteString.of("lor"));
        assertThat(ByteStringUtil.byteStringify(ByteString.of("lorem"), 10)).isEqualTo(ByteString.of("lorem"));
        assertThat(ByteStringUtil.byteStringify(ByteString.of("tű"), 2)).containsExactly(0x74, 0xC5);
    }

}
