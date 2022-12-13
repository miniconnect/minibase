package hu.webarticum.minibase.query.execution;

import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniResult;

public interface QueryExecutor {

    public MiniResult execute(StorageAccess storageAccess, SessionState state, Query query);
    
}
