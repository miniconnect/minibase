package hu.webarticum.minibase.query.query;

public class VariableValue implements SpecialCondition {
    
    private final String name;
    

    public VariableValue(String name) {
        this.name = name;
    }
    
    
    public String name() {
        return name;
    }
    
}
