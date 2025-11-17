package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

/**
 * Utility class for transforming objects to numbers for using them in arithmetic operations.
 * 
 * <p>One of the main goals is to prevent arithmetic overflows.</p>
 */
public final class NumberUtil {
    
    private static final Pattern NUMBER_CLEAN_PATTERN = Pattern.compile("[ _]");
    
    private static final Pattern NUMBER_PREFIX_PATTERN = Pattern.compile(
            "^(?<sign>[+\\-]?)(?<whole>\\d+)?(?<frac>\\.\\d+)?");
    

    private NumberUtil() {
        // utility class
    }
    

    /**
     * Searches the best fitting numeric type for the given type.
     * 
     * <p>Returns with one of the following class types:</p>
     * 
     * <ul>
     * <li><code>Void.class</code></li>
     * <li><code>LargeInteger.class</code></li>
     * <li><code>BigDecimal.class</code></li>
     * <li><code>Double.class</code></li>
     * </ul>
     * 
     * <p>This method is consistent with {@link #numberify(Object)}.</p>
     * 
     * @param type Any object
     * @return the Class instance
     */
    public static Class<?> numberifyType(Class<?> type) {
        if (type == Void.class) {
            return Void.class;
        } else if (
                LargeInteger.class.isAssignableFrom(type) ||
                type == Long.class ||
                type == Integer.class ||
                type == Short.class ||
                type == Byte.class ||
                type == BigInteger.class ||
                type == Boolean.class ||
                type == Character.class ||
                type == LocalDate.class ||
                type == LocalTime.class ||
                type == OffsetTime.class ||
                type == LocalDateTime.class ||
                type == OffsetDateTime.class ||
                type == Instant.class) {
            return LargeInteger.class;
        } else if (
                type == BigDecimal.class ||
                type == String.class ||
                CharSequence.class.isAssignableFrom(type)) {
            return BigDecimal.class;
        } else if (Number.class.isAssignableFrom(type)) {
            return Double.class;
        } else {
            throw new IllegalArgumentException("Can not convert to numeric type: " + type);
        }
    }

    /**
     * Searches the best fitting common numeric type for the given types.
     * 
     * <p>Returns with one of the following class types:</p>
     * 
     * <ul>
     * <li><code>Void.class</code></li>
     * <li><code>LargeInteger.class</code></li>
     * <li><code>BigDecimal.class</code></li>
     * <li><code>Double.class</code></li>
     * </ul>
     * 
     * @param types Any types
     * @return The common class
     */
    public static Class<?> commonNumericTypeOf(Class<?>... types) {
        ImmutableList<Class<?>> numericTypes = ImmutableList.of(types).map(NumberUtil::numberifyType);
        Class<?>[] typesToCheck = new Class<?>[] { Double.class, BigDecimal.class, LargeInteger.class };
        for (Class<?> type : typesToCheck) {
            if (numericTypes.contains(type)) {
                return type;
            }
        }
        return Void.class;
    }
    
    public static Number promote(Object convertedValue, Class<?> targetType) {
        if (convertedValue == null) {
            if (targetType == Void.class) {
                return null;
            } else {
                throw new IllegalArgumentException("Can not promote null to " + targetType);
            }
        } else if (targetType == Void.class) {
            throw new IllegalArgumentException("Can not promote " + convertedValue + " to void");
        } else if (targetType == LargeInteger.class) {
            if (convertedValue instanceof LargeInteger) {
                return (LargeInteger) convertedValue;
            } else {
                throw new IllegalArgumentException("Can not promote " + convertedValue + " to " + LargeInteger.class);
            }
        } else if (targetType == BigDecimal.class) {
            if (convertedValue instanceof LargeInteger) {
                return ((LargeInteger) convertedValue).bigDecimalValue();
            } else if (convertedValue instanceof BigDecimal) {
                return (BigDecimal) convertedValue;
            } else {
                throw new IllegalArgumentException("Can not promote " + convertedValue + " to " + BigDecimal.class);
            }
        } else if (targetType != Double.class) {
            throw new IllegalArgumentException("Unsupported promotion to " + targetType);
        } else if (convertedValue instanceof LargeInteger) {
            return ((LargeInteger) convertedValue).doubleValue();
        } else if (convertedValue instanceof BigDecimal) {
            return ((BigDecimal) convertedValue).doubleValue();
        } else if (convertedValue instanceof Double) {
            return (Double) convertedValue;
        } else {
            throw new IllegalArgumentException("Unsupported value for promotion: " + convertedValue);
        }
    }

    public static Number[] unify(Object value1, Object value2) {
        Number number1 = numberify(value1);
        Number number2 = numberify(value2);
        if (number1 == null || number2 == null) {
            return new Number[] { number1, number2 };
        }

        Class<? extends Number> class1 = number1.getClass();
        Class<? extends Number> class2 = number2.getClass();
        if (class1 == class2 && class1 != BigDecimal.class) {
            return new Number[] { number1, number2 };
        }

        if (class1 == Double.class) {
            return new Number[] { number1, promote(number2, Double.class) };
        } else if (class2 == Double.class) {
            return new Number[] { promote(number1, Double.class), number2 };
        } else if (class1 == BigDecimal.class) {
            return unifyBigDecimals(number1, promote(number2, BigDecimal.class));
        } else if (class2 == BigDecimal.class) {
            return unifyBigDecimals(promote(number1, BigDecimal.class), number2);
        } else {
            return new Number[] { promote(number1, Double.class), promote(number2, Double.class) };
        }
    }

    private static Number[] unifyBigDecimals(Number value1, Number value2) {
        BigDecimal bigDecimal1 = (BigDecimal) value1;
        BigDecimal bigDecimal2 = (BigDecimal) value2;
        int scale1 = bigDecimal1.scale();
        int scale2 = bigDecimal2.scale();
        if (scale1 == scale2) {
            return new Number[] { value1, value2 };
        } else if (scale1 > scale2) {
            return new Number[] { value1, bigDecimal2.setScale(scale1) };
        } else {
            return new Number[] { bigDecimal1.setScale(scale2), value2 };
        }
    }
    
    /**
     * Converts an object to one of the following types:
     * 
     * <ul>
     * <li><code>null</code></li>
     * <li><code>LargeInteger</code></li>
     * <li><code>BigDecimal</code></li>
     * <li><code>Double</code></li>
     * </ul>
     * 
     * <p>This method is consistent with {@link #numberifyType(Class)}.</p>
     * 
     * @param object Any object
     * @return the Number instance
     */
    public static Number numberify(Object object) { // NOSONAR method complexity is necessary
        if (object == null) {
            return null;
        } else if (object instanceof LargeInteger) {
            return (LargeInteger) object;
        } else if (object instanceof BigDecimal) {
            return (BigDecimal) object;
        } else if (
                object instanceof Long ||
                object instanceof Integer ||
                object instanceof Short ||
                object instanceof Byte) {
            return LargeInteger.of(((Number) object).longValue());
        } else if (object instanceof BigInteger) {
            return LargeInteger.of((BigInteger) object);
        } else if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else if (object instanceof Boolean) {
            return LargeInteger.of((boolean) object ? 1L : 0L);
        } else if (object instanceof Character) {
            return LargeInteger.of((long) (char) object);
        } else if (object instanceof LocalDate) {
            return LargeInteger.of(((LocalDate) object).toEpochDay());
        } else if (object instanceof LocalTime) {
            return LargeInteger.of(((LocalTime) object).getSecond());
        } else if (object instanceof OffsetTime) {
            return numberify(((OffsetTime) object).toLocalTime());
        } else if (object instanceof LocalDateTime) {
            return LargeInteger.of(((LocalDateTime) object).toEpochSecond(ZoneOffset.UTC));
        } else if (object instanceof OffsetDateTime) {
            return numberify(((OffsetDateTime) object).toInstant());
        } else if (object instanceof Instant) {
            return LargeInteger.of(((Instant) object).getEpochSecond());
        } else if (object instanceof CharSequence) {
            return parse(object.toString());
        } else {
            throw new IllegalArgumentException("Can not convert to number: " + object);
        }
    }

    /**
     * Converts the given object to int losslessly if possible.
     * 
     * @param object Any object
     * @throws IllegalArgumentException if the lossless conversion is not possible
     * @return the int value if applicable
     */
    public static int asInt(Object object) {
        try {
            return asIntInternal(object);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private static int asIntInternal(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Null cannot be converted to int");
        } else if (object instanceof LargeInteger) {
            return ((LargeInteger) object).intValueExact();
        } else if (object instanceof Integer) {
            return (Integer) object;
        } else if (object instanceof Long) {
            return Math.toIntExact((Long) object);
        } else if (object instanceof Short || object instanceof Byte) {
            return ((Number) object).intValue();
        } else if (object instanceof BigInteger) {
            return ((BigInteger) object).intValueExact();
        } else if (object instanceof BigDecimal) {
            return ((BigDecimal) object).intValueExact();
        } else if (object instanceof Number) {
            double doubleValue = ((Number) object).doubleValue();
            int intValue = (int) doubleValue;
            if (doubleValue != (double) intValue) {
                throw new IllegalArgumentException("Floating-point number can not be converted to int: " + object);
            }
            return intValue;
        } else if (object instanceof Boolean) {
            return ((Boolean) object) ? 1 : 0;
        } else if (object instanceof CharSequence) {
            return Integer.parseInt(object.toString());
        } else {
            throw new IllegalArgumentException("Value cannot be converted convert to int, type: " + object.getClass());
        }
    }

    /**
     * Checks if the given number is zero.
     * 
     * <p>Appropriate for non-zero checks and boolean conversion.</p>
     * <p>In this context, null is not zero.</p>
     * <p>Possible <code>numberify()</code> results are handled first for performance reasons.</p>
     * 
     * @param number A numberic object
     * @return true if the given number is zero, false otherwise
     */
    public static boolean isZero(Number number) {
        if (number instanceof LargeInteger) {
            return ((LargeInteger) number).isZero();
        } else if (number instanceof BigDecimal) {
            return ((BigDecimal) number).signum() == 0;
        } else if (number == null) {
            return false;
        } else if (
                number instanceof Long ||
                number instanceof Integer ||
                number instanceof Short ||
                number instanceof Byte) {
            return number.longValue() == 0;
        } else if (number instanceof BigInteger) {
            return ((BigInteger) number).signum() == 0;
        } else {
            return number.doubleValue() == 0.0;
        }
    }
    
    /**
     * Parses the given {@link String} to {@link BigDecimal}.
     * 
     * <p>Works for any input (cleans the text first).</p>
     */
    public static BigDecimal parse(String numberString) {
        if (numberString == null) {
            return null;
        }
        
        String cleanNumberString = NUMBER_CLEAN_PATTERN.matcher(numberString).replaceAll("");
        Matcher prefixMatcher = NUMBER_PREFIX_PATTERN.matcher(cleanNumberString);
        if (!prefixMatcher.find()) {
            throw new IllegalArgumentException("Failed to parse number: " + numberString);
        }

        String signPart = prefixMatcher.group("sign");
        String wholePart = prefixMatcher.group("whole");
        String fracPart = prefixMatcher.group("frac");
        
        StringBuilder decimalBuilder = new StringBuilder();
        decimalBuilder.append(signPart);
        decimalBuilder.append(wholePart != null ? wholePart : "0");
        decimalBuilder.append(fracPart != null ? fracPart : "");
        return new BigDecimal(decimalBuilder.toString());
    }

    /**
     * Converts the given object to {@link BigDecimal}.
     */
    public static BigDecimal bigDecimalify(Object value) {
        Number numericValue = numberify(value);
        if (numericValue instanceof BigDecimal) {
            return (BigDecimal) numericValue;
        } else if (numericValue instanceof LargeInteger) {
            return ((LargeInteger) numericValue).bigDecimalValue();
        } else if (value == null) {
            return null;
        } else {
            BigDecimal candidate = new BigDecimal(numericValue.doubleValue());
            if (candidate.scale() <= 14) {
                return candidate;
            } else {
                return candidate.setScale(14, RoundingMode.HALF_UP).stripTrailingZeros();
            }
        }
    }

    /**
     * Converts the given object to the given numeric type.
     */
    public static Number convertToNumber(Object value, Class<?> targetType, Integer size, Integer scale) {
        if (value == null) {
            return null;
        } else if (targetType == LargeInteger.class) {
            return convertToLargeInteger(value, size);
        } else if (targetType == BigDecimal.class) {
            return convertToBigDecimal(value, size, scale);
        } else if (targetType == Double.class) {
            return NumberUtil.numberify(value).doubleValue();
        } else if (targetType == Float.class) {
            return NumberUtil.numberify(value).floatValue();
        } else if (targetType == Long.class) {
            return convertToLargeInteger(value, size).longValue();
        } else if (targetType == Integer.class) {
            return convertToLargeInteger(value, size).intValue();
        } else if (targetType == Short.class) {
            return convertToLargeInteger(value, size).shortValue();
        } else if (targetType == Byte.class) {
            return convertToLargeInteger(value, size).byteValue();
        } else if (targetType == BigInteger.class) {
            return convertToLargeInteger(value, size).bigIntegerValue();
        } else {
            throw new IllegalArgumentException("Can not convert value to " + targetType);
        }
    }

    private static BigDecimal convertToBigDecimal(Object value, Integer size, Integer scale) {
        BigDecimal bigDecimalValue = NumberUtil.bigDecimalify(value);
        if (scale != null) {
            bigDecimalValue = bigDecimalValue.setScale(scale, RoundingMode.FLOOR);
        }
        int effectiveScale = bigDecimalValue.scale();

        if (size == null) {
            return bigDecimalValue;
        }

        int currentPrecision = bigDecimalValue.precision();
        if (size >= currentPrecision) {
            return bigDecimalValue;
        }

        return createBoundaryBigDecimal(size, effectiveScale, bigDecimalValue.signum() == -1);
    }

    private static BigDecimal createBoundaryBigDecimal(int size, int scale, boolean isNegative) {
        if (size == 0 && scale == 0) {
            return BigDecimal.ZERO;
        }

        String prefix = isNegative ? "-" : "";
        int wholeSize = size - scale;
        String wholePart = wholeSize > 0 ? repeat('9', wholeSize) : "0";
        int interSize = size >= scale ? 0 : scale - size;
        String interPart = repeat('0', interSize);
        int fractionSize = size >= scale ? scale : size;
        String fractionPart = repeat('9', fractionSize);
        String stringValue = prefix + wholePart + "." + interPart + fractionPart;
        return new BigDecimal(stringValue);
    }

    private static LargeInteger convertToLargeInteger(Object value, Integer size) {
        LargeInteger largeIntegerValue = largeIntegerify(value);
        if (size == null) {
            return largeIntegerValue;
        }

        int currentPrecision = largeIntegerValue.abs().toString().length();
        if (size >= currentPrecision) {
            return largeIntegerValue;
        }

        return createBoundaryLargeInteger(size, largeIntegerValue.isNegative());
    }

    private static LargeInteger largeIntegerify(Object value) {
        Number numericValue = NumberUtil.numberify(value);
        if (numericValue instanceof LargeInteger) {
            return (LargeInteger) numericValue;
        } else if (numericValue instanceof BigDecimal) {
            return LargeInteger.of(((BigDecimal) numericValue).toBigInteger());
        } else {
            return LargeInteger.of(numericValue.longValue());
        }
    }

    private static LargeInteger createBoundaryLargeInteger(int size, boolean isNegative) {
        if (size == 0) {
            return LargeInteger.ZERO;
        }

        String prefix = isNegative ? "-" : "";
        return LargeInteger.of(prefix + repeat('9', size));
    }

    private static String repeat(char c, int n) {
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            resultBuilder.append(c);
        }
        return resultBuilder.toString();
    }

}
