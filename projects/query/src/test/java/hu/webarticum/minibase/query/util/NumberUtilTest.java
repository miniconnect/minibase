package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
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
