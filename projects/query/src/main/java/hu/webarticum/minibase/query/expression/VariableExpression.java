package hu.webarticum.minibase.query.expression;

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
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        return values.get(variableParameter);
    }
    
    @Override
    public String automaticName() {
        return "@" + variableParameter.variableName();
    }

}
