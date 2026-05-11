package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class LcmExpression implements Expression {

    private final Expression aOperand;

    private final Expression bOperand;


    public LcmExpression(Expression aOperand, Expression bOperand) {
        this.aOperand = aOperand;
        this.bOperand = bOperand;
    }


    public Expression aOperand() {
        return aOperand;
    }

    public Expression bOperand() {
        return bOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return aOperand.parameters().concat(bOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> typeOfA = aOperand.type().orElse(null);
        Class<?> typeOfB = bOperand.type().orElse(null);
        Class<?> numericTypeOfA = typeOfA != null ? NumberUtil.numberifyType(typeOfA) : null;
        Class<?> numericTypeOfB = typeOfB != null ? NumberUtil.numberifyType(typeOfB) : null;
        if (
                numericTypeOfA == Double.class ||
                numericTypeOfA == BigDecimal.class ||
                numericTypeOfB == Double.class ||
                numericTypeOfB == BigDecimal.class) {
            return Optional.of(BigDecimal.class);
        } else if (numericTypeOfA == null || numericTypeOfB == null) {
            return Optional.empty();
        } else {
            return Optional.of(LargeInteger.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        Class<?> numericTypeOfA = NumberUtil.numberifyType(aOperand.type(typeSubstitutions));
        Class<?> numericTypeOfB = NumberUtil.numberifyType(bOperand.type(typeSubstitutions));
        if (
                numericTypeOfA == Double.class ||
                numericTypeOfA == BigDecimal.class ||
                numericTypeOfB == Double.class ||
                numericTypeOfB == BigDecimal.class) {
            return BigDecimal.class;
        } else {
            return LargeInteger.class;
        }
    }

    @Override
    public boolean isNullable() {
        return aOperand.isNullable() || bOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return aOperand.isNullable(nullabilitySubstitutions) || bOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object aValue = aOperand.evaluate(substitutions);
        if (aValue == null) {
            return null;
        }
        Object bValue = bOperand.evaluate(substitutions);
        if (bValue == null) {
            return null;
        }

        Number aNumber = NumberUtil.numberify(aValue);
        Number bNumber = NumberUtil.numberify(bValue);
        if (aNumber instanceof LargeInteger && bNumber instanceof LargeInteger) {
            return ((LargeInteger) aNumber).lcm((LargeInteger) bNumber);
        } else {
            BigDecimal a = (BigDecimal) ConvertUtil.convert(aNumber, BigDecimal.class);
            BigDecimal b = (BigDecimal) ConvertUtil.convert(bNumber, BigDecimal.class);
            BigDecimal gcd = NumberUtil.gcd(a, b);
            int commonScale = Math.max(a.scale(), b.scale());
            return a.divide(gcd).multiply(b).setScale(commonScale, RoundingMode.UNNECESSARY).abs();
        }
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_lcm_col" + columnIndex;
    }

}
