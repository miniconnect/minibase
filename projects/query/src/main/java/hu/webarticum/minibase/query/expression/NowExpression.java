package hu.webarticum.minibase.query.expression;

import java.time.Instant;
import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class NowExpression implements Expression {

    public NowExpression() {
    }


    @Override
    public ImmutableList<Parameter> parameters() {
        return ImmutableList.empty();
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Instant.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return Instant.class;
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
        return Instant.now();
    }

    @Override
    public String automaticName() {
        return "NOW()";
    }

}
