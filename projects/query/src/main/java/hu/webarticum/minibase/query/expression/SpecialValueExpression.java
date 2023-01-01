package hu.webarticum.minibase.query.expression;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class SpecialValueExpression implements FixedTypeExpression {
    
    private final SpecialValueParameter specialValueParameter;
    

    public SpecialValueExpression(SpecialValueParameter specialValueParameter) {
        this.specialValueParameter = specialValueParameter;
    }

    
    public SpecialValueParameter specialValueParameter() {
        return specialValueParameter;
    }

    @Override
    public Class<?> type() {
        return specialValueParameter.type();
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return ImmutableList.of(specialValueParameter);
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
