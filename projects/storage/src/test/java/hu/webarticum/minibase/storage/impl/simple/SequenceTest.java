package hu.webarticum.minibase.storage.impl.simple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class SequenceTest {

    @Test
    void testNegativeUntil() {
        assertThatThrownBy(() -> new Sequence(-10)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testEmpty() {
        assertThat(new Sequence(0)).isEmpty();
    }

    @Test
    void testSingle() {
        assertThat(new Sequence(1)).containsExactly(LargeInteger.ZERO);
    }
    
    @Test
    void testUntilFive() {
        assertThat(new Sequence(5)).containsExactly(LargeInteger.arrayOf(0, 1, 2, 3, 4));
    }
    
}
