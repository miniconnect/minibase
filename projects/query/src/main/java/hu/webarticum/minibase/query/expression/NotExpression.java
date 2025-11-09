package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BooleanUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class NotExpression implements Expression {
    
    private final Expression subExpression;
    

    public NotExpression(Expression subExpression) {
        this.subExpression = subExpression;
    }


    @Override
    public ImmutableList<Parameter> parameters() {
        return subExpression.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Boolean.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return Boolean.class;
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
        Boolean subBoolean = BooleanUtil.boolify(subValue);
        if (subBoolean == null) {
            return null;
        } else {
            return !subBoolean;
        }
    }

    @Override
    public String automaticName() {
        return "NOT " + subExpression.automaticName();
    }
    
}
