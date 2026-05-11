package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class OctetLengthExpression implements Expression {

    private final Expression operand;


    public OctetLengthExpression(Expression operand) {
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
        } else if (value instanceof BitString) {
            return LargeInteger.of((((BitString) value).length() + 7) >>> 3);
        } else if (value instanceof Number) {
            return LargeInteger.of((BitStringUtil.bitStringify(value).length() + 7) >>> 3);
        } else {
            return LargeInteger.of(ByteStringUtil.byteStringify(value).length());
        }
    }

    @Override
    public String automaticName(int columnIndex) {
        return "length_" + operand.automaticName(columnIndex);
    }

}
