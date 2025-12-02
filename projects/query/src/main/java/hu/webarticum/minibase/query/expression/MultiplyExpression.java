package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.DateTimeDeltaUtil;
import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class MultiplyExpression implements Expression {
    
    private final Expression leftOperand;
    
    private final Expression rightOperand;
    
    
    public MultiplyExpression(Expression leftOperand, Expression rightOperand) {
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
        if (leftType == rightType) {
            return Optional.ofNullable(leftType);
        }
        if (leftType == null) {
            return TemporalAmount.class.isAssignableFrom(rightType) ? Optional.of(DateTimeDelta.class) : Optional.empty();
        } else if (rightType == null) {
            return TemporalAmount.class.isAssignableFrom(leftType) ? Optional.of(DateTimeDelta.class) : Optional.empty();
        }
        return Optional.of(unifyTypesForMultiplication(leftType, rightType));
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> leftType = leftOperand.type(types);
        Class<?> rightType = rightOperand.type(types);
        return unifyTypesForMultiplication(leftType, rightType);
    }

    private Class<?> unifyTypesForMultiplication(Class<?> leftType, Class<?> rightType) {
        if (TemporalAmount.class.isAssignableFrom(leftType) || TemporalAmount.class.isAssignableFrom(rightType)) {
            return DateTimeDelta.class;
        } else {
            return NumberUtil.commonNumericTypeOf(leftType, rightType);
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
        }
        if (leftValue instanceof TemporalAmount) {
            return multiplyTemporalAmount(leftValue, rightValue);
        } else if (rightValue instanceof TemporalAmount) {
            return multiplyTemporalAmount(rightValue, leftValue);
        }
        Class<?> commonType = NumberUtil.commonNumericTypeOf(leftValue.getClass(), rightValue.getClass());
        if (commonType == Double.class) {
            double leftDouble = (Double) ConvertUtil.convert(leftValue, Double.class);
            double rightDouble = (Double) ConvertUtil.convert(rightValue, Double.class);
            return leftDouble * rightDouble;
        } else if (commonType == BigDecimal.class) {
            BigDecimal leftBigDecimal = (BigDecimal) ConvertUtil.convert(leftValue, BigDecimal.class);
            BigDecimal rightBigDecimal = (BigDecimal) ConvertUtil.convert(rightValue, BigDecimal.class);
            return leftBigDecimal.multiply(rightBigDecimal);
        } else if (commonType == LargeInteger.class) {
            LargeInteger leftLargeInteger = (LargeInteger) ConvertUtil.convert(leftValue, LargeInteger.class);
            LargeInteger rightLargeInteger = (LargeInteger) ConvertUtil.convert(rightValue, LargeInteger.class);
            return leftLargeInteger.multiply(rightLargeInteger);
        } else {
            throw new IllegalArgumentException("Can not unify values for multiplication");
        }
    }

    private DateTimeDelta multiplyTemporalAmount(Object temporalAmount, Object multiplier) {
            DateTimeDelta delta = DateTimeDeltaUtil.deltaify(temporalAmount);
            Number number = NumberUtil.numberify(multiplier);
            if (number instanceof LargeInteger) {
                int scalar = NumberUtil.asInt(number);
                return delta.multipliedBy(scalar);
            }

            Duration duration = delta.toCollapsedDuration();
            BigDecimal bigDecimalMultiplier = NumberUtil.bigDecimalify(number);
            BigDecimal bigDecimalAmount = BigDecimal.valueOf(duration.getSeconds());
            bigDecimalAmount = bigDecimalAmount.add(new BigDecimal(BigInteger.valueOf(duration.getNano()), 9));
            bigDecimalAmount = bigDecimalAmount.multiply(bigDecimalMultiplier);
            long seconds = bigDecimalAmount.toBigInteger().longValueExact();
            int nanos = bigDecimalAmount.setScale(9, RoundingMode.HALF_UP).remainder(BigDecimal.ONE).unscaledValue().intValue();
            return DateTimeDelta.of(Duration.ofSeconds(seconds, nanos)).collapsed();
    }

    @Override
    public String automaticName() {
        return leftOperand.automaticName() + " * " + rightOperand.automaticName();
    }
    
}
