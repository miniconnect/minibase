package hu.webarticum.minibase.query.expression;

import hu.webarticum.miniconnect.lang.LargeInteger;

public enum SpecialValueParameter implements Parameter {

    CURRENT_USER(String.class),
    
    CURRENT_SCHEMA(String.class),
    
    CURRENT_CATALOG(String.class),
    
    READONLY(Boolean.class),
    
    AUTOCOMMIT(Boolean.class),
    
    LAST_INSERT_ID(LargeInteger.class),
    
    ;
    
    
    private final Class<?> type;
    
    private SpecialValueParameter(Class<?> type) {
        this.type = type;
    }

    
    public Class<?> type() {
        return type;
    }
    
}
