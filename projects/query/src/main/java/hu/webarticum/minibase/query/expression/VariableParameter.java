package hu.webarticum.minibase.query.expression;

public class VariableParameter implements Parameter {

    private final String variableName;
    
    
    public VariableParameter(String variableName) {
        this.variableName = variableName;
    }
    
    
    public String variableName() {
        return variableName;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (!(other instanceof VariableParameter)) {
            return false;
        }
        
        VariableParameter otherVariableParameter = (VariableParameter) other;
        return variableName.equals(otherVariableParameter.variableName);
    }
    
    @Override
    public int hashCode() {
        return variableName.hashCode();
    }
    
}
