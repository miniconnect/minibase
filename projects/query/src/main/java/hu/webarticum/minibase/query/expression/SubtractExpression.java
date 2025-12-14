package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

import hu.webarticum.minibase.query.util.DateTimeDeltaUtil;
import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class SubtractExpression implements Expression {

    private final Expression leftOperand;

    private final Expression rightOperand;


    public SubtractExpression(Expression leftOperand, Expression rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }


    public Expression leftOperand() {
        return leftOperand;
    }

    public Expression rightOperand() {
        return rightOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return leftOperand.parameters().concat(rightOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> leftType = leftOperand.type().orElse(null);
        Class<?> rightType = rightOperand.type().orElse(null);
        if (leftType != null && Temporal.class.isAssignableFrom(leftType)) {
            return typeForTemporal(leftType, rightOperand);
        } else if (leftType == null) {
            return Optional.empty();
        } else if (TemporalAmount.class.isAssignableFrom(leftType)) {
            return Optional.of(DateTimeDelta.class);
        } else if (rightType == null) {
            return Optional.empty();
        }
        Class<?> leftNumericType = NumberUtil.numberifyType(leftType);
        Class<?> rightNumericType = NumberUtil.numberifyType(rightType);
        if (leftNumericType == Void.class || rightNumericType == Void.class) {
            return Optional.of(Void.class);
        } else if (leftNumericType == Double.class || rightNumericType == Double.class) {
            return Optional.of(Double.class);
        } else if (leftNumericType == BigDecimal.class || rightNumericType == BigDecimal.class) {
            return Optional.of(BigDecimal.class);
        } else if (leftNumericType == LargeInteger.class || rightNumericType == LargeInteger.class) {
            return Optional.of(LargeInteger.class);
        } else {
            throw new IllegalArgumentException("Type detection failed");
        }
    }

    private Optional<Class<?>> typeForTemporal(Class<?> temporalType, Expression temporalAmountExpression) {
        if (temporalType != LocalDate.class && temporalType != LocalTime.class && temporalType != OffsetTime.class) {
            return Optional.of(temporalType);
        } else if (temporalType == null || temporalAmountExpression.isNullable() || !temporalAmountExpression.parameters().isEmpty()) {
            return Optional.empty();
        }
        if (temporalType == LocalDate.class) {
            Class<?> temporalAmountType = temporalAmountExpression.type().orElse(null);
            if (
                    temporalAmountType != null &&
                    !TemporalAmount.class.isAssignableFrom(temporalAmountType) &&
                    NumberUtil.numberifyType(temporalAmountType) == LargeInteger.class) {
                return Optional.of(LocalDate.class);
            }
        }
        DateTimeDelta delta = DateTimeDeltaUtil.deltaify(temporalAmountExpression.evaluate(ImmutableMap.empty()));
        if (temporalType == LocalDate.class) {
            return delta.getDuration().isZero() ? Optional.of(LocalDate.class) : Optional.of(LocalDateTime.class);
        } else if (temporalType == LocalTime.class) {
            return delta.getPeriod().isZero() ? Optional.of(LocalTime.class) : Optional.of(LocalDateTime.class);
        } else {
            return delta.getPeriod().isZero() ? Optional.of(OffsetTime.class) : Optional.of(OffsetDateTime.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> leftType = leftOperand.type(types);
        Class<?> rightType = rightOperand.type(types);
        if (Temporal.class.isAssignableFrom(leftType)) {
            return typeForTemporal(leftType, rightOperand, types);
        } else if (TemporalAmount.class.isAssignableFrom(leftType)) {
            return DateTimeDelta.class;
        }
        Class<?> leftNumericType = NumberUtil.numberifyType(leftType);
        Class<?> rightNumericType = NumberUtil.numberifyType(rightType);
        if (leftNumericType == Void.class || rightNumericType == Void.class) {
            return Void.class;
        } else if (leftNumericType == Double.class || rightNumericType == Double.class) {
            return Double.class;
        } else if (leftNumericType == BigDecimal.class || rightNumericType == BigDecimal.class) {
            return BigDecimal.class;
        } else if (leftNumericType == LargeInteger.class || rightNumericType == LargeInteger.class) {
            return LargeInteger.class;
        } else {
            throw new IllegalArgumentException("Type detection failed");
        }
    }

    private Class<?> typeForTemporal(Class<?> temporalType, Expression temporalAmountExpression, ImmutableMap<Parameter, Class<?>> types) {
        if (temporalType != LocalDate.class && temporalType != LocalTime.class && temporalType != OffsetTime.class) {
            return temporalType;
        }
        if (temporalType == LocalDate.class) {
            Class<?> temporalAmountType = temporalAmountExpression.type(types);
            if (
                    !TemporalAmount.class.isAssignableFrom(temporalAmountType) &&
                    NumberUtil.numberifyType(temporalAmountType) == LargeInteger.class) {
                return LocalDate.class;
            }
        }
        boolean hasPeriod = true;
        boolean hasDuration = true;
        if (temporalAmountExpression.parameters().isEmpty()) {
            Object value = temporalAmountExpression.evaluate(ImmutableMap.empty());
            if (value == null) {
                return Void.class;
            }
            DateTimeDelta delta = DateTimeDeltaUtil.deltaify(value);
            hasPeriod = !delta.getPeriod().isZero();
            hasDuration = !delta.getDuration().isZero();
        } else {
            Class<?> temporalAmountType = temporalAmountExpression.type(types);
            if (temporalAmountType == Period.class) {
                hasDuration = false;
            } else if (temporalAmountType == Duration.class) {
                hasPeriod = false;
            } else {
                // final fallback
                return Instant.class;
            }
        }
        if (temporalType == LocalDate.class) {
            return hasDuration ? LocalDateTime.class : LocalDate.class;
        } else if (temporalType == LocalTime.class) {
            return hasPeriod ? LocalDateTime.class : LocalTime.class;
        } else {
            return hasPeriod ? OffsetDateTime.class : OffsetTime.class;
        }
    }

    @Override
    public boolean isNullable() {
        return leftOperand.isNullable() || rightOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return leftOperand.isNullable(nullabilities) || rightOperand.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object leftValue = leftOperand.evaluate(values);
        Object rightValue = rightOperand.evaluate(values);
        if (leftValue == null || rightValue == null) {
            return null;
        } else if (leftValue instanceof Temporal) {
            return operate((Temporal) leftValue, rightValue);
        } else if (leftValue instanceof TemporalAmount) {
            return DateTimeDeltaUtil.deltaify(leftValue).minus(DateTimeDeltaUtil.deltaify(rightValue));
        }
        Object leftNumericValue = NumberUtil.numberify(leftValue);
        Object rightNumericValue = NumberUtil.numberify(rightValue);
        if (leftNumericValue == null || rightNumericValue == null) {
            return null;
        } else if (leftNumericValue instanceof Double || rightNumericValue instanceof Double) {
            double leftDouble = (Double) NumberUtil.promote(leftNumericValue, Double.class);
            double rightDouble = (Double) NumberUtil.promote(rightNumericValue, Double.class);
            return operate(leftDouble, rightDouble);
        } else if (leftNumericValue instanceof BigDecimal || rightNumericValue instanceof BigDecimal) {
            BigDecimal leftBigDecimal = (BigDecimal) NumberUtil.promote(leftNumericValue, BigDecimal.class);
            BigDecimal rightBigDecimal = (BigDecimal) NumberUtil.promote(rightNumericValue, BigDecimal.class);
            return operate(leftBigDecimal, rightBigDecimal);
        } else if (leftNumericValue instanceof LargeInteger || rightNumericValue instanceof LargeInteger) {
            LargeInteger leftLargeInteger = (LargeInteger) NumberUtil.promote(leftNumericValue, LargeInteger.class);
            LargeInteger rightLargeInteger = (LargeInteger) NumberUtil.promote(rightNumericValue, LargeInteger.class);
            return operate(leftLargeInteger, rightLargeInteger);
        } else {
            throw new IllegalArgumentException("Can not unify values for subtraction");
        }
    }

    private Temporal operate(Temporal temporal, Object deltaValue) {
        DateTimeDelta delta;
        if (deltaValue instanceof TemporalAmount || !(temporal instanceof LocalDate)) {
            delta = DateTimeDeltaUtil.deltaify(deltaValue);
        } else {
            Number deltaNumber = NumberUtil.numberify(deltaValue);
            if (deltaNumber instanceof LargeInteger) {
                delta = DateTimeDelta.of(Period.ofDays(((LargeInteger) deltaNumber).intValueExact()));
            } else {
                delta = DateTimeDeltaUtil.deltaifyDays(NumberUtil.bigDecimalify(deltaNumber));
            }
        }
        return delta.subtractFromWidening(temporal);
    }

    private double operate(double left, double right) {
        return left - right;
    }

    private BigDecimal operate(BigDecimal left, BigDecimal right) {
        return left.subtract(right);
    }

    private LargeInteger operate(LargeInteger left, LargeInteger right) {
        return left.subtract(right);
    }

    @Override
    public String automaticName() {
        return leftOperand.automaticName() + " - " + rightOperand.automaticName();
    }

}
