package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class PowExpression implements Expression {

    private final Expression baseOperand;

    private final Expression exponentOperand;


    public PowExpression(Expression baseOperand, Expression exponentOperand) {
        this.baseOperand = baseOperand;
        this.exponentOperand = exponentOperand;
    }


    public Expression baseOperand() {
        return baseOperand;
    }

    public Expression exponentOperand() {
        return exponentOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return baseOperand.parameters().concat(exponentOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Double.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return Double.class;
    }

    @Override
    public boolean isNullable() {
        return baseOperand.isNullable() || exponentOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return baseOperand.isNullable(nullabilitySubstitutions) || exponentOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object baseValue = baseOperand.evaluate(substitutions);
        if (baseValue == null) {
            return null;
        }
        Object exponentValue = exponentOperand.evaluate(substitutions);
        if (exponentValue == null) {
            return null;
        }

        double base = (Double) ConvertUtil.convert(baseValue, Double.class);
        double exponent = (Double) ConvertUtil.convert(exponentValue, Double.class);
        return Math.pow(base, exponent);
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_pow_col" + columnIndex;
    }

}
