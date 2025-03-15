package hu.webarticum.minibase.query.util;

import java.math.BigDecimal;
import java.math.BigInteger;
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
public class NumberParser {
    
    private static final Pattern NUMBER_CLEAN_PATTERN = Pattern.compile("[ _]");
    
    private static final Pattern NUMBER_PREFIX_PATTERN = Pattern.compile(
            "^(?<sign>[+\\-]?)(?<whole>\\d+)?(?<frac>\\.\\d+)?");
    

    private NumberParser() {
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
        ImmutableList<Class<?>> numericTypes = ImmutableList.of(types).map(NumberParser::numberifyType);
        Class<?>[] typesToCheck = new Class<?>[] { Double.class, BigDecimal.class, LargeInteger.class };
        for (Class<?> type : typesToCheck) {
            if (numericTypes.contains(type)) {
                return type;
            }
        }
        return Void.class;
    }
    
    public static Object promote(Object convertedValue, Class<?> targetType) {
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
                return convertedValue;
            } else {
                throw new IllegalArgumentException("Can not promote " + convertedValue + " to " + LargeInteger.class);
            }
        } else if (targetType == BigDecimal.class) {
            if (convertedValue instanceof LargeInteger) {
                return ((LargeInteger) convertedValue).bigDecimalValue();
            } else if (convertedValue instanceof BigDecimal) {
                return convertedValue;
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
            return convertedValue;
        } else {
            throw new IllegalArgumentException("Unsupported value for promotion: " + convertedValue);
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

}
