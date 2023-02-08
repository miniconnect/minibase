package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class SpecialValueExpression implements Expression {
    
    private final SpecialValueParameter specialValueParameter;
    

    public SpecialValueExpression(SpecialValueParameter specialValueParameter) {
        this.specialValueParameter = specialValueParameter;
    }

    
    public SpecialValueParameter specialValueParameter() {
        return specialValueParameter;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return ImmutableList.of(specialValueParameter);
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(specialValueParameter.type());
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return specialValueParameter.type();
    }
    
    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        return values.get(specialValueParameter);
    }
    
    @Override
    public String automaticName() {
        return specialValueParameter.name();
    }

}
