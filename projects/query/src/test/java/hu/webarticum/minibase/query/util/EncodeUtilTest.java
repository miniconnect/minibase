package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;
import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ByteString;

class EncodeUtilTest {

    @Test
    void testEncodeHex() {
        assertThat(EncodeUtil.encodeHex(ByteString.empty())).isEmpty();
        assertThat(EncodeUtil.encodeHex(ByteString.ofByte(0))).isEqualTo("00");
        assertThat(EncodeUtil.encodeHex(ByteString.ofByte(65))).isEqualTo("41");
        assertThat(EncodeUtil.encodeHex(ByteString.ofByte(-2))).isEqualTo("FE");
        assertThat(EncodeUtil.encodeHex(ByteString.wrap(new byte[] { 34, -23, 92, 0, -1, 7 })))
                .isEqualTo("22E95C00FF07");
    }

    @Test
    void testDecodeHex() {
        assertThat(EncodeUtil.decodeHex("")).isEmpty();
        assertThat(EncodeUtil.decodeHex("2")).isEmpty();
        assertThat(EncodeUtil.decodeHex("2A")).containsExactly(42);
        assertThat(EncodeUtil.decodeHex("123")).containsExactly(18);
        assertThat(EncodeUtil.decodeHex("ab01F3")).containsExactly(171, 1, 243);
        assertThat(EncodeUtil.decodeHex("x2AA__CC")).containsExactly(0, 170, 0, 204);
    }

    @Test
    void testIsHexadecimalChar() {
        StringBuilder resultBuilder = new StringBuilder();
        for (char c = 0; c < 1000; c++) {
            if (EncodeUtil.isHexadecimalChar(c)) {
                resultBuilder.append(c);
            }
        }
        assertThat(resultBuilder.toString()).isEqualTo("0123456789ABCDEFabcdef");
    }

    @Test
    void testEncodeBase64() {
        assertThat(EncodeUtil.encodeBase64(ByteString.empty())).isEmpty();
        assertThat(EncodeUtil.encodeBase64(ByteString.ofByte(0))).isEqualTo("AA==");
        assertThat(EncodeUtil.encodeBase64(ByteString.ofByte(17))).isEqualTo("EQ==");
        assertThat(EncodeUtil.encodeBase64(ByteString.of(new byte[] {
                -91, -55, 64, 22, 91, -52, -28 }))).isEqualTo(
                "pclAFlvM5A==");
    }

    @Test
    void testDecodeBase64() {
        assertThat(EncodeUtil.decodeBase64("")).isEmpty();
        assertThat(EncodeUtil.decodeBase64("AA")).containsExactly(0);
        assertThat(EncodeUtil.decodeBase64("AA==")).containsExactly(0);
        assertThat(EncodeUtil.decodeBase64("pJy7MAI=")).containsExactly(-92, -100, -69, 48, 2);
        assertThat(EncodeUtil.decodeBase64("é:")).isEmpty();
    }

}
