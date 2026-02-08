package hu.webarticum.minibase.test.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import hu.webarticum.miniconnect.impl.result.StoredColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredResultSet;
import hu.webarticum.miniconnect.impl.result.StoredResultSetData;
import hu.webarticum.miniconnect.impl.result.StoredValue;
import hu.webarticum.miniconnect.impl.result.StoredValueDefinition;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.record.ResultTable;
import hu.webarticum.miniconnect.record.converter.Converter;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;
import hu.webarticum.miniconnect.record.translator.ValueTranslator;
import hu.webarticum.miniconnect.record.type.StandardValueType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultTableMatcherTest {

    private TableHeaderMatcher tableHeaderMatcher;

    private Converter converter;

    private ImmutableList<ValueTranslator> valueTranslators;

    private DataMatcher dataMatcher;

    @Test
    void testMatchBothPass() {
        ImmutableList<StoredColumnHeader> givenColumnHeaders = ImmutableList.of(
                buildColumnHeader("id", false, StandardValueType.INT),
                buildColumnHeader("label", false, StandardValueType.STRING));
        ImmutableList<ImmutableList<Object>> givenData = ImmutableList.of(
                ImmutableList.of(1, "lorem"),
                ImmutableList.of(2, "ipsum"));
        ResultTable givenTable = buildTable(givenColumnHeaders, givenData);
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem"),
                ImmutableList.of(2, "ipsum"));
        assertThat(DefaultTableMatcher.of(tableHeaderMatcher, dataMatcher).match(givenTable, expectedData)).isTrue();
    }

    @Test
    void testMatchHeaderNotPass() {
        ImmutableList<StoredColumnHeader> givenColumnHeaders = ImmutableList.of(
                buildColumnHeader("id", false, StandardValueType.INT),
                buildColumnHeader("labelxxx", false, StandardValueType.STRING));
        ImmutableList<ImmutableList<Object>> givenData = ImmutableList.of(
                ImmutableList.of(1, "lorem"),
                ImmutableList.of(2, "ipsum"));
        ResultTable givenTable = buildTable(givenColumnHeaders, givenData);
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem"),
                ImmutableList.of(2, "ipsum"));
        assertThat(DefaultTableMatcher.of(tableHeaderMatcher, dataMatcher).match(givenTable, expectedData)).isFalse();
    }

    @Test
    void testMatchDataNotPass() {
        ImmutableList<StoredColumnHeader> givenColumnHeaders = ImmutableList.of(
                buildColumnHeader("id", false, StandardValueType.INT),
                buildColumnHeader("label", false, StandardValueType.STRING));
        ImmutableList<ImmutableList<Object>> givenData = ImmutableList.of(
                ImmutableList.of(1, "lorem"),
                ImmutableList.of(999, "ipsum"));
        ResultTable givenTable = buildTable(givenColumnHeaders, givenData);
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem"),
                ImmutableList.of(2, "ipsum"));
        assertThat(DefaultTableMatcher.of(tableHeaderMatcher, dataMatcher).match(givenTable, expectedData)).isFalse();
    }

    @Test
    void testMatchNeitherPass() {
        ImmutableList<StoredColumnHeader> givenColumnHeaders = ImmutableList.of(
                buildColumnHeader("idxxx", false, StandardValueType.INT),
                buildColumnHeader("label", false, StandardValueType.STRING));
        ImmutableList<ImmutableList<Object>> givenData = ImmutableList.of(
                ImmutableList.of(1, "xxxxxxxxx"),
                ImmutableList.of(2, "ipsum"));
        ResultTable givenTable = buildTable(givenColumnHeaders, givenData);
        ImmutableList<ImmutableList<Object>> expectedData = ImmutableList.of(
                ImmutableList.of(1, "lorem"),
                ImmutableList.of(2, "ipsum"));
        assertThat(DefaultTableMatcher.of(tableHeaderMatcher, dataMatcher).match(givenTable, expectedData)).isFalse();
    }

    private ResultTable buildTable(ImmutableList<StoredColumnHeader> columnHeaders, ImmutableList<ImmutableList<Object>> data) {
        ImmutableList<ImmutableList<StoredValue>> wrappedData =
                data.map(r -> r.map((i, v) -> StoredValue.from(valueTranslators.get(i).encodeFully(v))));
        return new ResultTable(
                StoredResultSet.of(StoredResultSetData.of(columnHeaders, wrappedData)), valueTranslators, converter);
    }

    @BeforeEach
    void prepare() {
        tableHeaderMatcher = DefaultTableHeaderMatcher.of(ImmutableList.of(
                ColumnHeaderNameMatcher.of("id"),
                ColumnHeaderNameMatcher.of("label")));
        converter = new DefaultConverter();
        valueTranslators = ImmutableList.of(
                StandardValueType.INT.translatorFor(ImmutableMap.empty()),
                StandardValueType.STRING.translatorFor(ImmutableMap.empty()));
        FieldMatcher fieldMatcher = EqualityFieldMatcher.instance();
        RecordMatcher recordMatcher = DefaultRecordMatcher.of(ImmutableList.of(fieldMatcher, fieldMatcher));
        dataMatcher = OrderedDataMatcher.of(recordMatcher);
    }

    private StoredColumnHeader buildColumnHeader(String name, boolean isNullable, StandardValueType type) {
        return StoredColumnHeader.of(name, isNullable, StoredValueDefinition.of(type.name()));
    }

}
