package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

import hu.webarticum.minibase.query.util.DateTimeDeltaUtil;
import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class AbsExpression implements Expression {

    private final Expression subExpression;


    public AbsExpression(Expression subExpression) {
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
        if (subType == null) {
            return Optional.empty();
        }
        if (TemporalAmount.class.isAssignableFrom(subType)) {
            return Optional.of(DateTimeDelta.class);
        }
        return Optional.of(NumberUtil.numberifyType(subType));
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> subType = subExpression.type(types);
        if (TemporalAmount.class.isAssignableFrom(subType)) {
            return DateTimeDelta.class;
        }
        return NumberUtil.numberifyType(subType);
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
        if (subValue instanceof TemporalAmount) {
            return absDateTimeDelta(DateTimeDeltaUtil.deltaify(subValue));
        }
        Number subNumber = NumberUtil.numberify(subValue);
        if (subNumber == null) {
            return null;
        } else if (subNumber instanceof LargeInteger) {
            return ((LargeInteger) subNumber).abs();
        } else if (subNumber instanceof BigDecimal) {
            return ((BigDecimal) subNumber).abs();
        } else {
            return Math.abs(subNumber.doubleValue());
        }
    }

    private DateTimeDelta absDateTimeDelta(DateTimeDelta delta) {
        if (!delta.isNegative()) {
            return delta;
        } 
        DateTimeDelta negatedDelta = delta.negated();
        if (!negatedDelta.isNegative()) {
            return negatedDelta;
        }
        DateTimeDelta collapsedDelta = delta.collapsed();
        return collapsedDelta.isNegative() ? collapsedDelta.negated() : collapsedDelta;
    }

    @Override
    public String automaticName() {
        return "ABS(" + subExpression.automaticName() + ")";
    }

}
