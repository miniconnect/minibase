package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class PiExpression implements Expression {

    public PiExpression() {
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
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return Double.class;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return false;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        return Math.PI;
    }

    @Override
    public String automaticName() {
        return "PI()";
    }

}
