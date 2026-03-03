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

public class DivideExpression implements Expression {

    private final Expression leftOperand;

    private final Expression rightOperand;


    public DivideExpression(Expression leftOperand, Expression rightOperand) {
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
        if (TemporalAmount.class.isAssignableFrom(leftType)) {
            return Optional.of(DateTimeDelta.class);
        } else if (leftType == null || rightType == null) {
            return Optional.empty();
        }
        return Optional.of(NumberUtil.commonNumericTypeOf(leftType, rightType));
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> leftType = leftOperand.type(types);
        if (TemporalAmount.class.isAssignableFrom(leftType)) {
            return DateTimeDelta.class;
        }
        return NumberUtil.commonNumericTypeOf(leftType, rightOperand.type(types));
    }

    @Override
    public boolean isNullable() {
        return leftOperand.isNullable() || rightOperand.isNullable() || canResultInZero(rightOperand);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return leftOperand.isNullable(nullabilities) || rightOperand.isNullable(nullabilities) || canResultInZero(rightOperand);
    }

    private boolean canResultInZero(Expression expression) {
        if (!expression.parameters().isEmpty()) {
            return true;
        }

        return NumberUtil.isZero(expression.evaluate(ImmutableMap.empty()));
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object leftValue = leftOperand.evaluate(values);
        Object rightValue = rightOperand.evaluate(values);
        if (NumberUtil.isZero(rightValue)) {
            // TODO: raise SQL warning
            return null;
        }
        if (leftValue instanceof TemporalAmount) {
            return divideTemporalAmount(leftValue, rightValue);
        }
        Class<?> commonType = NumberUtil.commonNumericTypeOf(leftValue.getClass(), rightValue.getClass());
        if (commonType == Double.class) {
            double leftDouble = (Double) ConvertUtil.convert(leftValue, Double.class);
            double rightDouble = (Double) ConvertUtil.convert(rightValue, Double.class);
            return leftDouble / rightDouble;
        } else if (commonType == BigDecimal.class) {
            BigDecimal leftBigDecimal = (BigDecimal) ConvertUtil.convert(leftValue, BigDecimal.class);
            BigDecimal rightBigDecimal = (BigDecimal) ConvertUtil.convert(rightValue, BigDecimal.class);
            return NumberUtil.divideBigDecimals(leftBigDecimal, rightBigDecimal);
        } else if (commonType == LargeInteger.class) {
            LargeInteger leftLargeInteger = (LargeInteger) ConvertUtil.convert(leftValue, LargeInteger.class);
            LargeInteger rightLargeInteger = (LargeInteger) ConvertUtil.convert(rightValue, LargeInteger.class);
            return leftLargeInteger.divide(rightLargeInteger);
        } else {
            throw new IllegalArgumentException("Can not unify values for division");
        }
    }

    private DateTimeDelta divideTemporalAmount(Object temporalAmount, Object divisor) {
        Duration duration = DateTimeDeltaUtil.deltaify(temporalAmount).toCollapsedDuration();
        BigDecimal bigDecimalDivisor = NumberUtil.bigDecimalify(NumberUtil.numberify(divisor));
        BigDecimal bigDecimalAmount = BigDecimal.valueOf(duration.getSeconds());
        bigDecimalAmount = bigDecimalAmount.add(new BigDecimal(BigInteger.valueOf(duration.getNano()), 9));
        bigDecimalAmount = NumberUtil.divideBigDecimals(bigDecimalAmount, bigDecimalDivisor);
        long seconds = bigDecimalAmount.toBigInteger().longValueExact();
        int nanos = bigDecimalAmount.setScale(9, RoundingMode.HALF_UP).remainder(BigDecimal.ONE).unscaledValue().intValue();
        return DateTimeDelta.of(Duration.ofSeconds(seconds, nanos)).collapsed();
    }

    @Override
    public String automaticName() {
        return leftOperand.automaticName() + " / " + rightOperand.automaticName();
    }

}
