package hu.webarticum.minibase.engine.api;

import hu.webarticum.minibase.query.execution.QueryExecutor;
import hu.webarticum.minibase.query.parser.SqlParser;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.lang.CheckableCloseable;

public interface EngineSession extends CheckableCloseable {
    
    public Engine engine();

    public SessionState state();

    public SqlParser sqlParser();

    public QueryExecutor queryExecutor();

    public StorageAccess storageAccess();
    
}
