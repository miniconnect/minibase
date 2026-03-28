package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class AsciiExpression implements Expression {

    private final Expression operand;


    public AsciiExpression(Expression operand) {
        this.operand = operand;
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

        ByteString byteStringValue = ByteStringUtil.byteStringify(value);
        if (byteStringValue.isEmpty()) {
            return LargeInteger.ZERO;
        }
        return LargeInteger.of(Byte.toUnsignedLong(byteStringValue.byteAt(0)));
    }

    @Override
    public String automaticName() {
        return "ASCII(" + operand.automaticName() + ")";
    }

}
