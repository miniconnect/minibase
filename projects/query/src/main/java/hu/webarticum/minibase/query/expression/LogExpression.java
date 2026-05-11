package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class LogExpression implements Expression {

    private final Expression baseOperand;

    private final Expression subjectOperand;


    public LogExpression(Expression baseOperand, Expression subjectOperand) {
        this.baseOperand = baseOperand;
        this.subjectOperand = subjectOperand;
    }


    public Expression baseOperand() {
        return baseOperand;
    }

    public Expression subjectOperand() {
        return subjectOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectOperand.parameters().concat(baseOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Double.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return Double.class;
    }

    @Override
    public boolean isNullable() {
        return baseOperand.isNullable() || subjectOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return baseOperand.isNullable(nullabilitySubstitutions) || subjectOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object baseValue = baseOperand.evaluate(substitutions);
        if (baseValue == null) {
            return null;
        }
        Object subjectValue = subjectOperand.evaluate(substitutions);
        if (subjectValue == null) {
            return null;
        }

        double base = (Double) ConvertUtil.convert(baseValue, Double.class);
        double subject = (Double) ConvertUtil.convert(subjectValue, Double.class);
        return Math.log(subject) / Math.log(base);
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_log_col" + columnIndex;
    }

}
