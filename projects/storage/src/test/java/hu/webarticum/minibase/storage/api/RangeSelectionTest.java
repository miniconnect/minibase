package hu.webarticum.minibase.storage.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class RangeSelectionTest {

    @Test
    void testEmpty() {
        RangeSelection selection = new RangeSelection(0L, 0L);
        assertThat(selection).isEmpty();
        assertThat(selection.containsRow(LargeInteger.of(3L))).isFalse();
    }

    @Test
    void testAsc() {
        RangeSelection selection = new RangeSelection(
                LargeInteger.of(3L),
                LargeInteger.of(10L),
                true);
        assertThat(selection).containsExactly(LargeInteger.arrayOf(3, 4, 5, 6, 7, 8, 9));
        assertThat(selection.containsRow(LargeInteger.of(0L))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(3L))).isTrue();
        assertThat(selection.containsRow(LargeInteger.of(7L))).isTrue();
        assertThat(selection.containsRow(LargeInteger.of(10L))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(15L))).isFalse();
    }

    @Test
    void testDesc() {
        RangeSelection selection = new RangeSelection(
                LargeInteger.of(3L),
                LargeInteger.of(10L),
                false);
        assertThat(selection).containsExactly(LargeInteger.arrayOf(9, 8, 7, 6, 5, 4, 3));
        assertThat(selection.containsRow(LargeInteger.of(0L))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(3L))).isTrue();
        assertThat(selection.containsRow(LargeInteger.of(7L))).isTrue();
        assertThat(selection.containsRow(LargeInteger.of(10L))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(15L))).isFalse();
    }
    
}
