package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ValueUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class NullifExpression implements Expression {

    private final Expression firstExpression;

    private final Expression secondExpression;


    public NullifExpression(Expression firstExpression, Expression secondExpression) {
        this.firstExpression = firstExpression;
        this.secondExpression = secondExpression;
    }


    public Expression firstExpression() {
        return firstExpression;
    }

    public Expression secondExpression() {
        return secondExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return firstExpression.parameters().concat(secondExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return firstExpression.type();
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return firstExpression.type(types);
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return true;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object value1 = firstExpression.evaluate(values);
        Object value2 = secondExpression.evaluate(values);
        boolean areEqual = ValueUtil.evalEquality(value1, value2);
        return areEqual ? null : value1;
    }

    @Override
    public String automaticName() {
        return "NULLIF(" + firstExpression.automaticName() + ", " + secondExpression.automaticName() + ")";
    }

}
