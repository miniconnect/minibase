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

public class NegateExpression implements Expression {

    private final Expression subExpression;


    public NegateExpression(Expression subExpression) {
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
            DateTimeDelta subDelta = DateTimeDeltaUtil.deltaify(subValue);
            return subDelta.negated();
        }
        Number subNumber = NumberUtil.numberify(subValue);
        if (subNumber == null) {
            return null;
        } else if (subNumber instanceof LargeInteger) {
            return ((LargeInteger) subNumber).negate();
        } else if (subNumber instanceof BigDecimal) {
            return ((BigDecimal) subNumber).negate();
        } else {
            return -subNumber.doubleValue();
        }
    }

    @Override
    public String automaticName() {
        return "-" + subExpression.automaticName();
    }

}
