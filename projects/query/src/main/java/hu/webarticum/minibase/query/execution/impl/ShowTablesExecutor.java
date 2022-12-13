package hu.webarticum.minibase.query.execution.impl;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.ShowTablesQuery;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.LikeMatcher;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.api.MiniValue;
import hu.webarticum.miniconnect.impl.result.StoredColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredResult;
import hu.webarticum.miniconnect.impl.result.StoredResultSetData;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.translator.ValueTranslator;
import hu.webarticum.miniconnect.record.type.StandardValueType;

public class ShowTablesExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(storageAccess, state, (ShowTablesQuery) query);
    }
    
    private MiniResult executeInternal(
            StorageAccess storageAccess, SessionState state, ShowTablesQuery showTablesQuery) {
        String schemaName = showTablesQuery.from();
        
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
        
        ImmutableList<String> tableNames = schema.tables().names();
        String like = showTablesQuery.like();
        if (like != null) {
            tableNames = tableNames.filter(tableName -> match(like, tableName));
        }
        ValueTranslator stringTranslator = StandardValueType.STRING.defaultTranslator();
        MiniColumnHeader columnHeader = new StoredColumnHeader(
                "Tables_in_" + schemaName,
                false,
                stringTranslator.definition());
        ImmutableList<MiniColumnHeader> columnHeaders = ImmutableList.of(columnHeader);
        ImmutableList<ImmutableList<MiniValue>> data = tableNames.map(
                tableName -> ImmutableList.of(stringTranslator.encodeFully(tableName)));
        return new StoredResult(new StoredResultSetData(columnHeaders, data));
    }

    private boolean match(String like, String tableName) {
        return new LikeMatcher(like).test(tableName);
    }
    
}
