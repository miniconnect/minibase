package hu.webarticum.minibase.storage.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TablePatch;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class TablePatchUtil {

    private TablePatchUtil() {
        // utility class
    }
    

    public static void checkIndividualValues(Table table, TablePatch patch) {
        NamedResourceStore<Column> columnStore = table.columns();
        
        ImmutableList<ColumnDefinition> columnDefinitions = columnStore.resources().map(Column::definition);

        Set<Integer> nonNullableColumnIndices = new HashSet<>();
        Map<Integer, Set<Object>> enumColumnValues = new HashMap<>();
        int columnCount = columnDefinitions.size();
        for (int i = 0; i < columnCount; i++) {
            ColumnDefinition columnDefinition = columnDefinitions.get(i);
            if (!columnDefinition.isNullable()) {
                nonNullableColumnIndices.add(i);
            }
            Optional<ImmutableList<Object>> enumValuesOptional = columnDefinition.enumValues();
            if (enumValuesOptional.isPresent()) {
                enumColumnValues.put(i, toTreeSet(enumValuesOptional.get(), columnDefinition.comparator()));
            }
        }
        if (nonNullableColumnIndices.isEmpty() && enumColumnValues.isEmpty()) {
            return;
        }

        ImmutableList<String> columnNames = columnStore.names();
        for (ImmutableMap<Integer, Object> rowUpdates : patch.updates().values()) {
            for (Map.Entry<Integer, Object> updateEntry : rowUpdates.entrySet()) {
                Integer columnIndex = updateEntry.getKey();
                Object value = updateEntry.getValue();
                checkValue(value, columnIndex, columnNames, nonNullableColumnIndices, enumColumnValues);
            }
        }

        for (ImmutableList<Object> insertedRow : patch.insertedRows()) {
            int size = insertedRow.size();
            for (int i = 0; i < size; i++) {
                Object value = insertedRow.get(i);
                checkValue(value, i, columnNames, nonNullableColumnIndices, enumColumnValues);
            }
        }
    }
    
    private static Set<Object> toTreeSet(ImmutableList<Object> values, Comparator<?> comparator) {
        @SuppressWarnings("unchecked")
        Comparator<Object> objectComparator = (Comparator<Object>) comparator;
        TreeSet<Object> result = new TreeSet<>(objectComparator);
        result.addAll(values.asList());
        return result;
    }
    
    private static void checkValue(
            Object value,
            Integer columnIndex,
            ImmutableList<String> columnNames,
            Set<Integer> nonNullableColumnIndices,
            Map<Integer, Set<Object>> enumColumnValues) {
        if (nonNullableColumnIndices.contains(columnIndex) && value == null) {
            String columnName = columnNames.get(columnIndex);
            throw PredefinedError.COLUMN_VALUE_NULL.toException(columnName);
        }
        Set<Object> enumValues = enumColumnValues.get(columnIndex);
        if (enumValues != null && !enumValues.contains(value)) {
            String columnName = columnNames.get(columnIndex);
            throw PredefinedError.COLUMN_VALUE_NOT_IN_ENUM.toException(columnName, enumValues);
        }
    }
    
}
