package hu.webarticum.minibase.test.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.api.MiniValue;
import hu.webarticum.miniconnect.impl.result.StoredColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredValueDefinition;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.record.ResultRecord;
import hu.webarticum.miniconnect.record.converter.Converter;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;
import hu.webarticum.miniconnect.record.translator.ValueTranslator;
import hu.webarticum.miniconnect.record.type.StandardValueType;

import org.junit.jupiter.api.Test;

class DefaultRecordMatcherTest {

    @Test
    void testIsMatching() throws Exception {
        ResultRecord record = buildRecord();
        FieldMatcher equality = EqualityFieldMatcher.instance();
        assertThat(DefaultRecordMatcher.of(ImmutableList.of((f, v) -> true
                )).isMatching(record, ImmutableList.of(1, "xxx"))).isFalse();
        assertThat(DefaultRecordMatcher.of(ImmutableList.of((f, v) -> true, (f, v) -> true
                )).isMatching(record, ImmutableList.of(1, "xxx"))).isTrue();
        assertThat(DefaultRecordMatcher.of(ImmutableList.of((f, v) -> true, (f, v) -> true, (f, v) -> true
                )).isMatching(record, ImmutableList.of(1, "xxx"))).isFalse();
        assertThat(DefaultRecordMatcher.of(ImmutableList.of(equality, equality
                )).isMatching(record, ImmutableList.of(1, "lorem"))).isTrue();
        assertThat(DefaultRecordMatcher.of(ImmutableList.of(equality, equality
                )).isMatching(record, ImmutableList.of(3, "lorem"))).isFalse();
        assertThat(DefaultRecordMatcher.of(ImmutableList.of(equality, equality
                )).isMatching(record, ImmutableList.of(1, "xxx"))).isFalse();
    }

    @Test
    void testMatch() {
        ResultRecord record = buildRecord();
        FieldMatcher equality = EqualityFieldMatcher.instance();
        assertThatCode(() -> DefaultRecordMatcher.of(ImmutableList.of((f, v) -> true
                )).match(record, ImmutableList.of(1, "xxx"))).isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> DefaultRecordMatcher.of(ImmutableList.of((f, v) -> true, (f, v) -> true
                )).match(record, ImmutableList.of(1, "xxx"))).doesNotThrowAnyException();
        assertThatCode(() -> DefaultRecordMatcher.of(ImmutableList.of((f, v) -> true, (f, v) -> true, (f, v) -> true
                )).match(record, ImmutableList.of(1, "xxx"))).isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> DefaultRecordMatcher.of(ImmutableList.of(equality, equality
                )).match(record, ImmutableList.of(1, "lorem"))).doesNotThrowAnyException();
        assertThatCode(() -> DefaultRecordMatcher.of(ImmutableList.of(equality, equality
                )).match(record, ImmutableList.of(3, "lorem"))).isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> DefaultRecordMatcher.of(ImmutableList.of(equality, equality
                )).match(record, ImmutableList.of(1, "xxx"))).isInstanceOf(MatchFailedException.class);
    }

    private ResultRecord buildRecord() {
        Converter converter = new DefaultConverter();
        ImmutableList<MiniColumnHeader> columnHeaders = ImmutableList.of(
                buildColumnHeader("id", false, StandardValueType.INT),
                buildColumnHeader("label", false, StandardValueType.STRING));
        ImmutableList<ValueTranslator> valueTranslators = ImmutableList.of(
                StandardValueType.INT.translatorFor(ImmutableMap.empty()),
                StandardValueType.STRING.translatorFor(ImmutableMap.empty()));
        ImmutableList<Object> rowData = ImmutableList.of(1, "lorem");
        ImmutableList<MiniValue> row = valueTranslators.map((i, t) -> t.encodeFully(rowData.get(i)));
        return new ResultRecord(columnHeaders, row, valueTranslators, converter);
    }

    private MiniColumnHeader buildColumnHeader(String name, boolean isNullable, StandardValueType type) {
        return StoredColumnHeader.of(name, isNullable, StoredValueDefinition.of(type.name()));
    }

}
