package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ByteStringUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
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
        Class<?> contextType = contextExpression.type().orElse(null);
        if (contextType == null || contextType == ByteString.class || contextType == BitString.class) {
            return Optional.ofNullable(contextType);
        } else {
            return Optional.of(String.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        Class<?> contextType = contextExpression.type(values);
        if (contextType == ByteString.class || contextType == BitString.class) {
            return contextType;
        } else {
            return String.class;
        }
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

        if (contextValue instanceof ByteString) {
            return ByteStringUtil.replace(
                    (ByteString) contextValue, ByteStringUtil.byteStringify(fromValue), ByteStringUtil.byteStringify(toValue));
        } else if (contextValue instanceof BitString) {
            return BitStringUtil.replace(
                    (BitString) contextValue, BitStringUtil.bitStringify(fromValue), BitStringUtil.bitStringify(toValue));
        } else {
            return StringUtil.replace(
                    StringUtil.stringify(contextValue), StringUtil.stringify(fromValue), StringUtil.stringify(toValue));
        }
    }

    @Override
    public String automaticName() {
        return "REPLACE(" + contextExpression.automaticName() + ", " +
                fromExpression.automaticName() + ", " +
                toExpression.automaticName() + ")";
    }

}
