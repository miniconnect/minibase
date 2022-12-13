package hu.webarticum.minibase.query.execution.impl;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.UseQuery;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.impl.result.StoredResult;

public class UseExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(storageAccess, state, (UseQuery) query);
    }
    
    private MiniResult executeInternal(StorageAccess storageAccess, SessionState state, UseQuery useQuery) {
        String schemaName = useQuery.schema();
        if (!storageAccess.schemas().contains(schemaName)) {
            throw PredefinedError.SCHEMA_NOT_FOUND.toException(schemaName);
        }
        
        state.setCurrentSchema(schemaName);
        return new StoredResult();
    }

}
