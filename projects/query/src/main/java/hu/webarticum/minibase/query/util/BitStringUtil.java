package hu.webarticum.minibase.query.util;

import java.nio.ByteBuffer;

import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.LargeInteger;

public final class BitStringUtil {

    private BitStringUtil() {
        // utility class
    }


    public static BitString bitStringify(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof BitString) {
            return (BitString) value;
        } else if (value instanceof ByteString) {
            return BitString.of(((ByteString) value).extract());
        } else if (value instanceof CharSequence) {
            return BitString.of(value.toString());
        } else if (value instanceof Boolean) {
            return BitString.of((boolean) value);
        } else if (value instanceof LargeInteger) {
            LargeInteger largeIntegerValue = (LargeInteger) value;
            int bitLength = largeIntegerValue.bitLength();
            if (largeIntegerValue.isNegative()) {
                bitLength++;
            }
            BitString converted = BitString.of(largeIntegerValue.toByteArray());
            int convertedSize = converted.length();
            return converted.substring(convertedSize - bitLength, convertedSize);
        } else if (value instanceof Byte) {
            return BitString.of(new byte[] { (byte) value });
        } else if (value instanceof Short) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES);
            byteBuffer.asShortBuffer().put((short) value);
            return BitString.of(byteBuffer.array());
        } else if (value instanceof Integer) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
            byteBuffer.asIntBuffer().put((int) value);
            return BitString.of(byteBuffer.array());
        } else if (value instanceof Long) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
            byteBuffer.asLongBuffer().put((long) value);
            return BitString.of(byteBuffer.array());
        } else {
            return bitStringify(NumberUtil.convertToNumber(value, LargeInteger.class, null, null));
        }
    }

    public static BitString bitStringify(Object value, Integer size) {
        BitString bitStringValue = bitStringify(value);
        if (value == null || size == null) {
            return bitStringValue;
        }
        int convertedLength = bitStringValue.length();
        if (size == convertedLength) {
            return bitStringValue;
        }

        if (value instanceof Number) {
            if (size > convertedLength) {
                return bitStringValue.padLeft(size);
            } else {
                return bitStringValue.substring(convertedLength - size, convertedLength);
            }
        } else {
            return bitStringValue.resize(size);
        }
    }

    public static BitString replace(BitString context, BitString from, BitString to) {
        int length = context.length();
        if (length == 0) {
            return BitString.empty();
        }
        int fromLength = from.length();
        if (fromLength == 0) {
            return context;
        }

        BitString.Builder resultBuilder = BitString.builder();
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
