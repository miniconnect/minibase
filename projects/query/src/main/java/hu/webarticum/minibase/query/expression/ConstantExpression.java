package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ConstantExpression implements Expression {

    private final Object value;
    
    
    public ConstantExpression(Object value) {
        this.value = value;
    }
    

    @Override
    public Optional<Class<?>> type() {
        Class<?> clazz = value != null ? value.getClass() : Void.class;
        return Optional.of(clazz);
    }
    
    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return value != null ? value.getClass() : Void.class;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return ImmutableList.empty();
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        return value;
    }

    @Override
    public String automaticName() {
        if (value == null) {
            return "NULL";
        }
        
        String stringValue = value.toString();
        boolean stringLike = (value instanceof CharSequence) || (value instanceof ByteString);
        return stringLike ? "'" + stringValue + "'" : stringValue;
    }

}
