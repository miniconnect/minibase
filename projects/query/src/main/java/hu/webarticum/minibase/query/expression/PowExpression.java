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

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.DateTimeDeltaUtil;
import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.DateTimeDelta;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PowExpression implements Expression {

    private final Expression baseExpression;

    private final Expression powerExpression;


    public PowExpression(Expression baseExpression, Expression powerExpression) {
        this.baseExpression = baseExpression;
        this.powerExpression = powerExpression;
    }


    public Expression baseExpression() {
        return baseExpression;
    }

    public Expression powerExpression() {
        return powerExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return baseExpression.parameters().concat(powerExpression.parameters());
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
        return baseExpression.isNullable() || powerExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return baseExpression.isNullable(nullabilities) || powerExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object baseValue = baseExpression.evaluate(values);
        if (baseValue == null) {
            return null;
        }
        Object powerValue = powerExpression.evaluate(values);
        if (powerValue == null) {
            return null;
        }

        double base = (Double) ConvertUtil.convert(baseValue, Double.class);
        double power = (Double) ConvertUtil.convert(powerValue, Double.class);
        return Math.pow(base, power);
    }

    @Override
    public String automaticName() {
        return "POW(" + baseExpression.automaticName() + ", " + powerExpression.automaticName() + ")";
    }

}
