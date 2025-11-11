package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class CastExpression implements Expression {
    
    private final Expression subExpression;

    private final TypeConstruct targetType;
    

    public CastExpression(Expression subExpression, TypeConstruct targetType) {
        this.subExpression = subExpression;
        this.targetType = targetType;
    }


    @Override
    public ImmutableList<Parameter> parameters() {
        return subExpression.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        
        // FIXME
        //return Optional.of(targetType.symbol().type());
        return Optional.of(String.class);

    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        
        // FIXME
        //return targetType.symbol().type();
        return String.class;

    }
    
    @Override
    public boolean isNullable() {
        return subExpression.isNullable();
    }
    
    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return subExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object subValue = subExpression.evaluate(values);
        if (subValue == null) {
            return null;
        }

        // TODO
        return subValue + " --> " + targetType.symbol().name() + "(" + targetType.size() + ", " + targetType.scale() + ")";

    }

    @Override
    public String automaticName() {
        return "CAST(" + subExpression.automaticName() + " AS " + targetType.symbol().name() + ")";
    }
    
}
