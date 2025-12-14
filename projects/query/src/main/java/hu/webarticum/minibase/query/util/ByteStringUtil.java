package hu.webarticum.minibase.query.util;

import hu.webarticum.miniconnect.lang.ByteString;

public final class ByteStringUtil {

    private ByteStringUtil() {
        // utility class
    }


    public static ByteString byteStringify(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof ByteString) {
            return (ByteString) value;
        } else {
            return ByteString.of(StringUtil.stringify(value));
        }
    }

    public static ByteString byteStringify(Object value, Integer size) {
        ByteString byteStringValue = byteStringify(value);
        if (value == null || size == null) {
            return byteStringValue;
        }

        int length = byteStringValue.length();
        if (size >= length) {
            return byteStringValue;
        }

        return byteStringValue.substring(0, size);
    }

}
