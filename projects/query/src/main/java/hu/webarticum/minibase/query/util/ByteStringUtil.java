package hu.webarticum.minibase.query.util;

import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;

public final class ByteStringUtil {

    private ByteStringUtil() {
        // utility class
    }


    public static ByteString byteStringify(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof BitString) {
            return ByteString.wrap(((BitString) value).toByteArrayLeftAligned());
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

    public static ByteString replace(ByteString context, ByteString from, ByteString to) {
        int length = context.length();
        if (length == 0) {
            return ByteString.empty();
        }
        int fromLength = from.length();
        if (fromLength == 0) {
            return context;
        }

        ByteString.Builder resultBuilder = ByteString.builder();
        int pos = 0;
        while (true) {
            int foundIndex = context.indexOf(from, pos);
            if (foundIndex >= 0) {
                resultBuilder.append(context.substring(pos, foundIndex));
                resultBuilder.append(to);
                pos = foundIndex + fromLength;
            } else {
                resultBuilder.append(context.substring(pos));
                break;
            }
        }
        return resultBuilder.build();
    }

}
