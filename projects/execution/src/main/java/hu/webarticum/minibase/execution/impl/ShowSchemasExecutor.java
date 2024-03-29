package hu.webarticum.minibase.execution.impl;

import hu.webarticum.minibase.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.execution.util.LikeMatcher;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.ShowSchemasQuery;
import hu.webarticum.minibase.query.state.SessionState;
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

public class ShowSchemasExecutor implements ThrowingQueryExecutor {

    private static final String COLUMN_NAME = "Schemas";
    

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(storageAccess, state, (ShowSchemasQuery) query);
    }
    
    private MiniResult executeInternal(
            StorageAccess storageAccess, SessionState state, ShowSchemasQuery showSchemasQuery) {
        ImmutableList<String> schemaNames = storageAccess.schemas().names();
        String like = showSchemasQuery.like();
        if (like != null) {
            schemaNames = schemaNames.filter(schemaName -> match(like, schemaName));
        }
        ValueTranslator stringTranslator = StandardValueType.STRING.defaultTranslator();
        MiniColumnHeader columnHeader = new StoredColumnHeader(
                COLUMN_NAME,
                false,
                stringTranslator.definition());
        ImmutableList<MiniColumnHeader> columnHeaders = ImmutableList.of(columnHeader);
        ImmutableList<ImmutableList<MiniValue>> data = schemaNames.map(
                tableName -> ImmutableList.of(stringTranslator.encodeFully(tableName)));
        return new StoredResult(new StoredResultSetData(columnHeaders, data));
    }

    private boolean match(String like, String tableName) {
        return new LikeMatcher(like).test(tableName);
    }
    
}
