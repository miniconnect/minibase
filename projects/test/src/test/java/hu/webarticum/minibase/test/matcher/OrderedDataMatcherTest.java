package hu.webarticum.minibase.test.matcher;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderedDataMatcherTest {

    private Converter converter;

    private ImmutableList<MiniColumnHeader> columnHeaders;

    private ImmutableList<ValueTranslator> valueTranslators;

    private RecordMatcher recordMatcher;

    @Test
    void testMatchSuccessEmpty() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.empty();
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.empty();
        assertThatCode(() -> OrderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData))
                .doesNotThrowAnyException();
    }

    @Test
    void testMatchSuccessSomeRecords() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "ipsum", "Some other description"),
                buildRecord(3, "dolor", null),
                buildRecord(4, "sit", "HELLO"),
                buildRecord(5, "amet", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "ipsum", "Some other description"),
                ImmutableList.of(3, "dolor", null),
                ImmutableList.of(4, "sit", "HELLO"),
                ImmutableList.of(5, "amet", null));
        assertThatCode(() -> OrderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData))
                .doesNotThrowAnyException();
    }

    @Test
    void testMatchFailWrongOrder() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(5, "amet", null),
                buildRecord(3, "dolor", null),
                buildRecord(4, "sit", "HELLO"),
                buildRecord(2, "ipsum", "Some other description"));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "ipsum", "Some other description"),
                ImmutableList.of(3, "dolor", null),
                ImmutableList.of(4, "sit", "HELLO"),
                ImmutableList.of(5, "amet", null));
        assertThatCode(() -> OrderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData))
                .isInstanceOf(MatchFailedException.class);
    }

    @Test
    void testMatchFailChanged() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "ipsum", "Some other description"),
                buildRecord(3, "dolor", null),
                buildRecord(4, "sit", "HELLO"),
                buildRecord(5, "amet", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "ipsum", "Some other description"),
                ImmutableList.of(3, "DOLOR", null),
                ImmutableList.of(4, "sit", "HELLO"),
                ImmutableList.of(5, "amet", null));
        assertThatCode(() -> OrderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData))
                .isInstanceOf(MatchFailedException.class);
    }

    @Test
    void testMatchFailMissing() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "ipsum", "Some other description"),
                buildRecord(3, "dolor", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "ipsum", "Some other description"),
                ImmutableList.of(3, "dolor", null),
                ImmutableList.of(4, "sit", "HELLO"),
                ImmutableList.of(5, "amet", null));
        assertThatCode(() -> OrderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData))
                .isInstanceOf(MatchFailedException.class);
    }

    @Test
    void testMatchFailAdditional() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "ipsum", "Some other description"),
                buildRecord(3, "dolor", null),
                buildRecord(4, "sit", "HELLO"),
                buildRecord(5, "amet", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "ipsum", "Some other description"),
                ImmutableList.of(3, "dolor", null),
                ImmutableList.of(4, "sit", "HELLO"),
                ImmutableList.of(5, "amet", null),
                ImmutableList.of(6, "xxx", "Additional row"),
                ImmutableList.of(7, "yyy", null));
        assertThatCode(() -> OrderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData))
                .isInstanceOf(MatchFailedException.class);
    }

    private ResultRecord buildRecord(int id, String label, String description) {
        ImmutableList<Object> rowData = ImmutableList.of(id, label, description);
        ImmutableList<MiniValue> row = valueTranslators.map((i, t) -> t.encodeFully(rowData.get(i)));
        return new ResultRecord(columnHeaders, row, valueTranslators, converter);
    }

    @BeforeEach
    void prepare() {
        converter = new DefaultConverter();
        columnHeaders = ImmutableList.of(
                buildColumnHeader("id", false, StandardValueType.INT),
                buildColumnHeader("label", false, StandardValueType.STRING),
                buildColumnHeader("description", true, StandardValueType.STRING));
        valueTranslators = ImmutableList.of(
                StandardValueType.INT.translatorFor(ImmutableMap.empty()),
                StandardValueType.STRING.translatorFor(ImmutableMap.empty()),
                StandardValueType.STRING.translatorFor(ImmutableMap.empty()));
        FieldMatcher fieldMatcher = EqualityFieldMatcher.instance();
        recordMatcher = DefaultRecordMatcher.of(ImmutableList.of(fieldMatcher, fieldMatcher, fieldMatcher));
    }

    private MiniColumnHeader buildColumnHeader(String name, boolean isNullable, StandardValueType type) {
        return StoredColumnHeader.of(name, isNullable, StoredValueDefinition.of(type.name()));
    }

}
