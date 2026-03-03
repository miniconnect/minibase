package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class AsciiExpression implements Expression {

    private final Expression subExpression;


    public AsciiExpression(Expression subExpression) {
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
        return Optional.of(LargeInteger.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return LargeInteger.class;
    }

    @Override
    public boolean isNullable() {
        return subExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return subExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object value = subExpression.evaluate(values);
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
        return "ASCII(" + subExpression.automaticName() + ")";
    }

}
