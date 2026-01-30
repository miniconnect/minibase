package hu.webarticum.minibase.query.expression;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.minibase.query.util.ValueUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class InExpression implements Expression {

    private final Expression givenExpression;

    private final ImmutableList<Expression> listedExpressions;


    public InExpression(Expression givenExpression, ImmutableList<Expression> listedExpressions) {
        this.givenExpression = givenExpression;
        this.listedExpressions = listedExpressions;
    }


    public Expression givenExpression() {
        return givenExpression;
    }

    public ImmutableList<Expression> listedExpressions() {
        return listedExpressions;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        Set<Parameter> subParameters = new LinkedHashSet<>();
        subParameters.addAll(givenExpression.parameters().asList());
        for (Expression listedExpression : listedExpressions) {
            subParameters.addAll(listedExpression.parameters().asList());
        }
        return ImmutableList.fromCollection(subParameters);
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Boolean.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        if (givenExpression.isNullable()) {
            return true;
        }
        for (Expression listedExpression : listedExpressions) {
            if (!listedExpression.isNullable()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        if (givenExpression.isNullable(nullabilities)) {
            return true;
        }
        for (Expression listedExpression : listedExpressions) {
            if (!listedExpression.isNullable(nullabilities)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object givenValue = givenExpression.evaluate(values);
        if (givenValue == null) {
            return null;
        }
        Class<?> givenType = UnifyUtil.typeOf(givenValue);
        boolean foundNotNull = false;
        for (Expression listedExpression : listedExpressions) {
            Object listedValue = listedExpression.evaluate(values);
            if (listedValue == null) {
                continue;
            }
            Object convertedValue = ConvertUtil.convert(listedValue, givenType);
            if (ValueUtil.evalEquality(givenValue, convertedValue)) {
                return true;
            }
            foundNotNull = true;
        }
        return foundNotNull ? false : null;
    }

    @Override
    public String automaticName() {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(givenExpression.automaticName());
        resultBuilder.append(" IN (");
        boolean first = true;
        for (Expression listedExpression : listedExpressions) {
            if (first) {
                first = false;
            } else {
                resultBuilder.append(", ");
            }
            resultBuilder.append(listedExpression.automaticName());
        }
        resultBuilder.append(")");
        return resultBuilder.toString();
    }

}
