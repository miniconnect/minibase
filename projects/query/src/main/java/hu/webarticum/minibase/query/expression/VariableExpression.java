package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class VariableExpression implements Expression {

    private final VariableParameter variableParameter;


    public VariableExpression(String variableName) {
        this.variableParameter = new VariableParameter(variableName);
    }


    public VariableParameter variableParameter() {
        return variableParameter;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return ImmutableList.of(variableParameter);
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.empty();
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> result = types.get(variableParameter);
        return result != null ? result : Void.class;
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        Boolean result = nullabilities.get(variableParameter);
        return result != null ? result : true;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        return values.get(variableParameter);
    }

    @Override
    public String automaticName() {
        return "@" + variableParameter.variableName();
    }

}
