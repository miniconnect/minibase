package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class OrdExpression implements Expression {

    private final Expression operand;


    public OrdExpression(Expression operand) {
        this.operand = operand;
    }


    public Expression operand() {
        return operand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return operand.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(LargeInteger.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return LargeInteger.class;
    }

    @Override
    public boolean isNullable() {
        return operand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return operand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object value = operand.evaluate(substitutions);
        if (value == null) {
            return null;
        }

        String stringValue = StringUtil.stringify(value);
        if (stringValue.isEmpty()) {
            return LargeInteger.ZERO;
        }
        return LargeInteger.of((long) stringValue.charAt(0));
    }

    @Override
    public String automaticName(int columnIndex) {
        return "ord_" + operand.automaticName(columnIndex);
    }

}
