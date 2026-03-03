package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class SignExpression implements Expression {

    private final Expression subExpression;


    public SignExpression(Expression subExpression) {
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
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
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
        Object subValue = subExpression.evaluate(values);
        if (subValue == null) {
            return null;
        }

        Number numberValue = NumberUtil.numberify(subValue);
        if (numberValue instanceof LargeInteger) {
            return LargeInteger.of(((LargeInteger) numberValue).signum());
        } else if (numberValue instanceof BigDecimal) {
            return LargeInteger.of(((BigDecimal) numberValue).signum());
        } else {
            double doubleValue = numberValue.doubleValue();
            return Math.signum(doubleValue);
        }
    }

    @Override
    public String automaticName() {
        return "SIGN(" + subExpression.automaticName() + ")";
    }

}
