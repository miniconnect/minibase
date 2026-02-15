package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ConcatExpression implements Expression {

    private final ImmutableList<Expression> parameterExpressions;


    public ConcatExpression(ImmutableList<Expression> parameterExpressions) {
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
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return String.class;
    }

    @Override
    public boolean isNullable() {
        for (Expression parameterExpression : parameterExpressions.reverseOrder()) {
            if (parameterExpression.isNullable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        for (Expression parameterExpression : parameterExpressions.reverseOrder()) {
            if (parameterExpression.isNullable(nullabilities)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        StringBuilder resultBuilder = new StringBuilder();
        for (Expression parameterExpression : parameterExpressions) {
            Object value = parameterExpression.evaluate(values);
            if (value == null) {
                return null;
            }
            resultBuilder.append(StringUtil.stringify(value));
        }
        return resultBuilder.toString();
    }

    @Override
    public String automaticName() {
        StringBuilder resultBuilder = new StringBuilder("CONCAT(");
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
