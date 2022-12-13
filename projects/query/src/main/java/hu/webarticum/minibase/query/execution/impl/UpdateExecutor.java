package hu.webarticum.minibase.query.execution.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.UpdateQuery;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.TableQueryUtil;
import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TablePatch;
import hu.webarticum.minibase.storage.api.TablePatch.TablePatchBuilder;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.impl.result.StoredResult;
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
        Map<String, Object> queryWhere = updateQuery.where();
        
        TableQueryUtil.checkFields(table, queryUpdates.keySet());
        TableQueryUtil.checkFields(table, queryWhere.keySet());

        Map<String, Object> convertedQueryUpdates =
                TableQueryUtil.convertColumnValues(table, queryUpdates, state, true);
        Map<String, Object> convertedQueryWhere = TableQueryUtil.convertColumnValues(table, queryWhere, state, false);

        List<LargeInteger> rowIndexes = TableQueryUtil.filterRowsToList(
                table, convertedQueryWhere, Collections.emptyList(), null);
        
        ImmutableMap<Integer, Object> updates =
                TableQueryUtil.toByColumnPoisitionedImmutableMap(table, convertedQueryUpdates);
        TablePatchBuilder patchBuilder = TablePatch.builder();
        rowIndexes.forEach(i -> patchBuilder.update(i, updates));
        TablePatch patch = patchBuilder.build();

        table.applyPatch(patch);

        Optional<Column> autoIncrementedColumnHolder = TableQueryUtil.getAutoIncrementedColumn(table);
        if (autoIncrementedColumnHolder.isPresent()) {
            String columnName = autoIncrementedColumnHolder.get().name();
            if (convertedQueryUpdates.containsKey(columnName)) {
                Object value = convertedQueryUpdates.get(columnName);
                LargeInteger largeIntegerValue = TableQueryUtil.convert(value, LargeInteger.class);
                table.sequence().ensureGreaterThan(largeIntegerValue);
            }
        }
        
        return new StoredResult();
    }
    
}
