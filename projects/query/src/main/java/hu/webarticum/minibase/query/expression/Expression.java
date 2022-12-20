package hu.webarticum.minibase.query.expression;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public interface Expression {

    public ImmutableList<Parameter> parameters();
    
    public Object evaluate(ImmutableMap<Parameter, Object> values);
    
    public String automaticName();
    
}
