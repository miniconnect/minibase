package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class Atan2Expression implements Expression {

    private final Expression yExpression;

    private final Expression xExpression;


    public Atan2Expression(Expression yExpression, Expression xExpression) {
        this.yExpression = yExpression;
        this.xExpression = xExpression;
    }


    public Expression yExpression() {
        return yExpression;
    }

    public Expression xExpression() {
        return xExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return yExpression.parameters().concat(xExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Double.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return Double.class;
    }

    @Override
    public boolean isNullable() {
        return yExpression.isNullable() || xExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return yExpression.isNullable(nullabilities) || xExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object yValue = yExpression.evaluate(values);
        if (yValue == null ) {
            return null;
        }
        Object xValue = xExpression.evaluate(values);
        if (xValue == null ) {
            return null;
        }

        double y = (Double) ConvertUtil.convert(yValue, Double.class);
        double x = (Double) ConvertUtil.convert(xValue, Double.class);
        return Math.atan2(y, x);
    }

    @Override
    public String automaticName() {
        return "ATAN2(" + yExpression.automaticName() + ", " + xExpression.automaticName() + ")";
    }

}
