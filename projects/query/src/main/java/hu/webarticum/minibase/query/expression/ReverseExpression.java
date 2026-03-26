package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ReverseExpression implements Expression {

    private final Expression subExpression;


    public ReverseExpression(Expression subExpression) {
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
        Class<?> subType = subExpression.type().orElse(null);
        if (subType == null || subType == ByteString.class || subType == BitString.class) {
            return Optional.ofNullable(subType);
        } else {
            return Optional.of(String.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        Class<?> subType = subExpression.type(values);
        if (subType == ByteString.class || subType == BitString.class) {
            return subType;
        } else {
            return String.class;
        }
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

        if (value instanceof ByteString) {
            return ((ByteString) value).reverse();
        } else if (value instanceof BitString) {
            return ((BitString) value).reverse();
        } else {
            return new StringBuilder(StringUtil.stringify(value)).reverse().toString();
        }
    }

    @Override
    public String automaticName() {
        return "REVERSE(" + subExpression.automaticName() + ")";
    }

}
