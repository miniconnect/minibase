package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class ModExpression implements Expression {

    private final Expression leftOperand;

    private final Expression rightOperand;


    public ModExpression(Expression leftOperand, Expression rightOperand) {
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
        if (leftType == null || rightType == null) {
            return Optional.empty();
        }
        return Optional.of(NumberUtil.commonNumericTypeOf(leftType, rightType));
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return NumberUtil.commonNumericTypeOf(leftOperand.type(types), rightOperand.type(types));
    }

    @Override
    public boolean isNullable() {
        return leftOperand.isNullable() || rightOperand.isNullable() || canResultInNonPositive(rightOperand);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return leftOperand.isNullable(nullabilities) || rightOperand.isNullable(nullabilities) || canResultInNonPositive(rightOperand);
    }

    private boolean canResultInNonPositive(Expression expression) {
        if (!expression.parameters().isEmpty()) {
            return true;
        }

        return !NumberUtil.isPositive(expression.evaluate(ImmutableMap.empty()));
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object leftValue = leftOperand.evaluate(values);
        Object rightValue = rightOperand.evaluate(values);
        if (!NumberUtil.isPositive(rightValue)) {
            // TODO: raise SQL warning
            return null;
        }
        Class<?> commonType = NumberUtil.commonNumericTypeOf(leftValue.getClass(), rightValue.getClass());
        if (commonType == Double.class) {
            double leftDouble = (Double) ConvertUtil.convert(leftValue, Double.class);
            double rightDouble = (Double) ConvertUtil.convert(rightValue, Double.class);
            return mod(leftDouble, rightDouble);
        } else if (commonType == BigDecimal.class) {
            BigDecimal leftBigDecimal = (BigDecimal) ConvertUtil.convert(leftValue, BigDecimal.class);
            BigDecimal rightBigDecimal = (BigDecimal) ConvertUtil.convert(rightValue, BigDecimal.class);
            return mod(leftBigDecimal, rightBigDecimal);
        } else if (commonType == LargeInteger.class) {
            LargeInteger leftLargeInteger = (LargeInteger) ConvertUtil.convert(leftValue, LargeInteger.class);
            LargeInteger rightLargeInteger = (LargeInteger) ConvertUtil.convert(rightValue, LargeInteger.class);
            return leftLargeInteger.mod(rightLargeInteger);
        } else {
            throw new IllegalArgumentException("Can not unify values for mod operation");
        }
    }

    private double mod(double leftDouble, double rightDouble) {
        double remainder = leftDouble % rightDouble;
        if (remainder >= 0) {
            return remainder;
        } else {
            return rightDouble + remainder;
        }
    }

    public BigDecimal mod(BigDecimal leftBigDecimal, BigDecimal rightBigDecimal) {
        BigDecimal remainder = leftBigDecimal.remainder(rightBigDecimal);
        if (remainder.signum() >= 0) {
            return remainder;
        } else {
            return rightBigDecimal.add(remainder);
        }
    }

    @Override
    public String automaticName() {
        return leftOperand.automaticName() + " MOD " + rightOperand.automaticName();
    }

}
