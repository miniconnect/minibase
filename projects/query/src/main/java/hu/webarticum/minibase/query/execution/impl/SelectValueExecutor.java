package hu.webarticum.minibase.query.execution.impl;

import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.SelectValueQuery;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.ResultUtil;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniResult;

public class SelectValueExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(state, (SelectValueQuery) query);
    }
    
    private MiniResult executeInternal(SessionState state, SelectValueQuery selectValueQuery) {
        Object value = selectValueQuery.value();
        Object resolvedValue = ResultUtil.resolveValue(value, state);
        String alias = selectValueQuery.alias();
        String columnName = alias != null ? alias : ResultUtil.getAutoFieldNameFor(value);
        return ResultUtil.createSingleValueResult(columnName, resolvedValue);
    }

}
