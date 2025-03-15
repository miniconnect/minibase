package hu.webarticum.minibase.execution.impl;

import hu.webarticum.minibase.common.error.PredefinedError;
import hu.webarticum.minibase.execution.QueryExecutor;
import hu.webarticum.minibase.execution.impl.select.SelectExecutor;
import hu.webarticum.minibase.query.query.DeleteQuery;
import hu.webarticum.minibase.query.query.InsertQuery;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.SelectCountQuery;
import hu.webarticum.minibase.query.query.SelectQuery;
import hu.webarticum.minibase.query.query.ShowSpecialQuery;
import hu.webarticum.minibase.query.query.SetVariableQuery;
import hu.webarticum.minibase.query.query.ShowSchemasQuery;
import hu.webarticum.minibase.query.query.ShowTablesQuery;
import hu.webarticum.minibase.query.query.StandaloneSelectQuery;
import hu.webarticum.minibase.query.query.UpdateQuery;
import hu.webarticum.minibase.query.query.UseQuery;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniResult;

public class IntegratedQueryExecutor implements QueryExecutor {

    @Override
    public MiniResult execute(StorageAccess storageAccess, SessionState state, Query query) {
        if (query instanceof SelectQuery) {
            return new SelectExecutor().execute(storageAccess, state, query);
        } else if (query instanceof SelectCountQuery) {
            return new SelectCountExecutor().execute(storageAccess, state, query);
        } else if (query instanceof StandaloneSelectQuery) {
            return new StandaloneSelectExecutor().execute(storageAccess, state, query);
        } else if (query instanceof ShowSpecialQuery) {
            return new ShowSpecialExecutor().execute(storageAccess, state, query);
        } else if (query instanceof InsertQuery) {
            return new InsertExecutor().execute(storageAccess, state, query);
        } else if (query instanceof UpdateQuery) {
            return new UpdateExecutor().execute(storageAccess, state, query);
        } else if (query instanceof DeleteQuery) {
            return new DeleteExecutor().execute(storageAccess, state, query);
        } else if (query instanceof ShowSchemasQuery) {
            return new ShowSchemasExecutor().execute(storageAccess, state, query);
        } else if (query instanceof ShowTablesQuery) {
            return new ShowTablesExecutor().execute(storageAccess, state, query);
        } else if (query instanceof UseQuery) {
            return new UseExecutor().execute(storageAccess, state, query);
        } else if (query instanceof SetVariableQuery) {
            return new SetVariableExecutor().execute(storageAccess, state, query);
        } else {
            return PredefinedError.QUERY_TYPE_NOT_FOUND.toResult(query.getClass().getName());
        }
    }

}
