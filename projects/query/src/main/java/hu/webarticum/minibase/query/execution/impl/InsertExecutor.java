package hu.webarticum.minibase.query.execution.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.query.InsertQuery;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.ResultUtil;
import hu.webarticum.minibase.query.util.TableQueryUtil;
import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TablePatch;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.impl.result.StoredResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class InsertExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(storageAccess, state, (InsertQuery) query);
    }

    private MiniResult executeInternal(StorageAccess storageAccess, SessionState state, InsertQuery insertQuery) {
        String schemaName = insertQuery.schemaName();
        String tableName = insertQuery.tableName();
        
        if (schemaName == null) {
            schemaName = state.getCurrentSchema();
        }
        if (schemaName == null) {
            throw PredefinedError.SCHEMA_NOT_SELECTED.toException();
        }
        
        Schema schema = storageAccess.schemas().get(schemaName);
        if (schema == null) {
            throw PredefinedError.SCHEMA_NOT_FOUND.toException(schemaName);
        }
        
        Table table = schema.tables().get(tableName);
        if (table == null) {
            throw PredefinedError.TABLE_NOT_FOUND.toException(tableName);
        }
        if (!table.isWritable()) {
            throw PredefinedError.TABLE_READONLY.toException(tableName);
        }

        ImmutableList<String> givenInsertFields = insertQuery.fields();
        ImmutableList<String> insertFields = givenInsertFields != null ? givenInsertFields : table.columns().names();
        ImmutableList<Object> insertValues = insertQuery.values();
        
        Map<String, Object> insertValueMap =
                insertFields.assign((v, i) -> ResultUtil.resolveValue(insertValues.get(i), state)).toHashMap();
        TableQueryUtil.checkFields(table, insertValueMap.keySet());

        LargeInteger lastInsertId = includeDefaultValues(insertValueMap, table, state);
        
        Map<String, Object> convertedInsertValues =
                TableQueryUtil.convertColumnNewValues(table, insertValueMap, state, true);

        int columnCount = table.columns().names().size();
        ImmutableMap<Integer, Object> values =
                TableQueryUtil.toByColumnPoisitionedImmutableMap(table, convertedInsertValues);
        List<Object> rowDataBuilder = new ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            if (!values.containsKey(i)) {
                String columnName = table.columns().names().get(i);
                throw PredefinedError.COLUMN_MISSING.toException(columnName);
            }
            rowDataBuilder.add(values.get(i));
        }
        ImmutableList<Object> rowData = ImmutableList.fromCollection(rowDataBuilder);
        
        TablePatch.TablePatchBuilder patchBuilder = TablePatch.builder();
        patchBuilder.insert(rowData);
        
        if (insertQuery.replace()) {
            Set<LargeInteger> conflictingRowIndices = collectConflictingRowIndices(convertedInsertValues, table);
            for (LargeInteger conflictingRowIndex : conflictingRowIndices) {
                patchBuilder.delete(conflictingRowIndex);
            }
        }
        
        TablePatch patch = patchBuilder.build();
        
        table.applyPatch(patch);

        Column autoIncrementedColumn = TableQueryUtil.getAutoIncrementedColumn(table);
        if (autoIncrementedColumn != null) {
            String columName = autoIncrementedColumn.name();
            Object convertedAutoIncrementColumnValue = convertedInsertValues.get(columName);
            LargeInteger largeIntegerValue =
                    TableQueryUtil.convert(convertedAutoIncrementColumnValue, LargeInteger.class);
            table.sequence().ensureGreaterThan(largeIntegerValue);
        }

        if (lastInsertId != null) {
            state.setLastInsertId(lastInsertId);
        }
        
        return new StoredResult();
    }
    
    private LargeInteger includeDefaultValues(Map<String, Object> insertValueMap, Table table, SessionState state) {
        LargeInteger lastInsertId = null;
        for (Column column : table.columns().resources()) {
            String columnName = column.name();
            ColumnDefinition definition = column.definition();
            if (
                    !insertValueMap.containsKey(columnName) ||
                    (!definition.isNullable() && insertValueMap.get(columnName) == null)) {
                Object defaultValue = getDefaultValue(table, column);
                insertValueMap.put(columnName, defaultValue);
                if (lastInsertId == null && definition.isAutoIncremented()) {
                    lastInsertId = TableQueryUtil.convert(defaultValue, LargeInteger.class);
                }
            }
        }
        
        return lastInsertId;
    }
    
    private Object getDefaultValue(Table table, Column column) {
        ColumnDefinition definition = column.definition();
        if (definition.isAutoIncremented()) {
            return generateAutoIncrementedValue(table, column.name());
        }
        
        Object defaultValue = definition.defaultValue();
        if (defaultValue == null && !definition.isNullable()) {
            throw PredefinedError.COLUMN_VALUE_NULL.toException(column.name());
        }
        
        return defaultValue;
    }

    private LargeInteger generateAutoIncrementedValue(Table table, String columnName) {
        LargeInteger autoIncrementedValue = table.sequence().get();
        while (TableQueryUtil.isColumnContainingValue(table, columnName, autoIncrementedValue)) {
            autoIncrementedValue = autoIncrementedValue.increment();
        }
        return autoIncrementedValue;
    }

    private Set<LargeInteger> collectConflictingRowIndices(Map<String, Object> convertedInsertValues, Table table) {
        Set<LargeInteger> result = new HashSet<>();
        NamedResourceStore<Column> columns = table.columns();
        for (Map.Entry<String, Object> entry : convertedInsertValues.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }

            String columnName = entry.getKey();
            ColumnDefinition definition = columns.get(columnName).definition();
            if (!definition.isUnique()) {
                continue;
            }
            
            result.addAll(TableQueryUtil.findAllNonNull(table, columnName, value));
        }
        return result;
    }

}
