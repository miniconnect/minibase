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

class GroupingDataMatcherTest {

    private Converter converter;

    private ImmutableList<MiniColumnHeader> columnHeaders;

    private ImmutableList<ValueTranslator> valueTranslators;

    private RecordMatcher recordMatcher;

    @Test
    void testMatchSuccessEmpty() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.empty();
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.empty();
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isTrue();
    }

    @Test
    void testMatchSame() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "lorem", "Some other description"),
                buildRecord(3, "lorem", null),
                buildRecord(4, "ipsum", "HELLO"),
                buildRecord(5, "ipsum", null),
                buildRecord(6, "dolor", "===="),
                buildRecord(7, "lorem", "xyz"),
                buildRecord(8, "lorem", "123"),
                buildRecord(9, "sit", null),
                buildRecord(10, "sit", null),
                buildRecord(11, "amet", "Some description"));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "lorem", "Some other description"),
                ImmutableList.of(3, "lorem", null),
                ImmutableList.of(4, "ipsum", "HELLO"),
                ImmutableList.of(5, "ipsum", null),
                ImmutableList.of(6, "dolor", "===="),
                ImmutableList.of(7, "lorem", "xyz"),
                ImmutableList.of(8, "lorem", "123"),
                ImmutableList.of(9, "sit", null),
                ImmutableList.of(10, "sit", null),
                ImmutableList.of(11, "amet", "Some description"));
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(0)).match(givenRecords, expectedData)).isTrue();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isTrue();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(2)).match(givenRecords, expectedData)).isTrue();
    }

    @Test
    void testMatchInvariantOrder() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(2, "lorem", "Some other description"),
                buildRecord(3, "lorem", null),
                buildRecord(1, "lorem", "Some description"),
                buildRecord(4, "ipsum", "HELLO"),
                buildRecord(5, "ipsum", null),
                buildRecord(6, "dolor", "===="),
                buildRecord(8, "lorem", "123"),
                buildRecord(7, "lorem", "xyz"),
                buildRecord(10, "sit", null),
                buildRecord(9, "sit", null),
                buildRecord(11, "amet", "Some description"));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "lorem", "Some other description"),
                ImmutableList.of(3, "lorem", null),
                ImmutableList.of(4, "ipsum", "HELLO"),
                ImmutableList.of(5, "ipsum", null),
                ImmutableList.of(6, "dolor", "===="),
                ImmutableList.of(7, "lorem", "xyz"),
                ImmutableList.of(8, "lorem", "123"),
                ImmutableList.of(9, "sit", null),
                ImmutableList.of(10, "sit", null),
                ImmutableList.of(11, "amet", "Some description"));
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(0)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isTrue();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(2)).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchNonInvariantOrder() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(3, "lorem", null),
                buildRecord(8, "lorem", "123"),
                buildRecord(11, "amet", "Some description"),
                buildRecord(6, "dolor", "===="),
                buildRecord(1, "lorem", "Some description"),
                buildRecord(7, "lorem", "xyz"),
                buildRecord(10, "sit", null),
                buildRecord(5, "ipsum", null),
                buildRecord(2, "lorem", "Some other description"),
                buildRecord(9, "sit", null),
                buildRecord(4, "ipsum", "HELLO"));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "lorem", "Some other description"),
                ImmutableList.of(3, "lorem", null),
                ImmutableList.of(4, "ipsum", "HELLO"),
                ImmutableList.of(5, "ipsum", null),
                ImmutableList.of(6, "dolor", "===="),
                ImmutableList.of(7, "lorem", "xyz"),
                ImmutableList.of(8, "lorem", "123"),
                ImmutableList.of(9, "sit", null),
                ImmutableList.of(10, "sit", null),
                ImmutableList.of(11, "amet", "Some description"));
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(0)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(2)).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchMissingInGroup() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "lorem", "Some other description"),
                buildRecord(3, "lorem", null),
                buildRecord(4, "ipsum", "HELLO"),
                buildRecord(5, "ipsum", null),
                buildRecord(6, "dolor", "===="),
                buildRecord(7, "lorem", "xyz"),
                buildRecord(8, "lorem", "123"),
                buildRecord(9, "sit", null),
                buildRecord(11, "amet", "Some description"));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "lorem", "Some other description"),
                ImmutableList.of(3, "lorem", null),
                ImmutableList.of(4, "ipsum", "HELLO"),
                ImmutableList.of(5, "ipsum", null),
                ImmutableList.of(6, "dolor", "===="),
                ImmutableList.of(7, "lorem", "xyz"),
                ImmutableList.of(8, "lorem", "123"),
                ImmutableList.of(9, "sit", null),
                ImmutableList.of(10, "sit", null),
                ImmutableList.of(11, "amet", "Some description"));
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(0)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(2)).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchAdditionalInGroup() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "lorem", "Some other description"),
                buildRecord(3, "lorem", null),
                buildRecord(4, "ipsum", "HELLO"),
                buildRecord(5, "ipsum", null),
                buildRecord(6, "dolor", "===="),
                buildRecord(7, "lorem", "xyz"),
                buildRecord(8, "lorem", "123"),
                buildRecord(9, "sit", null),
                buildRecord(10, "sit", null),
                buildRecord(12, "sit", null),
                buildRecord(11, "amet", "Some description"));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "lorem", "Some other description"),
                ImmutableList.of(3, "lorem", null),
                ImmutableList.of(4, "ipsum", "HELLO"),
                ImmutableList.of(5, "ipsum", null),
                ImmutableList.of(6, "dolor", "===="),
                ImmutableList.of(7, "lorem", "xyz"),
                ImmutableList.of(8, "lorem", "123"),
                ImmutableList.of(9, "sit", null),
                ImmutableList.of(10, "sit", null),
                ImmutableList.of(11, "amet", "Some description"));
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(0)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(2)).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchAdditionalInLastGroup() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "lorem", "Some other description"),
                buildRecord(3, "lorem", null),
                buildRecord(4, "ipsum", "HELLO"),
                buildRecord(5, "ipsum", null),
                buildRecord(6, "dolor", "===="),
                buildRecord(7, "lorem", "xyz"),
                buildRecord(8, "lorem", "123"),
                buildRecord(9, "sit", null),
                buildRecord(10, "sit", null),
                buildRecord(11, "amet", "Some description"),
                buildRecord(12, "amet", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "lorem", "Some other description"),
                ImmutableList.of(3, "lorem", null),
                ImmutableList.of(4, "ipsum", "HELLO"),
                ImmutableList.of(5, "ipsum", null),
                ImmutableList.of(6, "dolor", "===="),
                ImmutableList.of(7, "lorem", "xyz"),
                ImmutableList.of(8, "lorem", "123"),
                ImmutableList.of(9, "sit", null),
                ImmutableList.of(10, "sit", null),
                ImmutableList.of(11, "amet", "Some description"));
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(0)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(2)).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchOneRemovalOneAddition() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "lorem", "Some other description"),
                buildRecord(3, "lorem", null),
                buildRecord(4, "ipsum", "HELLO"),
                buildRecord(5, "ipsum", null),
                buildRecord(6, "dolor", "===="),
                buildRecord(7, "lorem", "xyz"),
                buildRecord(8, "lorem", "123"),
                buildRecord(9, "sit", null),
                buildRecord(11, "amet", "Some description"),
                buildRecord(12, "amet", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "lorem", "Some other description"),
                ImmutableList.of(3, "lorem", null),
                ImmutableList.of(4, "ipsum", "HELLO"),
                ImmutableList.of(5, "ipsum", null),
                ImmutableList.of(6, "dolor", "===="),
                ImmutableList.of(7, "lorem", "xyz"),
                ImmutableList.of(8, "lorem", "123"),
                ImmutableList.of(9, "sit", null),
                ImmutableList.of(10, "sit", null),
                ImmutableList.of(11, "amet", "Some description"));
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(0)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(2)).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchMissingGroup() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "lorem", "Some other description"),
                buildRecord(3, "lorem", null),
                buildRecord(4, "ipsum", "HELLO"),
                buildRecord(5, "ipsum", null),
                buildRecord(6, "dolor", "===="),
                buildRecord(7, "lorem", "xyz"),
                buildRecord(8, "lorem", "123"),
                buildRecord(9, "sit", null),
                buildRecord(10, "sit", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "lorem", "Some other description"),
                ImmutableList.of(3, "lorem", null),
                ImmutableList.of(4, "ipsum", "HELLO"),
                ImmutableList.of(5, "ipsum", null),
                ImmutableList.of(6, "dolor", "===="),
                ImmutableList.of(7, "lorem", "xyz"),
                ImmutableList.of(8, "lorem", "123"),
                ImmutableList.of(9, "sit", null),
                ImmutableList.of(10, "sit", null),
                ImmutableList.of(11, "amet", "Some description"));
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(0)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(2)).match(givenRecords, expectedData)).isFalse();
    }

    @Test
    void testMatchAdditionalGroup() {
        ImmutableList<ResultRecord> givenRecords = ImmutableList.of(
                buildRecord(1, "lorem", "Some description"),
                buildRecord(2, "lorem", "Some other description"),
                buildRecord(3, "lorem", null),
                buildRecord(4, "ipsum", "HELLO"),
                buildRecord(5, "ipsum", null),
                buildRecord(6, "dolor", "===="),
                buildRecord(7, "lorem", "xyz"),
                buildRecord(8, "lorem", "123"),
                buildRecord(9, "sit", null),
                buildRecord(10, "sit", null),
                buildRecord(11, "amet", "Some description"),
                buildRecord(12, "additional", null));
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem", "Some description"),
                ImmutableList.of(2, "lorem", "Some other description"),
                ImmutableList.of(3, "lorem", null),
                ImmutableList.of(4, "ipsum", "HELLO"),
                ImmutableList.of(5, "ipsum", null),
                ImmutableList.of(6, "dolor", "===="),
                ImmutableList.of(7, "lorem", "xyz"),
                ImmutableList.of(8, "lorem", "123"),
                ImmutableList.of(9, "sit", null),
                ImmutableList.of(10, "sit", null),
                ImmutableList.of(11, "amet", "Some description"));
        DataMatcher groupDataMatcher = UnorderedDataMatcher.of(recordMatcher);
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(0)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(1)).match(givenRecords, expectedData)).isFalse();
        assertThat(GroupingDataMatcher.of(groupDataMatcher, r -> r.get(2)).match(givenRecords, expectedData)).isFalse();
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
