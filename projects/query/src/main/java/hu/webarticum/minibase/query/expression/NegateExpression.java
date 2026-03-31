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

    private final Expression operand;


    public NegateExpression(Expression operand) {
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
        }
        if (TemporalAmount.class.isAssignableFrom(operandType)) {
            return Optional.of(DateTimeDelta.class);
        }
        return Optional.of(NumberUtil.numberifyType(operandType));
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        Class<?> operandType = operand.type(typeSubstitutions);
        if (TemporalAmount.class.isAssignableFrom(operandType)) {
            return DateTimeDelta.class;
        }
        return NumberUtil.numberifyType(operandType);
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
        if (value instanceof TemporalAmount) {
            DateTimeDelta subDelta = DateTimeDeltaUtil.deltaify(value);
            return subDelta.negated();
        }
        Number numericValue = NumberUtil.numberify(value);
        if (numericValue == null) {
            return null;
        } else if (numericValue instanceof LargeInteger) {
            return ((LargeInteger) numericValue).negate();
        } else if (numericValue instanceof BigDecimal) {
            return ((BigDecimal) numericValue).negate();
        } else {
            return -numericValue.doubleValue();
        }
    }

    @Override
    public String automaticName(int columnIndex) {
        return "neg_" + operand.automaticName(columnIndex);
    }

}
