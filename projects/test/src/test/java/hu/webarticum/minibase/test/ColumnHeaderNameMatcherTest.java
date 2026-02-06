package hu.webarticum.minibase.test.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredValueDefinition;
import hu.webarticum.miniconnect.record.type.StandardValueType;

import org.junit.jupiter.api.Test;

class ColumnHeaderNameMatcherTest {

    @Test
    void testMatch() {
        assertThat(ColumnHeaderNameMatcher.of("").match(headerOfName(""))).isTrue();
        assertThat(ColumnHeaderNameMatcher.of("lorem").match(headerOfName("lorem"))).isTrue();
        assertThat(ColumnHeaderNameMatcher.of("lorem").match(headerOfName(""))).isFalse();
        assertThat(ColumnHeaderNameMatcher.of("").match(headerOfName("lorem"))).isFalse();
        assertThat(ColumnHeaderNameMatcher.of("LOREM").match(headerOfName("lorem"))).isFalse();
        assertThat(ColumnHeaderNameMatcher.of("lorem").match(headerOfName("lorem ipsum"))).isFalse();
        assertThat(ColumnHeaderNameMatcher.of("lorem ipsum").match(headerOfName("lorem"))).isFalse();
    }

    private MiniColumnHeader headerOfName(String name) {
        return StoredColumnHeader.of(name, false, StoredValueDefinition.of(StandardValueType.STRING.name()));
    }

}
