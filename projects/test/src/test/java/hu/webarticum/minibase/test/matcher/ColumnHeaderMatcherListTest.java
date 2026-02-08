package hu.webarticum.minibase.test.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredValueDefinition;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.type.StandardValueType;

import org.junit.jupiter.api.Test;

class ColumnHeaderMatcherListTest {

    @Test
    void testMatch() {
        MiniColumnHeader columnHeader = StoredColumnHeader.of(
            "lorem ipsum", false, StoredValueDefinition.of(StandardValueType.STRING.name()));
        assertThat(ColumnHeaderMatcherList.of(ImmutableList.empty()).match(columnHeader)).isTrue();
        assertThat(ColumnHeaderMatcherList.of(ImmutableList.of(h -> true)).match(columnHeader)).isTrue();
        assertThat(ColumnHeaderMatcherList.of(ImmutableList.of(h -> false)).match(columnHeader)).isFalse();
        assertThat(ColumnHeaderMatcherList.of(
                ImmutableList.of(h -> true, h -> false, h -> true)).match(columnHeader)).isFalse();
        assertThat(ColumnHeaderMatcherList.of(
                ImmutableList.of(h -> h.name().startsWith("lorem"))).match(columnHeader)).isTrue();
        assertThat(ColumnHeaderMatcherList.of(ImmutableList.of(
                        h -> h.name().startsWith("lorem"),
                        h -> !h.isNullable(),
                        h -> h.name().endsWith("ipsum")
                )).match(columnHeader)).isTrue();
    }

}
