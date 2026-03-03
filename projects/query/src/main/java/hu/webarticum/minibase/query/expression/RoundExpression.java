package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class RoundExpression implements Expression {

    public enum RoundMode {

        ROUND(RoundingMode.HALF_UP, Math::round),
        CEIL(RoundingMode.CEILING, Math::ceil),
        FLOOR(RoundingMode.FLOOR, Math::floor),
        ;

        private final RoundingMode roundingMode;

        private final DoubleUnaryOperator doubleOperator;

        private RoundMode(RoundingMode roundingMode, DoubleUnaryOperator doubleOperator) {
            this.roundingMode = roundingMode;
            this.doubleOperator = doubleOperator;
        }

    }


    private final Expression givenExpression;

    private final RoundMode roundMode;


    public RoundExpression(Expression givenExpression, RoundMode roundMode) {
        this.givenExpression = givenExpression;
        this.roundMode = roundMode;
    }


    public Expression givenExpression() {
        return givenExpression;
    }

    public RoundMode roundMode() {
        return roundMode;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return givenExpression.parameters();
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
        return givenExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return givenExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object givenValue = givenExpression.evaluate(values);
        if (givenValue == null) {
            return null;
        }

        Number givenNumber = NumberUtil.numberify(givenValue);
        if (givenNumber instanceof BigDecimal) {
            return LargeInteger.of(((BigDecimal) givenNumber).setScale(0, roundMode.roundingMode).toBigInteger());
        } else if (givenNumber instanceof LargeInteger) {
            return (LargeInteger) givenNumber;
        } else {
            double doubleValue = givenNumber.doubleValue();
            double roundedDoubleValue = roundMode.doubleOperator.applyAsDouble(doubleValue);
            return LargeInteger.of((long) roundedDoubleValue);
        }
    }

    @Override
    public String automaticName() {
        return roundMode.name() + "(" + givenExpression.automaticName() + ")";
    }

}
