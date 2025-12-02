package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class NumberUtilTest {

    @Test
    void testNumberifyType() {
        ImmutableList<Class<?>> types = createValues().map(this::classOf).map(NumberUtil::numberifyType);
        ImmutableList<Class<?>> exceptedTypes = ImmutableList.of(
                Void.class,
                LargeInteger.class,
                Double.class,
                Double.class,
                BigDecimal.class,
                BigDecimal.class,
                BigDecimal.class,
                LargeInteger.class,
                LargeInteger.class,
                LargeInteger.class,
                LargeInteger.class,
                LargeInteger.class,
                BigDecimal.class,
                LargeInteger.class,
                LargeInteger.class);
        assertThat(types).isEqualTo(exceptedTypes);
    }

    @Test
    void testNumberifyTypeInvalid() {
        assertThatThrownBy(() -> NumberUtil.numberifyType(Optional.class))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void testCommonNumericTypeOf1() {
        Class<?> commonType = NumberUtil.commonNumericTypeOf(
                Void.class,
                Integer.class,
                Boolean.class);
        Class<?> expectedCommonType = LargeInteger.class;
        assertThat(commonType).isEqualTo(expectedCommonType);
    }

    @Test
    void testCommonNumericTypeOf2() {
        Class<?> commonType = NumberUtil.commonNumericTypeOf(
                Void.class,
                String.class,
                Integer.class,
                Boolean.class);
        Class<?> expectedCommonType = BigDecimal.class;
        assertThat(commonType).isEqualTo(expectedCommonType);
    }

    @Test
    void testCommonNumericTypeOf3() {
        Class<?> commonType = NumberUtil.commonNumericTypeOf(
                Void.class,
                String.class,
                Integer.class,
                Float.class,
                BigDecimal.class,
                Boolean.class);
        Class<?> expectedCommonType = Double.class;
        assertThat(commonType).isEqualTo(expectedCommonType);
    }

    @Test
    void testPromoteInvalidParameter() {
        assertThatThrownBy(() -> NumberUtil.promote(1.1d, Void.class)) // not void
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.promote(2.2d, Float.class)) // invalid target
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.promote(2.2d, Optional.class)) // non-numeric target
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.promote(0.5f, Double.class)) // invalid source type
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPromoteInvalidPromotion() {
        assertThatThrownBy(() -> NumberUtil.promote(3.2d, LargeInteger.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.promote(3.2d, BigDecimal.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.promote(BigDecimal.valueOf(33L), LargeInteger.class))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void testPromote() {
        assertThat(NumberUtil.promote(null, Void.class)).isNull();
        assertThat(NumberUtil.promote(LargeInteger.of(10), LargeInteger.class)).isEqualTo(LargeInteger.of(10));
        assertThat(NumberUtil.promote(LargeInteger.of(10), BigDecimal.class)).isEqualTo(BigDecimal.valueOf(10));
        assertThat(NumberUtil.promote(LargeInteger.of(10), Double.class)).isEqualTo(10d);
        assertThat(NumberUtil.promote(BigDecimal.valueOf(20), BigDecimal.class)).isEqualTo(BigDecimal.valueOf(20));
        assertThat(NumberUtil.promote(BigDecimal.valueOf(20), Double.class)).isEqualTo(20d);
        assertThat(NumberUtil.promote(30d, Double.class)).isEqualTo(30d);
    }

    @Test
    void testUnify() {
        assertThat(NumberUtil.unify(null, null)).containsExactly(null, null);
        assertThat(NumberUtil.unify(Integer.valueOf(1), null)).containsExactly(LargeInteger.of(1), null);
        assertThat(NumberUtil.unify(LargeInteger.of(756), LargeInteger.of(99))).containsExactly(LargeInteger.of(756), LargeInteger.of(99));
        assertThat(NumberUtil.unify(null, LargeInteger.of(1))).containsExactly(null, LargeInteger.of(1));
        assertThat(NumberUtil.unify(Float.valueOf(0.4f), Integer.valueOf(1))).containsExactly(Double.valueOf((double) 0.4f), Double.valueOf(1));
        assertThat(NumberUtil.unify(LargeInteger.of(5), Integer.valueOf(7))).containsExactly(LargeInteger.of(5), LargeInteger.of(7));
        assertThat(NumberUtil.unify(new BigDecimal("0.47"), Integer.valueOf(7))).containsExactly(new BigDecimal("0.47"), new BigDecimal("7.00"));
        assertThat(NumberUtil.unify(Double.valueOf(7), new BigDecimal("0.32"))).containsExactly(Double.valueOf(7), Double.valueOf(0.32));
    }

    @Test
    void testNumberify() {
        ImmutableList<Number> numbers = createValues().map(NumberUtil::numberify);
        ImmutableList<Number> exceptedNumbers = ImmutableList.of(
                null,
                LargeInteger.of(9),
                Double.valueOf(3d),
                Double.valueOf(4.2d),
                new BigDecimal("52"),
                new BigDecimal("3.14"),
                new BigDecimal("-47084757718470983457098457834557490823479348570982384"),
                LargeInteger.ONE,
                LargeInteger.ZERO,
                LargeInteger.of(48),
                LargeInteger.of(63),
                LargeInteger.of(55),
                new BigDecimal("82.123"),
                LargeInteger.of(12),
                LargeInteger.of(15));
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testNumberifyInvalid() {
        assertThatThrownBy(() -> NumberUtil.numberify(Optional.empty())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testToInt() {
        assertThat(NumberUtil.asInt(0)).isZero();
        assertThat(NumberUtil.asInt(15)).isEqualTo(15);
        assertThat(NumberUtil.asInt(-42)).isEqualTo(-42);
        assertThat(NumberUtil.asInt(73L)).isEqualTo(73);
        assertThat(NumberUtil.asInt(-12L)).isEqualTo(-12);
        assertThat(NumberUtil.asInt((byte) -27)).isEqualTo(-27);
        assertThat(NumberUtil.asInt((short) 135)).isEqualTo(135);
        assertThat(NumberUtil.asInt(LargeInteger.ZERO)).isZero();
        assertThat(NumberUtil.asInt(LargeInteger.of(42))).isEqualTo(42);
        assertThat(NumberUtil.asInt(LargeInteger.of(-11))).isEqualTo(-11);
        assertThat(NumberUtil.asInt(LargeInteger.of(Integer.MAX_VALUE))).isEqualTo(Integer.MAX_VALUE);
        assertThat(NumberUtil.asInt(LargeInteger.of(Integer.MIN_VALUE))).isEqualTo(Integer.MIN_VALUE);
        assertThat(NumberUtil.asInt(BigInteger.valueOf(123))).isEqualTo(123);
        assertThat(NumberUtil.asInt(BigInteger.valueOf(-401))).isEqualTo(-401);
        assertThat(NumberUtil.asInt(2.0f)).isEqualTo(2);
        assertThat(NumberUtil.asInt(-12.f)).isEqualTo(-12);
        assertThat(NumberUtil.asInt(5.0)).isEqualTo(5);
        assertThat(NumberUtil.asInt(-4.0)).isEqualTo(-4);
        assertThat(NumberUtil.asInt(new BigDecimal("41223"))).isEqualTo(41223);
        assertThat(NumberUtil.asInt(new BigDecimal("425.0"))).isEqualTo(425);
        assertThat(NumberUtil.asInt(new BigDecimal("824.000"))).isEqualTo(824);
        assertThat(NumberUtil.asInt(new BigDecimal("-12.0"))).isEqualTo(-12);
        assertThat(NumberUtil.asInt(new BigDecimal("-55.00"))).isEqualTo(-55);
    }

    @Test
    void testToIntInvalid() {
        assertThatThrownBy(() -> NumberUtil.asInt(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt(3.2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt("1.55")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt("lorem")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt(Float.POSITIVE_INFINITY)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt(Double.NaN)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt(new BigInteger("999999999999999999"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt(LargeInteger.of("999999999999999999"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt(LargeInteger.of("-999999999999999999"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt(new BigDecimal("1.3"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberUtil.asInt(999999999999999999L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParse() {
        ImmutableList<BigDecimal> numbers = createStringValues().map(NumberUtil::parse);
        ImmutableList<BigDecimal> exceptedNumbers = ImmutableList.of(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("7"),
                new BigDecimal("5"),
                new BigDecimal("12375"),
                new BigDecimal("54200000"),
                new BigDecimal("6000000"),
                new BigDecimal("284357293457348534754"),
                new BigDecimal("-774780297457783883791082794711289472374793446882339413021"),
                new BigDecimal("54352356"),
                new BigDecimal("17.54"),
                new BigDecimal("22.100"),
                new BigDecimal("4322.125400"),
                new BigDecimal("-12.34"),
                new BigDecimal("-0.00700"),
                new BigDecimal("-12.15"),
                new BigDecimal("-12.6"),
                new BigDecimal("-12.44"),
                new BigDecimal("20.01"),
                new BigDecimal("0.0000200"),
                new BigDecimal("20.000000"),
                new BigDecimal("30.000000"),
                new BigDecimal("40.000000"),
                new BigDecimal("0.340"),
                new BigDecimal("-0.0210"),
                new BigDecimal("0.0540"));
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testBigDecimality() {
        ImmutableList<BigDecimal> numbers = createValues().map(NumberUtil::bigDecimalify);
        ImmutableList<BigDecimal> exceptedNumbers = ImmutableList.of(
                null,
                new BigDecimal("9"),
                new BigDecimal("3"),
                new BigDecimal("4.2"),
                new BigDecimal("52"),
                new BigDecimal("3.14"),
                new BigDecimal("-47084757718470983457098457834557490823479348570982384"),
                new BigDecimal("1"),
                new BigDecimal("0"),
                new BigDecimal("48"),
                new BigDecimal("63"),
                new BigDecimal("55"),
                new BigDecimal("82.123"),
                new BigDecimal("12"),
                new BigDecimal("15"));
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testConvertToNumberByte() {
        ImmutableList<Byte> numbers = createValues().map(v -> (Byte) NumberUtil.convertToNumber(v, Byte.class, null, null));
        ImmutableList<Byte> exceptedNumbers = ImmutableList.of(
                null,
                (byte) 9,
                (byte) 3,
                (byte) 4,
                (byte) 52,
                (byte) 3,
                (byte) 16,
                (byte) 1,
                (byte) 0,
                (byte) 48,
                (byte) 63,
                (byte) 55,
                (byte) 82,
                (byte) 12,
                (byte) 15);
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testConvertToNumberShort() {
        ImmutableList<Short> numbers = createValues().map(v -> (Short) NumberUtil.convertToNumber(v, Short.class, null, null));
        ImmutableList<Short> exceptedNumbers = ImmutableList.of(
                null,
                (short) 9,
                (short) 3,
                (short) 4,
                (short) 52,
                (short) 3,
                (short) 30736,
                (short) 1,
                (short) 0,
                (short) 48,
                (short) 63,
                (short) 55,
                (short) 82,
                (short) 12,
                (short) 15);
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testConvertToNumberInteger() {
        ImmutableList<Integer> numbers = createValues().map(v -> (Integer) NumberUtil.convertToNumber(v, Integer.class, null, null));
        ImmutableList<Integer> exceptedNumbers = ImmutableList.of(
                null,
                9,
                3,
                4,
                52,
                3,
                1258715152,
                1,
                0,
                48,
                63,
                55,
                82,
                12,
                15);
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testConvertToNumberLong() {
        ImmutableList<Long> numbers = createValues().map(v -> (Long) NumberUtil.convertToNumber(v, Long.class, null, null));
        ImmutableList<Long> exceptedNumbers = ImmutableList.of(
                null,
                9L,
                3L,
                4L,
                52L,
                3L,
                5976643574295853072L,
                1L,
                0L,
                48L,
                63L,
                55L,
                82L,
                12L,
                15L);
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testConvertToNumberLargeInteger() {
        ImmutableList<LargeInteger> numbers = createValues().map(v -> (LargeInteger) NumberUtil.convertToNumber(v, LargeInteger.class, null, null));
        ImmutableList<LargeInteger> exceptedNumbers = ImmutableList.of(
                null,
                LargeInteger.of("9"),
                LargeInteger.of("3"),
                LargeInteger.of("4"),
                LargeInteger.of("52"),
                LargeInteger.of("3"),
                LargeInteger.of("-47084757718470983457098457834557490823479348570982384"),
                LargeInteger.of("1"),
                LargeInteger.of("0"),
                LargeInteger.of("48"),
                LargeInteger.of("63"),
                LargeInteger.of("55"),
                LargeInteger.of("82"),
                LargeInteger.of("12"),
                LargeInteger.of("15"));
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testConvertToNumberLargeIntegerWithSize() {
        assertThat(NumberUtil.convertToNumber("7529801", LargeInteger.class, 20, null)).isEqualTo(LargeInteger.of("7529801"));
        assertThat(NumberUtil.convertToNumber("7529801", LargeInteger.class, 3, null)).isEqualTo(LargeInteger.of("999"));
        assertThat(NumberUtil.convertToNumber("-12345", LargeInteger.class, 4, null)).isEqualTo(LargeInteger.of("-9999"));
        assertThat(NumberUtil.convertToNumber("-12345", LargeInteger.class, 5, null)).isEqualTo(LargeInteger.of("-12345"));
        assertThat(NumberUtil.convertToNumber("-12345", LargeInteger.class, 6, null)).isEqualTo(LargeInteger.of("-12345"));
    }

    @Test
    void testConvertToNumberBigInteger() {
        ImmutableList<BigInteger> numbers = createValues().map(v -> (BigInteger) NumberUtil.convertToNumber(v, BigInteger.class, null, null));
        ImmutableList<BigInteger> exceptedNumbers = ImmutableList.of(
                null,
                new BigInteger("9"),
                new BigInteger("3"),
                new BigInteger("4"),
                new BigInteger("52"),
                new BigInteger("3"),
                new BigInteger("-47084757718470983457098457834557490823479348570982384"),
                new BigInteger("1"),
                new BigInteger("0"),
                new BigInteger("48"),
                new BigInteger("63"),
                new BigInteger("55"),
                new BigInteger("82"),
                new BigInteger("12"),
                new BigInteger("15"));
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testConvertToNumberBigIntegerWithSize() {
        assertThat(NumberUtil.convertToNumber("7529801", BigInteger.class, 20, null)).isEqualTo(new BigInteger("7529801"));
        assertThat(NumberUtil.convertToNumber("7529801", BigInteger.class, 3, null)).isEqualTo(new BigInteger("999"));
        assertThat(NumberUtil.convertToNumber("-12345", BigInteger.class, 4, null)).isEqualTo(new BigInteger("-9999"));
        assertThat(NumberUtil.convertToNumber("-12345", BigInteger.class, 5, null)).isEqualTo(new BigInteger("-12345"));
        assertThat(NumberUtil.convertToNumber("-12345", BigInteger.class, 6, null)).isEqualTo(new BigInteger("-12345"));
    }

    @Test
    void testConvertToNumberBigDecimal() {
        ImmutableList<BigDecimal> numbers = createValues().map(v -> (BigDecimal) NumberUtil.convertToNumber(v, BigDecimal.class, null, null));
        ImmutableList<BigDecimal> exceptedNumbers = ImmutableList.of(
                null,
                new BigDecimal("9"),
                new BigDecimal("3"),
                new BigDecimal("4.2"),
                new BigDecimal("52"),
                new BigDecimal("3.14"),
                new BigDecimal("-47084757718470983457098457834557490823479348570982384"),
                new BigDecimal("1"),
                new BigDecimal("0"),
                new BigDecimal("48"),
                new BigDecimal("63"),
                new BigDecimal("55"),
                new BigDecimal("82.123"),
                new BigDecimal("12"),
                new BigDecimal("15"));
        assertThat(numbers).isEqualTo(exceptedNumbers);
    }

    @Test
    void testConvertToNumberBigDecimalWithSize() {
        assertThat(NumberUtil.convertToNumber("7529801", BigDecimal.class, 20, null)).isEqualTo(new BigDecimal("7529801"));
        assertThat(NumberUtil.convertToNumber("7529801", BigDecimal.class, 3, null)).isEqualTo(new BigDecimal("999"));
        assertThat(NumberUtil.convertToNumber("-12345", BigDecimal.class, 4, null)).isEqualTo(new BigDecimal("-9999"));
        assertThat(NumberUtil.convertToNumber("-12345", BigDecimal.class, 5, null)).isEqualTo(new BigDecimal("-12345"));
        assertThat(NumberUtil.convertToNumber("-12345", BigDecimal.class, 6, null)).isEqualTo(new BigDecimal("-12345"));
    }

    @Test
    void testConvertToNumberBigDecimalWithScale() {
        assertThat(NumberUtil.convertToNumber("0.3", BigDecimal.class, null, 0)).isEqualTo(new BigDecimal("0"));
        assertThat(NumberUtil.convertToNumber("0.3", BigDecimal.class, null, 2)).isEqualTo(new BigDecimal("0.30"));
        assertThat(NumberUtil.convertToNumber("0", BigDecimal.class, null, 0)).isEqualTo(new BigDecimal("0"));
        assertThat(NumberUtil.convertToNumber("0", BigDecimal.class, null, 2)).isEqualTo(new BigDecimal("0.00"));
        assertThat(NumberUtil.convertToNumber("12345", BigDecimal.class, null, 0)).isEqualTo(new BigDecimal("12345"));
        assertThat(NumberUtil.convertToNumber("12345", BigDecimal.class, null, 3)).isEqualTo(new BigDecimal("12345.000"));
        assertThat(NumberUtil.convertToNumber("-71", BigDecimal.class, null, 1)).isEqualTo(new BigDecimal("-71.0"));
    }

    @Test
    void testConvertToNumberBigDecimalWithSizeAndScale() {
        assertThat(NumberUtil.convertToNumber("0", BigDecimal.class, 0, 0)).isEqualTo(new BigDecimal("0"));
        assertThat(NumberUtil.convertToNumber("0", BigDecimal.class, 0, 2)).isEqualTo(new BigDecimal("0.00"));
        assertThat(NumberUtil.convertToNumber("0", BigDecimal.class, 3, 2)).isEqualTo(new BigDecimal("0.00"));
        assertThat(NumberUtil.convertToNumber("12", BigDecimal.class, 3, 0)).isEqualTo(new BigDecimal("12"));
        assertThat(NumberUtil.convertToNumber("12", BigDecimal.class, 3, 1)).isEqualTo(new BigDecimal("12.0"));
        assertThat(NumberUtil.convertToNumber("12", BigDecimal.class, 3, 2)).isEqualTo(new BigDecimal("9.99"));
        assertThat(NumberUtil.convertToNumber("-342.1", BigDecimal.class, 6, 0)).isEqualTo(new BigDecimal("-343"));
        assertThat(NumberUtil.convertToNumber("-342.1", BigDecimal.class, 6, 2)).isEqualTo(new BigDecimal("-342.10"));
        assertThat(NumberUtil.convertToNumber("-342.1", BigDecimal.class, 6, 4)).isEqualTo(new BigDecimal("-99.9999"));
        assertThat(NumberUtil.convertToNumber("-342.1", BigDecimal.class, 6, 6)).isEqualTo(new BigDecimal("-0.999999"));
        assertThat(NumberUtil.convertToNumber("-342.1", BigDecimal.class, 6, 8)).isEqualTo(new BigDecimal("-0.00999999"));
    }

    @Test
    void testConvertToNumberFloat() {
        ImmutableList<Float> numbers = createValues().map(v -> (Float) NumberUtil.convertToNumber(v, Float.class, null, null));
        ImmutableList<Float> exceptedNumbers = ImmutableList.of(
                null,
                9f,
                3f,
                4.2f,
                52f,
                3.14f,
                Float.POSITIVE_INFINITY,
                1f,
                0.000000000001f,
                48f,
                63f,
                55f,
                82.123f,
                12f,
                15f);
        assertThat(numbers.map(this::normalizeFloat)).isEqualTo(exceptedNumbers.map(this::normalizeFloat));
    }

    private Number normalizeFloat(Float floatValue) {
        if (floatValue == null || !Float.isFinite(floatValue)) {
            return null;
        } else {
            return BigDecimal.valueOf(floatValue).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        }
    }

    @Test
    void testConvertToNumberDouble() {
        ImmutableList<Double> numbers = createValues().map(v -> (Double) NumberUtil.convertToNumber(v, Double.class, null, null));
        ImmutableList<Double> exceptedNumbers = ImmutableList.of(
                null,
                9d,
                3d,
                4.2d,
                52d,
                3.14d,
                -47084757718470983457098457834557490823479348570982384d,
                1d,
                0d,
                48d,
                63d,
                55d,
                82.123d,
                12d,
                15d);
        assertThat(numbers.map(this::normalizeDouble)).isEqualTo(exceptedNumbers.map(this::normalizeDouble));
    }

    @Test
    void testDivideBigDecimalsExact() {
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.ZERO, BigDecimal.ONE)).isEqualTo(BigDecimal.ZERO);
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.ZERO, BigDecimal.valueOf(2))).isEqualTo(BigDecimal.ZERO);
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.ZERO, BigDecimal.valueOf(3))).isEqualTo(BigDecimal.ZERO);
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.ONE, BigDecimal.valueOf(4))).isEqualTo(new BigDecimal("0.25"));
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.ONE, BigDecimal.valueOf(-20))).isEqualTo(new BigDecimal("-0.05"));
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.valueOf(14), BigDecimal.valueOf(-20))).isEqualTo(new BigDecimal("-0.7"));
        assertThat(NumberUtil.divideBigDecimals(new BigDecimal("-5.6"), BigDecimal.valueOf(10))).isEqualTo(new BigDecimal("-0.56"));
        assertThat(NumberUtil.divideBigDecimals(new BigDecimal("93"), new BigDecimal("-1.6"))).isEqualTo(new BigDecimal("-58.125"));
    }

    @Test
    void testDivideBigDecimalsNonExact() {
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.ONE, BigDecimal.valueOf(3))).isEqualTo(new BigDecimal("0.33333333333333"));
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.valueOf(2), BigDecimal.valueOf(3))).isEqualTo(new BigDecimal("0.66666666666667"));
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.valueOf(-2), BigDecimal.valueOf(3))).isEqualTo(new BigDecimal("-0.66666666666667"));
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.valueOf(2), BigDecimal.valueOf(-3))).isEqualTo(new BigDecimal("-0.66666666666667"));
        assertThat(NumberUtil.divideBigDecimals(BigDecimal.valueOf(-2), BigDecimal.valueOf(-3))).isEqualTo(new BigDecimal("0.66666666666667"));
        assertThat(NumberUtil.divideBigDecimals(new BigDecimal("-93"), new BigDecimal("1.7"))).isEqualTo(new BigDecimal("-54.70588235294118"));
    }

    @Test
    void testDivideBigDecimalsByZero() {
        assertThatThrownBy(() -> NumberUtil.divideBigDecimals(BigDecimal.ZERO, BigDecimal.ZERO)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> NumberUtil.divideBigDecimals(BigDecimal.valueOf(12), BigDecimal.ZERO)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> NumberUtil.divideBigDecimals(BigDecimal.valueOf(-21), BigDecimal.ZERO)).isInstanceOf(ArithmeticException.class);
    }

    private Number normalizeDouble(Double doubleValue) {
        if (doubleValue == null || !Double.isFinite(doubleValue)) {
            return null;
        } else {
            return BigDecimal.valueOf(doubleValue).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        }
    }

    private Class<?> classOf(Object object) {
        if (object == null) {
            return Void.class;
        } else {
            return object.getClass();
        }
    }

    private ImmutableList<Object> createValues() {
        return ImmutableList.of(
                null,
                9,
                3f,
                4.2d,
                "52",
                "3.14",
                "-47084757718470983457098457834557490823479348570982384",
                true,
                false,
                '0',
                LargeInteger.of(63),
                BigInteger.valueOf(55),
                new BigDecimal("82.123"),
                (short) 12,
                (byte) 15);
    }

    private ImmutableList<String> createStringValues() {
        return ImmutableList.of(
                "",
                "-",
                "+",
                "lorem",
                "lorem2",
                "lorem 3",
                "-0",
                "7",
                "5m",
                "12375",
                "54 200 000",
                "6__000000",
                "284357293457348534754",
                "-774780297457783883791082794711289472374793446882339413021",
                "+54352356",
                "17.54",
                "22.100",
                "4 322. 12 54 00",
                "-12.34",
                "-0.00700",
                "-12.15-",
                "-12.6-lorem+ipsum",
                "-12.44 lorem",
                "+20.01",
                "+0.0000200",
                "+20.000000",
                "+30.000000 lorem ipsum",
                "+40.000000 lorem 62 ipsum",
                ".340",
                "-.02 10",
                "+ .05 40");
    }

}
