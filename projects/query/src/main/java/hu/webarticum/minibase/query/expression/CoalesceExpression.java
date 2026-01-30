package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class CoalesceExpression implements Expression {

    private final ImmutableList<Expression> parameterExpressions;


    public CoalesceExpression(ImmutableList<Expression> parameterExpressions) {
        this.parameterExpressions = parameterExpressions;
    }


    public ImmutableList<Expression> parameterExpressions() {
        return parameterExpressions;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>();
        for (Expression parameterExpression : parameterExpressions) {
            subParameters.addAll(parameterExpression.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
    }

    @Override
    public Optional<Class<?>> type() {
        ImmutableList<Class<?>> parameterTypes = parameterExpressions.map(e -> e.type().orElse(null));
        return Optional.ofNullable(UnifyUtil.unifyTypes(parameterTypes));
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        ImmutableList<Class<?>> parameterTypes = parameterExpressions.map(e -> e.type(types));
        Class<?> result = UnifyUtil.unifyTypes(parameterTypes);
        return result == null ? String.class : result;
    }

    @Override
    public boolean isNullable() {
        for (Expression parameterExpression : parameterExpressions.reverseOrder()) {
            if (!parameterExpression.isNullable()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        for (Expression parameterExpression : parameterExpressions.reverseOrder()) {
            if (!parameterExpression.isNullable(nullabilities)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        for (Expression parameterExpression : parameterExpressions) {
            Object subValue = parameterExpression.evaluate(values);
            if (subValue != null) {
                return subValue;
            }
        }
        return null;
    }

    @Override
    public String automaticName() {
        StringBuilder resultBuilder = new StringBuilder("COALESCE(");
        boolean first = true;
        for (Expression parameterExpression : parameterExpressions) {
            if (first) {
                first = false;
            } else {
                resultBuilder.append(", ");
            }
            resultBuilder.append(parameterExpression.automaticName());
        }
        resultBuilder.append(")");
        return resultBuilder.toString();
    }

}
