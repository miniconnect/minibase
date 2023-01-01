package hu.webarticum.minibase.query.execution.impl;

import hu.webarticum.minibase.query.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.query.expression.SpecialValueExpression;
import hu.webarticum.minibase.query.expression.SpecialValueParameter;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.ShowSpecialQuery;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.ResultUtil;
import hu.webarticum.minibase.query.util.TableQueryUtil;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniResult;

public class ShowSpecialExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(state, (ShowSpecialQuery) query);
    }
    
    private MiniResult executeInternal(SessionState state, ShowSpecialQuery showSpecialQuery) {
        SpecialValueExpression expression = showSpecialQuery.specialValueExpression();
        SpecialValueParameter parameter = expression.specialValueParameter();
        
        String alias = showSpecialQuery.alias();
        if (alias == null) {
            alias = expression.automaticName();
        }
        
        Object value = TableQueryUtil.getSpecialValue(parameter, state);
        
        return ResultUtil.createSingleValueResult(alias, value);
    }

}
