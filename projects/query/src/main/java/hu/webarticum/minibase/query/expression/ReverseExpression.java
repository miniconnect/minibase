package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ReverseExpression implements Expression {

    private final Expression operand;


    public ReverseExpression(Expression operand) {
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
        Class<?> operandType = operand.type().orElse(null);
        if (operandType == null || operandType == ByteString.class || operandType == BitString.class) {
            return Optional.ofNullable(operandType);
        } else {
            return Optional.of(String.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        Class<?> operandType = operand.type(typeSubstitutions);
        if (operandType == ByteString.class || operandType == BitString.class) {
            return operandType;
        } else {
            return String.class;
        }
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

        if (value instanceof ByteString) {
            return ((ByteString) value).reverse();
        } else if (value instanceof BitString) {
            return ((BitString) value).reverse();
        } else {
            return new StringBuilder(StringUtil.stringify(value)).reverse().toString();
        }
    }

    @Override
    public String automaticName(int columnIndex) {
        return "rev_" + operand.automaticName(columnIndex);
    }

}
