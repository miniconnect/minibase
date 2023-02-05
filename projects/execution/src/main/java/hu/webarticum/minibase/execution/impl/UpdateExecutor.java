package hu.webarticum.minibase.execution.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.execution.util.TableQueryUtil;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.UpdateQuery;
import hu.webarticum.minibase.query.query.SelectQuery.WhereItem;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TablePatch;
import hu.webarticum.minibase.storage.api.TablePatch.TablePatchBuilder;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.impl.result.StoredResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class UpdateExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(storageAccess, state, (UpdateQuery) query);
    }
    
    private MiniResult executeInternal(StorageAccess storageAccess, SessionState state, UpdateQuery updateQuery) {
        String schemaName = updateQuery.schemaName();
        String tableName = updateQuery.tableName();
        
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

        Map<String, Object> queryUpdates = updateQuery.values();
        TableQueryUtil.checkFields(table, queryUpdates.keySet());
        
        ImmutableList<WhereItem> queryWhere = updateQuery.where();
        Map<String, Object> convertedQueryUpdates =
                TableQueryUtil.convertColumnNewValues(table, queryUpdates, state, true);
        Map<String, Object> convertedQueryWhere = TableQueryUtil.mergeAndConvertFilters(queryWhere, table, state);
        TableQueryUtil.checkFields(table, convertedQueryWhere.keySet());

        List<LargeInteger> rowIndexes = TableQueryUtil.filterRowsToList(
                table, convertedQueryWhere, Collections.emptyList(), null);
        
        ImmutableMap<Integer, Object> updates =
                TableQueryUtil.toByColumnPoisitionedImmutableMap(table, convertedQueryUpdates);
        TablePatchBuilder patchBuilder = TablePatch.builder();
        rowIndexes.forEach(i -> patchBuilder.update(i, updates));
        TablePatch patch = patchBuilder.build();

        table.applyPatch(patch);

        Column autoIncrementedColumn = TableQueryUtil.getAutoIncrementedColumn(table);
        if (autoIncrementedColumn != null) {
            String columnName = autoIncrementedColumn.name();
            if (convertedQueryUpdates.containsKey(columnName)) {
                Object value = convertedQueryUpdates.get(columnName);
                LargeInteger largeIntegerValue = TableQueryUtil.convert(value, LargeInteger.class);
                table.sequence().ensureGreaterThan(largeIntegerValue);
            }
        }
        
        return new StoredResult();
    }
    
}
