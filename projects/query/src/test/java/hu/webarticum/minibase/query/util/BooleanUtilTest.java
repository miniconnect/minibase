package hu.webarticum.minibase.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;

class BooleanUtilTest {

    @Test
    void testBoolifyPrimitives() {
        ImmutableList<Object> inputValues = ImmutableList.of(
                null, false, true, 0, 0.0, 1);
        ImmutableList<Boolean> convertedValues = inputValues.map(BooleanUtil::boolify);
        ImmutableList<Boolean> exceptedValues = ImmutableList.of(
                null, false, true, false, false, true);
        assertThat(convertedValues).isEqualTo(exceptedValues);
    }

    @Test
    void testBoolifyStrings() {
        ImmutableList<Object> inputValues = ImmutableList.of(
                '0', '1', "0", "4", "true", "TrUe", "FALSE", "FaLSe");
        ImmutableList<Boolean> convertedValues = inputValues.map(BooleanUtil::boolify);
        ImmutableList<Boolean> exceptedValues = ImmutableList.of(
                false, true, false, true, true, true, false, false);
        assertThat(convertedValues).isEqualTo(exceptedValues);
    }

}
