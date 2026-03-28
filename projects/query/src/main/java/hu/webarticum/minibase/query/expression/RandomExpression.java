package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RandomExpression implements Expression {

    public RandomExpression() {
    }


    @Override
    public ImmutableList<Parameter> parameters() {
        return ImmutableList.empty();
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
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        return ThreadLocalRandom.current().nextDouble();
    }

    @Override
    public String automaticName() {
        return "RANDOM()";
    }

}
