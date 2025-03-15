package hu.webarticum.minibase.execution.impl;

import java.util.Optional;

import hu.webarticum.minibase.execution.ThrowingQueryExecutor;
import hu.webarticum.minibase.execution.util.ResultUtil;
import hu.webarticum.minibase.execution.util.TableQueryUtil;
import hu.webarticum.minibase.query.expression.ColumnParameter;
import hu.webarticum.minibase.query.expression.Expression;
import hu.webarticum.minibase.query.expression.Parameter;
import hu.webarticum.minibase.query.expression.SpecialValueParameter;
import hu.webarticum.minibase.query.expression.VariableParameter;
import hu.webarticum.minibase.query.query.Query;
import hu.webarticum.minibase.query.query.StandaloneSelectQuery;
import hu.webarticum.minibase.query.state.SessionState;
import hu.webarticum.minibase.query.util.NumberParser;
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
        ImmutableList<Class<?>> types = aliases
                .map((i, a) -> findType(i, expressionMatrix, state));
        ImmutableList<Boolean> nullabilities = aliases
                .map((i, a) -> findNullability(i, expressionMatrix, state));
        ImmutableList<MiniColumnHeader> columnHeaders = aliases
                .map((i, a) -> createColumnHeader(a, types.get(i), nullabilities.get(i)));
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
    
    private Class<?> findType(
            int columnIndex, ImmutableList<ImmutableList<Expression>> expressionMatrix, SessionState state) {
        Class<?> bestFoundType = Void.class;
        for (ImmutableList<Expression> expressionRow : expressionMatrix) {
            Expression expression = expressionRow.get(columnIndex);
            Class<?> type = extractType(expression, state);
            bestFoundType = mergeType(bestFoundType, type);
            if (type == Object.class || type == String.class) {
                return String.class;
            }
        }
        return bestFoundType;
    }
    
    private Class<?> mergeType(Class<?> bestFoundType, Class<?> type) {
        if (type == bestFoundType || bestFoundType == Void.class) {
            return type;
        } else if (type == Void.class) {
            return bestFoundType;
        } else if (Number.class.isAssignableFrom(type) && Number.class.isAssignableFrom(bestFoundType)) {
            return NumberParser.commonNumericTypeOf(bestFoundType, type);
        } else if (CharSequence.class.isAssignableFrom(type) && CharSequence.class.isAssignableFrom(bestFoundType)) {
            return String.class;
        } else {
            return Object.class;
        }
    }

    private Class<?> extractType(Expression expression, SessionState state) {
        Optional<Class<?>> fixedType = expression.type();
        if (fixedType.isPresent()) {
            return fixedType.get();
        }
        
        ImmutableMap<Parameter, Class<?>> types = expression.parameters().assign(p -> getParameterType(p, state));
        return expression.type(types);
    }

    private Class<?> getParameterType(Parameter parameter, SessionState state) {
        Object value = substitute(parameter, state);
        if (value == null) {
            return Void.class;
        } else {
            return value.getClass();
        }
    }

    private boolean findNullability(
            int columnIndex, ImmutableList<ImmutableList<Expression>> expressionMatrix, SessionState state) {
        for (ImmutableList<Expression> expressionRow : expressionMatrix) {
            Expression expression = expressionRow.get(columnIndex);
            boolean isNullable = extractNullability(expression, state);
            if (isNullable) {
                return true;
            }
        }
        return false;
    }
    
    private boolean extractNullability(Expression expression, SessionState state) {
        if (!expression.isNullable()) {
            return false;
        }
        
        ImmutableMap<Parameter, Boolean> nullabilities =
                expression.parameters().assign(p -> isParameterNullable(p, state));
        return expression.isNullable(nullabilities);
    }

    private boolean isParameterNullable(Parameter parameter, SessionState state) {
        Object value = substitute(parameter, state);
        return value == null;
    }
    
    private MiniColumnHeader createColumnHeader(String alias, Class<?> type, boolean nullable) {
        ValueTranslator translator = ResultUtil.createValueTranslatorFor(type);
        MiniValueDefinition columnDefinition = translator.definition();
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
