package hu.webarticum.minibase.test.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredValueDefinition;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.type.StandardValueType;

import org.junit.jupiter.api.Test;

class DefaultTableHeaderMatcherTest {

    @Test
    void testMatch() {
        ImmutableList<MiniColumnHeader> columnHeaders = ImmutableList.of(
                buildColumnHeader("id", false, StandardValueType.INT),
                buildColumnHeader("label", false, StandardValueType.STRING));
        assertThat(DefaultTableHeaderMatcher.of(ImmutableList.empty()).match(columnHeaders)).isFalse();
        assertThat(DefaultTableHeaderMatcher.of(ImmutableList.of(h -> false)).match(columnHeaders)).isFalse();
        assertThat(DefaultTableHeaderMatcher.of(ImmutableList.of(h -> true)).match(columnHeaders)).isFalse();
        assertThat(DefaultTableHeaderMatcher.of(ImmutableList.of(h -> true, h -> false)).match(columnHeaders)).isFalse();
        assertThat(DefaultTableHeaderMatcher.of(ImmutableList.of(h -> true, h -> true)).match(columnHeaders)).isTrue();
        assertThat(DefaultTableHeaderMatcher.of(ImmutableList.of(
                h -> true, h -> true, h -> true)).match(columnHeaders)).isFalse();
        assertThat(DefaultTableHeaderMatcher.of(ImmutableList.of(
                h -> h.name().equals("id"), h -> h.name().startsWith("la"))).match(columnHeaders)).isTrue();
    }

    private MiniColumnHeader buildColumnHeader(String name, boolean isNullable, StandardValueType type) {
        return StoredColumnHeader.of(name, isNullable, StoredValueDefinition.of(type.name()));
    }

}
