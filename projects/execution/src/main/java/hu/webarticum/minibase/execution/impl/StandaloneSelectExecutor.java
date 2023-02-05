package hu.webarticum.minibase.execution.impl;

import hu.webarticum.minibase.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.execution.util.ResultUtil;
import hu.webarticum.minibase.execution.util.TableQueryUtil;
import hu.webarticum.minibase.query.expression.ColumnParameter;
import hu.webarticum.minibase.query.expression.Expression;
import hu.webarticum.minibase.query.expression.FixedTypeExpression;
import hu.webarticum.minibase.query.expression.Parameter;
import hu.webarticum.minibase.query.expression.SpecialValueParameter;
import hu.webarticum.minibase.query.expression.VariableParameter;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.StandaloneSelectQuery;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.api.MiniValue;
import hu.webarticum.miniconnect.api.MiniValueDefinition;
import hu.webarticum.miniconnect.impl.result.StoredColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredResult;
import hu.webarticum.miniconnect.impl.result.StoredResultSetData;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.record.translator.ValueTranslator;

public class StandaloneSelectExecutor implements ThrowingQueryExecutor {

    @Override
    public MiniResult executeThrowing(StorageAccess storageAccess, SessionState state, Query query) {
        return executeInternal(state, (StandaloneSelectQuery) query);
    }
    
    private MiniResult executeInternal(SessionState state, StandaloneSelectQuery standaloneSelectQuery) {
        ImmutableList<ImmutableList<Expression>> expressionMatrix = standaloneSelectQuery.expressionMatrix();
        ImmutableList<Expression> firstRow = expressionMatrix.get(0);
        ImmutableList<String> aliases = standaloneSelectQuery.aliases()
                .map((i, a) -> ensureAlias(a, firstRow.get(i)));
        ImmutableList<Class<?>> types = aliases.map((i, a) -> findType(i, standaloneSelectQuery.expressionMatrix()));
        ImmutableList<MiniColumnHeader> columnHeaders = aliases
                .map((i, a) -> createColumnHeader(a, types.get(i)));
        ImmutableList<ImmutableList<MiniValue>> values = standaloneSelectQuery.expressionMatrix()
                .map(r -> r.map((i, e) -> craftValue(e, types.get(i), state)));
        return new StoredResult(new StoredResultSetData(columnHeaders, values));
    }
    
    private String ensureAlias(String alias, Expression firstExpression) {
        if (alias != null) {
            return alias;
        } else {
            return firstExpression.automaticName();
        }
    }
    
    private Class<?> findType(int columnIndex, ImmutableList<ImmutableList<Expression>> expressionMatrix) {
        Class<?> bestFoundType = Void.class;
        for (ImmutableList<Expression> expressionRow : expressionMatrix) {
            Expression expression = expressionRow.get(columnIndex);
            Class<?> type = extractType(expression);
            if (type == Object.class || type == String.class) {
                return String.class;
            }
            if (bestFoundType == Void.class) {
                bestFoundType = type;
            } else if (type != Void.class && type != bestFoundType) {
                if (Number.class.isAssignableFrom(type) && Number.class.isAssignableFrom(bestFoundType)) {
                    bestFoundType = LargeInteger.class;
                } else {
                    return String.class;
                }
            }
        }
        return bestFoundType;
    }

    private Class<?> extractType(Expression expression) {
        if (expression instanceof FixedTypeExpression) {
            return ((FixedTypeExpression) expression).type();
        } else {
            return Object.class;
        }
    }

    private MiniColumnHeader createColumnHeader(String alias, Class<?> type) {
        ValueTranslator translator = ResultUtil.createValueTranslatorFor(type);
        MiniValueDefinition columnDefinition = translator.definition();
        boolean nullable = true;
        return new StoredColumnHeader(alias, nullable, columnDefinition);
    }
    
    private MiniValue craftValue(Expression expression, Class<?> type, SessionState state) {
        ImmutableMap<Parameter, Object> substitutions = expression.parameters().assign(p -> substitute(p, state));
        Object value = expression.evaluate(substitutions);
        Object convertedValue = TableQueryUtil.convert(value, type);
        return ResultUtil.createValueTranslatorFor(type).encodeFully(convertedValue);
    }
    
    private Object substitute(Parameter parameter, SessionState state) {
        if (parameter instanceof VariableParameter) {
            return state.getUserVariable(((VariableParameter) parameter).variableName());
        } else if (parameter instanceof SpecialValueParameter) {
            return TableQueryUtil.getSpecialValue((SpecialValueParameter) parameter, state);
        } else if (parameter instanceof ColumnParameter) {
            throw new IllegalArgumentException("No table for column expression");
        } else {
            throw new IllegalArgumentException("Unknown parameter type: " + parameter.getClass());
        }
    }
    
}
