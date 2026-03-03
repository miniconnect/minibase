package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class IsNullExpression implements Expression {

    private final Expression subExpression;


    public IsNullExpression(Expression subExpression) {
        this.subExpression = subExpression;
    }


    public Expression subExpression() {
        return subExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subExpression.parameters();
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
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object subValue = subExpression.evaluate(values);
        return subValue == null;
    }

    @Override
    public String automaticName() {
        return subExpression.automaticName() + " IS NULL";
    }

}
