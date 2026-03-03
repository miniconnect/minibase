package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class OrdExpression implements Expression {

    private final Expression subExpression;


    public OrdExpression(Expression subExpression) {
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

        String stringValue = StringUtil.stringify(value);
        if (stringValue.isEmpty()) {
            return LargeInteger.ZERO;
        }
        return LargeInteger.of((long) stringValue.charAt(0));
    }

    @Override
    public String automaticName() {
        return "ORD(" + subExpression.automaticName() + ")";
    }

}
