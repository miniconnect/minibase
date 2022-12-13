package hu.webarticum.minibase.query.execution.impl;

import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.SetVariableQuery;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.ResultUtil;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.impl.result.StoredResult;

public class SetVariableExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(state, (SetVariableQuery) query);
    }
    
    private MiniResult executeInternal(SessionState state, SetVariableQuery setVariableQuery) {
        String variableName = setVariableQuery.name();
        Object value = setVariableQuery.value();
        Object resolvedValue = ResultUtil.resolveValue(value, state);
        
        state.setUserVariable(variableName, resolvedValue);
        
        return new StoredResult();
    }

}
