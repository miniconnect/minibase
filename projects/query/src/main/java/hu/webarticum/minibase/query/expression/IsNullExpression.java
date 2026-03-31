package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class IsNullExpression implements Expression {

    private final Expression operand;


    public IsNullExpression(Expression subExpression) {
        this.operand = subExpression;
    }


    public Expression subExpression() {
        return operand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return operand.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Boolean.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subValue = operand.evaluate(substitutions);
        return subValue == null;
    }

    @Override
    public String automaticName(int columnIndex) {
        return "null_" + operand.automaticName(columnIndex);
    }

}
