package hu.webarticum.minibase.test.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import hu.webarticum.miniconnect.api.MiniValue;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.record.ResultField;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;
import hu.webarticum.miniconnect.record.type.StandardValueType;

import org.junit.jupiter.api.Test;

class EqualityFieldMatcherTest {

    @Test
    void testIsMatching() {
        assertThat(EqualityFieldMatcher.instance().isMatching(fieldOf(StandardValueType.INT, null), null)).isTrue();
        assertThat(EqualityFieldMatcher.instance().isMatching(fieldOf(StandardValueType.INT, 1), null)).isFalse();
        assertThat(EqualityFieldMatcher.instance().isMatching(fieldOf(StandardValueType.INT, null), 1)).isFalse();
        assertThat(EqualityFieldMatcher.instance().isMatching(fieldOf(StandardValueType.INT, 1), 1)).isTrue();
        assertThat(EqualityFieldMatcher.instance().isMatching(fieldOf(StandardValueType.INT, 1), 2)).isFalse();
        assertThat(EqualityFieldMatcher.instance().isMatching(fieldOf(StandardValueType.INT, 1), "xxx")).isFalse();
        assertThat(EqualityFieldMatcher.instance().isMatching(fieldOf(StandardValueType.STRING, "lorem"), "lorem")).isTrue();
        assertThat(EqualityFieldMatcher.instance().isMatching(fieldOf(StandardValueType.STRING, "lorem ipsum"), "lorem")).isFalse();
        assertThat(EqualityFieldMatcher.instance().isMatching(fieldOf(StandardValueType.STRING, "lorem"), "lorem ipsum")).isFalse();
    }

    @Test
    void testMatch() {
        assertThatCode(() -> EqualityFieldMatcher.instance().match(fieldOf(StandardValueType.INT, null), null))
                .doesNotThrowAnyException();
        assertThatCode(() -> EqualityFieldMatcher.instance().match(fieldOf(StandardValueType.INT, 1), null))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> EqualityFieldMatcher.instance().match(fieldOf(StandardValueType.INT, null), 1))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> EqualityFieldMatcher.instance().match(fieldOf(StandardValueType.INT, 1), 1))
                .doesNotThrowAnyException();
        assertThatCode(() -> EqualityFieldMatcher.instance().match(fieldOf(StandardValueType.INT, 1), 2))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> EqualityFieldMatcher.instance().match(fieldOf(StandardValueType.INT, 1), "xxx"))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> EqualityFieldMatcher.instance().match(fieldOf(StandardValueType.STRING, "lorem"), "lorem"))
                .doesNotThrowAnyException();
        assertThatCode(() -> EqualityFieldMatcher.instance().match(fieldOf(StandardValueType.STRING, "lorem ipsum"), "lorem"))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> EqualityFieldMatcher.instance().match(fieldOf(StandardValueType.STRING, "lorem"), "lorem ipsum"))
                .isInstanceOf(MatchFailedException.class);
    }

    private ResultField fieldOf(StandardValueType valueType, Object value) {
        MiniValue miniValue = valueType.translatorFor(ImmutableMap.empty()).encodeFully(value);
        return new ResultField(miniValue, value, new DefaultConverter());
    }

}
