package hu.webarticum.minibase.test.runner;

import java.util.Comparator;
import java.util.Optional;

import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.impl.diff.DiffTable;
import hu.webarticum.minibase.storage.impl.simple.SimpleColumnDefinition;
import hu.webarticum.minibase.storage.impl.simple.SimpleTable;
import hu.webarticum.minibase.test.model.dataset.DatasetColumnDescription;
import hu.webarticum.minibase.test.model.dataset.DatasetTableDescription;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.converter.Converter;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;

public class TableRenderer {

    private final Converter converter = new DefaultConverter();

    public Table renderTable(DatasetTableDescription tableDescription) {
        Table table = renderBaseTable(tableDescription);
        if (tableDescription.addDiffLayer()) {
            table = new DiffTable(table);
        }
        return table;
    }

    private Table renderBaseTable(DatasetTableDescription tableDescription) {
        SimpleTable.SimpleTableBuilder builder = SimpleTable.builder();
        builder.name(tableDescription.name());
        ImmutableList<DatasetColumnDescription> columns = tableDescription.columns();
        for (DatasetColumnDescription columnDescription : columns) {
            builder.addColumn(columnDescription.name(), buildColumnDefinition(columnDescription));
        }
        for (ImmutableList<Object> rawRow : tableDescription.data()) {
            builder.addRow(rawRow.map((i, v) -> convert(v, columns.get(i).type())));
        }
        for (ImmutableList<String> columnNames : tableDescription.indexes()) {
            builder.addIndex("idx_" + String.join("_", columnNames), columnNames);
        }
        return builder.build();
    }

    private ColumnDefinition buildColumnDefinition(DatasetColumnDescription columnDescription) {
        Class<?> type = columnDescription.type();
        return new SimpleColumnDefinition(
                type,
                columnDescription.nullable(),
                columnDescription.unique(),
                columnDescription.autoIncremented(),
                columnDescription.enumValues().orElse(null),
                buildColumnComparator(columnDescription),
                convert(columnDescription.defaultValue().orElse(null), type));
    }

    private Comparator<?> buildColumnComparator(DatasetColumnDescription columnDescription) {
        Optional<ImmutableList<Object>> enumValuesOptional = columnDescription.enumValues();
        if (!enumValuesOptional.isPresent()) {
            return Comparator.naturalOrder();
        }
        ImmutableList<Object> enumValues = enumValuesOptional.get();
        return (a, b) -> Integer.compare(enumValues.indexOf(a), enumValues.indexOf(b));
    }

    private Object convert(Object value, Class<?> targetClazz) {
        if (value == null) {
            return null;
        }
        return converter.convert(value, targetClazz);
    }

}
