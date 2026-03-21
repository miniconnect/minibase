package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class GcdExpression implements Expression {

    private final Expression aExpression;

    private final Expression bExpression;


    public GcdExpression(Expression aExpression, Expression bExpression) {
        this.aExpression = aExpression;
        this.bExpression = bExpression;
    }


    public Expression aExpression() {
        return aExpression;
    }

    public Expression bExpression() {
        return bExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return aExpression.parameters().concat(bExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> leftType = aExpression.type().orElse(null);
        Class<?> rightType = bExpression.type().orElse(null);
        Class<?> leftNumericType = leftType != null ? NumberUtil.numberifyType(leftType) : null;
        Class<?> rightNumericType = rightType != null ? NumberUtil.numberifyType(rightType) : null;
        if (
                leftNumericType == Double.class ||
                leftNumericType == BigDecimal.class ||
                rightNumericType == Double.class ||
                rightNumericType == BigDecimal.class) {
            return Optional.of(BigDecimal.class);
        } else if (leftNumericType == null || rightNumericType == null) {
            return Optional.empty();
        } else {
            return Optional.of(LargeInteger.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> leftNumericType = NumberUtil.numberifyType(aExpression.type(types));
        Class<?> rightNumericType = NumberUtil.numberifyType(bExpression.type(types));
        if (
                leftNumericType == Double.class ||
                leftNumericType == BigDecimal.class ||
                rightNumericType == Double.class ||
                rightNumericType == BigDecimal.class) {
            return BigDecimal.class;
        } else {
            return LargeInteger.class;
        }
    }

    @Override
    public boolean isNullable() {
        return aExpression.isNullable() || bExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return aExpression.isNullable(nullabilities) || bExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object aValue = aExpression().evaluate(values);
        if (aValue == null) {
            return null;
        }
        Object bValue = bExpression().evaluate(values);
        if (bValue == null) {
            return null;
        }

        Number aNumber = NumberUtil.numberify(aValue);
        Number bNumber = NumberUtil.numberify(bValue);
        if (aNumber instanceof LargeInteger && bNumber instanceof LargeInteger) {
            return ((LargeInteger) aNumber).gcd((LargeInteger) bNumber);
        } else {
            BigDecimal a = (BigDecimal) ConvertUtil.convert(aNumber, BigDecimal.class);
            BigDecimal b = (BigDecimal) ConvertUtil.convert(bNumber, BigDecimal.class);
            return NumberUtil.gcd(a, b);
        }
    }

    @Override
    public String automaticName() {
        return "GCD(" + aExpression.automaticName() + ", " + bExpression.automaticName() + ")";
    }

}
