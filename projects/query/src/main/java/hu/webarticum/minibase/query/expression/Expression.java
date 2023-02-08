package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public interface Expression {

    public ImmutableList<Parameter> parameters();
    
    public Optional<Class<?>> type();
    
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types);
    
    public Object evaluate(ImmutableMap<Parameter, Object> values);
    
    public String automaticName();
    
}
