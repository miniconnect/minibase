package hu.webarticum.minibase.query.execution.impl;

import java.util.Map;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.SelectCountQuery;
import hu.webarticum.minibase.query.query.SelectQuery.WhereItem;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.ResultUtil;
import hu.webarticum.minibase.query.util.TableQueryUtil;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class SelectCountExecutor implements ThrowingQueryExecutor {

    private static final String COLUMN_NAME = "COUNT";
    
    
    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(storageAccess, state, (SelectCountQuery) query);
    }
    
    private MiniResult executeInternal(
            StorageAccess storageAccess, SessionState state, SelectCountQuery selectCountQuery) {
        String schemaName = selectCountQuery.schemaName();
        String tableName = selectCountQuery.tableName();
        
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
        
        ImmutableList<WhereItem> queryWhere = selectCountQuery.where();
        if (queryWhere.isEmpty()) {
            return ResultUtil.createSingleValueResult(COLUMN_NAME, table.size());
        }
        
        Map<String, Object> convertedQueryWhere = TableQueryUtil.mergeAndConvertFilters(queryWhere, table, state);
        LargeInteger count = TableQueryUtil.countRows(table, convertedQueryWhere);
        return ResultUtil.createSingleValueResult(COLUMN_NAME, count);
    }
    
}
