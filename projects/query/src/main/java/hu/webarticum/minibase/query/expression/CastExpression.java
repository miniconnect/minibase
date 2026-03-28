package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class CastExpression implements Expression {

    private final Expression subjectOperand;

    private final TypeConstruct targetTypeConstruct;


    public CastExpression(Expression subjectOperand, TypeConstruct targetTypeConstruct) {
        this.subjectOperand = subjectOperand;
        this.targetTypeConstruct = targetTypeConstruct;
    }


    public Expression subjectOperand() {
        return subjectOperand;
    }

    public TypeConstruct targetTypeConstruct() {
        return targetTypeConstruct;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectOperand.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(targetTypeConstruct.symbol().type());
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return targetTypeConstruct.symbol().type();
    }

    @Override
    public boolean isNullable() {
        return targetTypeConstruct.symbol().type() == Void.class || subjectOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return targetTypeConstruct.symbol().type() == Void.class || subjectOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subjectValue = subjectOperand.evaluate(substitutions);
        Class<?> targetType = targetTypeConstruct.symbol().type();
        Integer size = targetTypeConstruct.size();
        Integer scale = targetTypeConstruct.scale();
        return ConvertUtil.convert(subjectValue, targetType, size, scale);
    }

    @Override
    public String automaticName() {
        return "CAST(" + subjectOperand.automaticName() + " AS " + targetTypeConstruct.symbol().name() + ")";
    }

}
