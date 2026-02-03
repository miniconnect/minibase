package hu.webarticum.minibase.query.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import hu.webarticum.miniconnect.lang.ByteString;

public final class EncodeUtil {

    private EncodeUtil() {
        // utility class
    }

    public static String encodeHex(ByteString data) {
        StringBuilder resultBuilder = new StringBuilder();
        int length = data.length();
        for (int i = 0; i < length; i++) {
            byte b = data.byteAt(i);
            int byteInt = Byte.toUnsignedInt(b);
            String byteString = Integer.toString(byteInt, 16);
            if (byteString.length() == 1) {
                byteString = '0' + byteString;
            }
            resultBuilder.append(byteString);
        }
        return resultBuilder.toString().toUpperCase();
    }

    public static ByteString decodeHex(String hexString) {
        ByteString.Builder builder = ByteString.builder();
        int length = hexString.length();
        int wholeByteLength = length - (length % 2);
        for (int i = 0; i < wholeByteLength; i += 2) {
            String byteString = hexString.substring(i, i + 2);
            if (!isHexadecimalChar(byteString.charAt(0)) || !isHexadecimalChar(byteString.charAt(1))) {
                builder.append((byte) 0);
                continue;
            }
            byte b = (byte) Integer.parseInt(byteString, 16);
            builder.append(b);
        }
        return builder.build();
    }

    public static boolean isHexadecimalChar(char c) {
        if (c < 58) {
            return c >= 48;
        } else if (c < 71) {
            return c >= 65;
        } else if (c < 103) {
            return c >= 97;
        } else {
            return false;
        }
    }

    public static String encodeBase64(ByteString data) {
        return new String(Base64.getEncoder().encode(data.extract()), StandardCharsets.US_ASCII);
    }

    public static ByteString decodeBase64(String base64String) {
        try {
            return ByteString.wrap(Base64.getDecoder().decode(base64String.getBytes(StandardCharsets.US_ASCII)));
        } catch (IllegalArgumentException e) {
            return ByteString.empty();
        }
    }

}
