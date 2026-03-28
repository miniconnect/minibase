package hu.webarticum.minibase.query.expression;

import java.util.Objects;
import java.util.Optional;

import hu.webarticum.minibase.query.util.ValueUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class NullifExpression implements Expression {

    private final Expression subjectOperand;

    private final Expression checkOperand;


    public NullifExpression(Expression subjectOperand, Expression checkOperand) {
        this.subjectOperand = subjectOperand;
        this.checkOperand = checkOperand;
    }


    public Expression subjectOperand() {
        return subjectOperand;
    }

    public Expression secondExpression() {
        return checkOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectOperand.parameters().concat(checkOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return subjectOperand.type();
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return subjectOperand.type(typeSubstitutions);
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return true;
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subjectValue = subjectOperand.evaluate(substitutions);
        Object checkValue = checkOperand.evaluate(substitutions);
        Boolean areEqual = ValueUtil.evalEquality(subjectValue, checkValue);
        return Objects.equals(areEqual, true) ? null : subjectValue;
    }

    @Override
    public String automaticName() {
        return "NULLIF(" + subjectOperand.automaticName() + ", " + checkOperand.automaticName() + ")";
    }

}
