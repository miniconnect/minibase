package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BooleanUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class AndExpression implements Expression {

    private final Expression leftOperand;

    private final Expression rightOperand;


    public AndExpression(Expression leftOperand, Expression rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }


    public Expression leftOperand() {
        return leftOperand;
    }

    public Expression rightOperand() {
        return rightOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return leftOperand.parameters().concat(rightOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.empty();
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        return leftOperand.isNullable() || rightOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return leftOperand.isNullable(nullabilities) || rightOperand.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Boolean leftValue = BooleanUtil.boolify(leftOperand.evaluate(values));
        boolean leftIsNull = (leftValue == null);
        if (!leftIsNull && leftValue == false) {
            return false;
        }

        Boolean rightValue = BooleanUtil.boolify(rightOperand.evaluate(values));
        boolean rightIsNull = (rightValue == null);
        if (!rightIsNull && rightValue == false) {
            return false;
        }

        if (leftIsNull || rightIsNull) {
            return null;
        }

        return true;
    }

    @Override
    public String automaticName() {
        return leftOperand.automaticName() + " AND " + rightOperand.automaticName();
    }

}
