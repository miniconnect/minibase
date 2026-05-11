package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class Atan2Expression implements Expression {

    private final Expression yOperand;

    private final Expression xOperand;


    public Atan2Expression(Expression yOperand, Expression xOperand) {
        this.yOperand = yOperand;
        this.xOperand = xOperand;
    }


    public Expression yOperand() {
        return yOperand;
    }

    public Expression xOperand() {
        return xOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return yOperand.parameters().concat(xOperand.parameters());
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
        return yOperand.isNullable() || xOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return yOperand.isNullable(nullabilitySubstitutions) || xOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object yValue = yOperand.evaluate(substitutions);
        if (yValue == null ) {
            return null;
        }
        Object xValue = xOperand.evaluate(substitutions);
        if (xValue == null ) {
            return null;
        }

        double y = (Double) ConvertUtil.convert(yValue, Double.class);
        double x = (Double) ConvertUtil.convert(xValue, Double.class);
        return Math.atan2(y, x);
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_atan2_col" + columnIndex;
    }

}
