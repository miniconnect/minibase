package hu.webarticum.minibase.query.expression;

import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class ConstantExpression implements FixedTypeExpression {

    private final Object value;
    
    
    public ConstantExpression(Object value) {
        this.value = value;
    }
    
    
    @Override
    public Class<?> type() {
        if (value == null) {
            return Void.class;
        }
        
        return value.getClass();
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
