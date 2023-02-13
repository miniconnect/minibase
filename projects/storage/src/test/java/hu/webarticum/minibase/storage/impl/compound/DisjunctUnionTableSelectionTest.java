package hu.webarticum.minibase.storage.impl.compound;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.minibase.storage.impl.simple.SimpleSelection;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class DisjunctUnionTableSelectionTest {

    @Test
    void testEmpty() {
        DisjunctUnionTableSelection selection = DisjunctUnionTableSelection.of();
        assertThat(selection.iterator()).isExhausted();
        assertThat(selection.containsRow(LargeInteger.of(1))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(2))).isFalse();
    }

    @Test
    void testOfEmpty() {
        DisjunctUnionTableSelection selection = DisjunctUnionTableSelection.of(
                new SimpleSelection(ImmutableList.empty()));
        assertThat(selection.iterator()).isExhausted();
        assertThat(selection.containsRow(LargeInteger.of(1))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(2))).isFalse();
    }

    @Test
    void testOfEmpties() {
        DisjunctUnionTableSelection selection = DisjunctUnionTableSelection.of(
                new SimpleSelection(ImmutableList.empty()));
        assertThat(selection.iterator()).isExhausted();
        assertThat(selection.containsRow(LargeInteger.of(1))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(2))).isFalse();
    }

    @Test
    void testOfOne() {
        DisjunctUnionTableSelection selection = DisjunctUnionTableSelection.of(
                new SimpleSelection(LargeInteger.arrayOf(2, 4, 6, 7, 10)));
        assertThat(selection).containsExactly(LargeInteger.arrayOf(2, 4, 6, 7, 10));
        assertThat(selection.containsRow(LargeInteger.of(1))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(2))).isTrue();
    }

    @Test
    void testOfTwo() {
        DisjunctUnionTableSelection selection = DisjunctUnionTableSelection.of(
                new SimpleSelection(LargeInteger.arrayOf(3, 6, 9)),
                new SimpleSelection(LargeInteger.arrayOf(4, 8, 12)));
        assertThat(selection).containsExactly(LargeInteger.arrayOf(3, 6, 9, 4, 8, 12));
        assertThat(selection.containsRow(LargeInteger.of(1))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(2))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(3))).isTrue();
        assertThat(selection.containsRow(LargeInteger.of(4))).isTrue();
    }

    @Test
    void testOfMany() {
        DisjunctUnionTableSelection selection = DisjunctUnionTableSelection.of(
                new SimpleSelection(LargeInteger.arrayOf(3, 6, 9)),
                new SimpleSelection(LargeInteger.arrayOf(4, 8, 12)),
                new SimpleSelection(ImmutableList.empty()),
                new SimpleSelection(LargeInteger.arrayOf(5)),
                new SimpleSelection(LargeInteger.arrayOf(10, 20)));
        assertThat(selection).containsExactly(LargeInteger.arrayOf(3, 6, 9, 4, 8, 12, 5, 10, 20));
        assertThat(selection.containsRow(LargeInteger.of(1))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(2))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(3))).isTrue();
        assertThat(selection.containsRow(LargeInteger.of(4))).isTrue();
        assertThat(selection.containsRow(LargeInteger.of(7))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(10))).isTrue();
        assertThat(selection.containsRow(LargeInteger.of(15))).isFalse();
        assertThat(selection.containsRow(LargeInteger.of(20))).isTrue();
        assertThat(selection.containsRow(LargeInteger.of(30))).isFalse();
    }
    
}
