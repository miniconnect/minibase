package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class BetweenExpression implements Expression {
    
    private final Expression givenExpression;
    
    private final Expression minExpression;
    
    private final Expression maxExpression;
    
    
    public BetweenExpression(Expression givenExpression, Expression minExpression, Expression maxExpression) {
        this.givenExpression = givenExpression;
        this.minExpression = minExpression;
        this.maxExpression = maxExpression;
    }


    public Expression givenExpression() {
        return givenExpression;
    }
    
    public Expression minExpression() {
        return minExpression;
    }
    
    public Expression maxExpression() {
        return maxExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return givenExpression.parameters().concat(minExpression.parameters()).concat(maxExpression.parameters());
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
        return givenExpression.isNullable() || minExpression.isNullable() || maxExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return
            givenExpression.isNullable(nullabilities) ||
            minExpression.isNullable(nullabilities) ||
            maxExpression.isNullable(nullabilities);
    }
    
    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Expression subExpression1 = new OrderRelationExpression(
                OrderRelationExpression.Operation.LESS_EQ, minExpression, givenExpression);
        Expression subExpression2 = new OrderRelationExpression(
                OrderRelationExpression.Operation.LESS_EQ, givenExpression, maxExpression);
        Expression andExpression = new AndExpression(subExpression1, subExpression2);
        return andExpression.evaluate(values);
    }

    @Override
    public String automaticName() {
        return givenExpression.automaticName() + " BETWEEN " + minExpression.automaticName() + " AND " + maxExpression.automaticName();
    }
    
}
