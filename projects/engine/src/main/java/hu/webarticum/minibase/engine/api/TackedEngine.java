package hu.webarticum.minibase.engine.api;

import hu.webarticum.minibase.query.execution.QueryExecutor;
import hu.webarticum.minibase.query.parser.SqlParser;
import hu.webarticum.minibase.storage.api.StorageAccess;

public interface TackedEngine extends Engine {

    public SqlParser sqlParser();

    public QueryExecutor queryExecutor();

    public StorageAccess storageAccess();

}