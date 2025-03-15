package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class NumberParserTest {

    @Test
    void testNumberifyType() {
        ImmutableList<Class<?>> types = createValues().map(this::classOf).map(NumberParser::numberifyType);
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
        assertThatThrownBy(() -> NumberParser.numberifyType(Optional.class))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void testCommonNumericTypeOf1() {
        Class<?> commonType = NumberParser.commonNumericTypeOf(
                Void.class,
                Integer.class,
                Boolean.class);
        Class<?> expectedCommonType = LargeInteger.class;
        assertThat(commonType).isEqualTo(expectedCommonType);
    }

    @Test
    void testCommonNumericTypeOf2() {
        Class<?> commonType = NumberParser.commonNumericTypeOf(
                Void.class,
                String.class,
                Integer.class,
                Boolean.class);
        Class<?> expectedCommonType = BigDecimal.class;
        assertThat(commonType).isEqualTo(expectedCommonType);
    }

    @Test
    void testCommonNumericTypeOf3() {
        Class<?> commonType = NumberParser.commonNumericTypeOf(
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
        assertThatThrownBy(() -> NumberParser.promote(1.1d, Void.class)) // not void
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberParser.promote(2.2d, Float.class)) // invalid target
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberParser.promote(2.2d, Optional.class)) // non-numeric target
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberParser.promote(0.5f, Double.class)) // invalid source type
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPromoteInvalidPromotion() {
        assertThatThrownBy(() -> NumberParser.promote(3.2d, LargeInteger.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberParser.promote(3.2d, BigDecimal.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumberParser.promote(BigDecimal.valueOf(33L), LargeInteger.class))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void testPromote() {
        assertThat(NumberParser.promote(null, Void.class)).isNull();
        assertThat(NumberParser.promote(LargeInteger.of(10), LargeInteger.class)).isEqualTo(LargeInteger.of(10));
        assertThat(NumberParser.promote(LargeInteger.of(10), BigDecimal.class)).isEqualTo(BigDecimal.valueOf(10));
        assertThat(NumberParser.promote(LargeInteger.of(10), Double.class)).isEqualTo(10d);
        assertThat(NumberParser.promote(BigDecimal.valueOf(20), BigDecimal.class)).isEqualTo(BigDecimal.valueOf(20));
        assertThat(NumberParser.promote(BigDecimal.valueOf(20), Double.class)).isEqualTo(20d);
        assertThat(NumberParser.promote(30d, Double.class)).isEqualTo(30d);
    }

    @Test
    void testNumberify() {
        ImmutableList<Number> numbers = createValues().map(NumberParser::numberify);
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
        assertThatThrownBy(() -> NumberParser.numberify(Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void testParse() {
        ImmutableList<BigDecimal> numbers = createStringValues().map(NumberParser::parse);
        ImmutableList<Number> exceptedNumbers = ImmutableList.of(
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
