package hu.webarticum.minibase.query.execution.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.query.DeleteQuery;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.SelectQuery.WhereItem;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.TableQueryUtil;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TablePatch;
import hu.webarticum.minibase.storage.api.TablePatch.TablePatchBuilder;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.impl.result.StoredResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class DeleteExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(storageAccess, state, (DeleteQuery) query);
    }
    
    private MiniResult executeInternal(StorageAccess storageAccess, SessionState state, DeleteQuery deleteQuery) {
        String schemaName = deleteQuery.schemaName();
        String tableName = deleteQuery.tableName();
        
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

        ImmutableList<WhereItem> queryWhere = deleteQuery.where();
        Map<String, Object> convertedQueryWhere = TableQueryUtil.mergeAndConvertFilters(queryWhere, table, state);
        TableQueryUtil.checkFields(table, convertedQueryWhere.keySet());
        
        List<LargeInteger> rowIndexes = TableQueryUtil.filterRowsToList(
                table, convertedQueryWhere, Collections.emptyList(), null);
        
        TablePatchBuilder patchBuilder = TablePatch.builder();
        rowIndexes.forEach(patchBuilder::delete);
        TablePatch patch = patchBuilder.build();

        table.applyPatch(patch);
        
        return new StoredResult();
    }

}
