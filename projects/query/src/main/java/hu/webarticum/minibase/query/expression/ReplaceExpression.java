package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ReplaceExpression implements Expression {

    private final Expression contextExpression;

    private final Expression fromExpression;

    private final Expression toExpression;


    public ReplaceExpression(Expression contextExpression, Expression fromExpression, Expression toExpression) {
        this.contextExpression = contextExpression;
        this.fromExpression = fromExpression;
        this.toExpression = toExpression;
    }


    public Expression contextExpression() {
        return contextExpression;
    }

    public Expression fromExpression() {
        return fromExpression;
    }

    public Expression toExpression() {
        return toExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextExpression.parameters().concat(fromExpression.parameters()).concat(toExpression.parameters());
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
        return contextExpression.isNullable() || fromExpression.isNullable() || toExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return
                contextExpression.isNullable(nullabilities) ||
                fromExpression.isNullable(nullabilities) ||
                toExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object contextValue = contextExpression.evaluate(values);
        if (contextValue == null) {
            return null;
        }

        Object fromValue = fromExpression.evaluate(values);
        if (fromValue == null) {
            return null;
        }

        Object toValue = toExpression.evaluate(values);
        if (toValue == null) {
            return null;
        }

        String contextString = StringUtil.stringify(contextValue);
        String fromString = StringUtil.stringify(fromValue);
        String toString = StringUtil.stringify(toValue);

        return StringUtil.replace(contextString, fromString, toString);
    }

    @Override
    public String automaticName() {
        return "REPLACE(" + contextExpression.automaticName() + ", " +
                fromExpression.automaticName() + ", " +
                toExpression.automaticName() + ")";
    }

}
