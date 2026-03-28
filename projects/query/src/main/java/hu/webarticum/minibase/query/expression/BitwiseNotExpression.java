package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BitwiseNotExpression implements Expression {

    private final Expression operand;


    public BitwiseNotExpression(Expression operand) {
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
        if (operandType == null) {
            return Optional.empty();
        } else if (operandType == Void.class || Number.class.isAssignableFrom(operandType)) {
            return Optional.of(LargeInteger.class);
        } else {
            return Optional.of(BitString.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        Class<?> operandType = operand.type(typeSubstitutions);
        if (operandType == Void.class || Number.class.isAssignableFrom(operandType)) {
            return operandType;
        } else {
            return BitString.class;
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
        } else if (value instanceof Number) {
            LargeInteger largeIntegerValue = (LargeInteger) ConvertUtil.convert(value, LargeInteger.class);
            return largeIntegerValue.not();
        }

        return bitStringify(value).not();
    }

    private BitString bitStringify(Object value) {
        Object bitsValue = value instanceof String ? ByteString.of((String) value) : value;
        return BitStringUtil.bitStringify(bitsValue);
    }

    @Override
    public String automaticName() {
        return "~" + operand.automaticName();
    }

}
