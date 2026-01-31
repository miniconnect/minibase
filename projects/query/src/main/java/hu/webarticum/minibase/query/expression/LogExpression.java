package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class LogExpression implements Expression {

    private final Expression baseExpression;

    private final Expression givenExpression;


    public LogExpression(Expression baseExpression, Expression givenExpression) {
        this.baseExpression = baseExpression;
        this.givenExpression = givenExpression;
    }


    public Expression givenExpression() {
        return givenExpression;
    }

    public Expression baseExpression() {
        return baseExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return givenExpression.parameters().concat(baseExpression.parameters());
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
        return baseExpression.isNullable() || givenExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return baseExpression.isNullable(nullabilities) || givenExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object baseValue = baseExpression.evaluate(values);
        if (baseValue == null) {
            return null;
        }
        Object givenValue = givenExpression.evaluate(values);
        if (givenValue == null) {
            return null;
        }

        double base = (Double) ConvertUtil.convert(baseValue, Double.class);
        double given = (Double) ConvertUtil.convert(givenValue, Double.class);
        return Math.log(given) / Math.log(base);
    }

    @Override
    public String automaticName() {
    return "LOG(" + baseExpression.automaticName() + ", " + givenExpression.automaticName() + ")";
    }

}
