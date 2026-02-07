package hu.webarticum.minibase.test.matcher;

import static org.assertj.core.api.Assertions.assertThat;

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

class UnorderedDataMatcherTest {

    private Converter converter = new DefaultConverter();

    private ImmutableList<MiniColumnHeader> columnHeaders;

    private ImmutableList<ValueTranslator> valueTranslators;

    private RecordMatcher recordMatcher;

    @Test
    void testMatchSuccessEmpty() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.empty();
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.empty();
        assertThat(UnorderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData)).isTrue();
    }

    @Test
    void testMatchSuccessSameOrder() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord("lorem", "Some description"),
                buildRecord("lorem", "Some description"),
                buildRecord("amet", null),
                buildRecord("ipsum", "XYZ"),
                buildRecord("amet", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("amet", null),
                ImmutableList.of("ipsum", "XYZ"),
                ImmutableList.of("amet", null));
        assertThat(UnorderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData)).isTrue();
    }

    @Test
    void testMatchSuccessDifferentOrder() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord("amet", null),
                buildRecord("lorem", "Some description"),
                buildRecord("amet", null),
                buildRecord("lorem", "Some description"),
                buildRecord("ipsum", "XYZ"));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("amet", null),
                ImmutableList.of("ipsum", "XYZ"),
                ImmutableList.of("amet", null));
        assertThat(UnorderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData)).isTrue();
    }

    @Test
    void testMatchFailChanged() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord("lorem", "Some description"),
                buildRecord("lorem", "Some description"),
                buildRecord("amet", null),
                buildRecord("ipsum", "xyz"),
                buildRecord("amet", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("amet", null),
                ImmutableList.of("ipsum", "XYZ"),
                ImmutableList.of("amet", null));
        assertThat(UnorderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchFailMissing() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord("lorem", "Some description"),
                buildRecord("lorem", "Some description"),
                buildRecord("amet", null),
                buildRecord("amet", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("amet", null),
                ImmutableList.of("ipsum", "XYZ"),
                ImmutableList.of("amet", null));
        assertThat(UnorderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchFailMissingDuplication() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord("lorem", "Some description"),
                buildRecord("amet", null),
                buildRecord("ipsum", "XYZ"),
                buildRecord("amet", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("amet", null),
                ImmutableList.of("ipsum", "XYZ"),
                ImmutableList.of("amet", null));
        assertThat(UnorderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchFailAdditional() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord("lorem", "Some description"),
                buildRecord("lorem", "Some description"),
                buildRecord("amet", null),
                buildRecord("ipsum", "XYZ"),
                buildRecord("amet", null),
                buildRecord("123", "456"));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("amet", null),
                ImmutableList.of("ipsum", "XYZ"),
                ImmutableList.of("amet", null));
        assertThat(UnorderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchFailAdditionalDuplication() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord("lorem", "Some description"),
                buildRecord("lorem", "Some description"),
                buildRecord("amet", null),
                buildRecord("ipsum", "XYZ"),
                buildRecord("amet", null),
                buildRecord("ipsum", "XYZ"));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("lorem", "Some description"),
                ImmutableList.of("amet", null),
                ImmutableList.of("ipsum", "XYZ"),
                ImmutableList.of("amet", null));
        assertThat(UnorderedDataMatcher.of(recordMatcher).match(givenRecords, expectedData)).isFalse();
    }

    private ResultRecord buildRecord(String label, String description) {
        ImmutableList<Object> rowData = ImmutableList.of(label, description);
        ImmutableList<MiniValue> row = valueTranslators.map((i, t) -> t.encodeFully(rowData.get(i)));
        return new ResultRecord(columnHeaders, row, valueTranslators, converter);
    }

    @BeforeEach
    void prepare() {
        converter = new DefaultConverter();
        columnHeaders = ImmutableList.of(
                buildColumnHeader("label", false, StandardValueType.STRING),
                buildColumnHeader("description", true, StandardValueType.STRING));
        valueTranslators = ImmutableList.of(
                StandardValueType.STRING.translatorFor(ImmutableMap.empty()),
                StandardValueType.STRING.translatorFor(ImmutableMap.empty()));
        FieldMatcher fieldMatcher = EqualityFieldMatcher.instance();
        recordMatcher = DefaultRecordMatcher.of(ImmutableList.of(fieldMatcher, fieldMatcher));
    }

    private MiniColumnHeader buildColumnHeader(String name, boolean isNullable, StandardValueType type) {
        return StoredColumnHeader.of(name, isNullable, StoredValueDefinition.of(type.name()));
    }

}
