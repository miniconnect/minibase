package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class BetweenExpression implements Expression {

    private final Expression subjectOperand;

    private final Expression minOperand;

    private final Expression maxOperand;


    public BetweenExpression(Expression subjectOperand, Expression minOperand, Expression maxOperand) {
        this.subjectOperand = subjectOperand;
        this.minOperand = minOperand;
        this.maxOperand = maxOperand;
    }


    public Expression subjectOperand() {
        return subjectOperand;
    }

    public Expression minOperand() {
        return minOperand;
    }

    public Expression maxOperand() {
        return maxOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectOperand.parameters().concat(minOperand.parameters()).concat(maxOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(Boolean.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        return subjectOperand.isNullable() || minOperand.isNullable() || maxOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return
                subjectOperand.isNullable(nullabilitySubstitutions) ||
                minOperand.isNullable(nullabilitySubstitutions) ||
                maxOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Expression subExpression1 = new OrderRelationExpression(
                OrderRelationExpression.Operation.LESS_EQ, minOperand, subjectOperand);
        Expression subExpression2 = new OrderRelationExpression(
                OrderRelationExpression.Operation.LESS_EQ, subjectOperand, maxOperand);
        Expression andExpression = new AndExpression(subExpression1, subExpression2);
        return andExpression.evaluate(substitutions);
    }

    @Override
    public String automaticName() {
        return subjectOperand.automaticName() + " BETWEEN " + minOperand.automaticName() + " AND " + maxOperand.automaticName();
    }

}
