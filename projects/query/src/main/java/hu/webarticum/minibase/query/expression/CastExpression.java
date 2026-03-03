package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class CastExpression implements Expression {

    private final Expression subExpression;

    private final TypeConstruct targetTypeConstruct;


    public CastExpression(Expression subExpression, TypeConstruct targetTypeConstruct) {
        this.subExpression = subExpression;
        this.targetTypeConstruct = targetTypeConstruct;
    }


    public Expression subExpression() {
        return subExpression;
    }

    public TypeConstruct targetTypeConstruct() {
        return targetTypeConstruct;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subExpression.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(targetTypeConstruct.symbol().type());
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return targetTypeConstruct.symbol().type();
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
        Class<?> targetType = targetTypeConstruct.symbol().type();
        Integer size = targetTypeConstruct.size();
        Integer scale = targetTypeConstruct.scale();
        return ConvertUtil.convert(subValue, targetType, size, scale);
    }

    @Override
    public String automaticName() {
        return "CAST(" + subExpression.automaticName() + " AS " + targetTypeConstruct.symbol().name() + ")";
    }

}
