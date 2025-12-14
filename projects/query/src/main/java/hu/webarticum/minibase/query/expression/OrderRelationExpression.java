package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class OrderRelationExpression implements Expression {

    public enum Operation {

        LESS("<"), LESS_EQ("<="), GREATER(">"), GREATER_EQ(">=");

        private final String operator;

        private Operation(String operator) {
            this.operator = operator;
        }

        public String operator() {
            return operator;
        }

    }


    private final Operation operation;

    private final Expression leftOperand;

    private final Expression rightOperand;


    public OrderRelationExpression(Operation operation, Expression leftOperand, Expression rightOperand) {
        this.operation = operation;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }


    public Operation operation() {
        return operation;
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
        return Optional.of(Boolean.class);
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
        Object leftValue = leftOperand.evaluate(values);
        Object rightValue = rightOperand.evaluate(values);
        if (leftValue == null || rightValue == null) {
            return null;
        }

        Class<?> commonType = UnifyUtil.unifyTypes(leftValue.getClass(), rightValue.getClass());
        if (commonType == null) {
            return compare(leftValue.toString(), rightValue.toString());
        }

        Object convertedLeftValue;
        Object convertedRightValue;
        try {
            convertedLeftValue = ConvertUtil.convert(leftValue, commonType);
            convertedRightValue = ConvertUtil.convert(rightValue, commonType);
        } catch (IllegalArgumentException e) {
            return compare(leftValue.toString(), rightValue.toString());
        }

        if (!(convertedLeftValue instanceof Comparable)) {
            return compare(leftValue.toString(), rightValue.toString());
        }

        return compare(convertedLeftValue, convertedRightValue);
    }

    private boolean compare(Object value1, Object value2) {
        @SuppressWarnings("unchecked")
        int cmp = ((Comparable<Object>) value1).compareTo(value2);
        switch (operation) {
            case LESS:
                return cmp < 0;
            case LESS_EQ:
                return cmp <= 0;
            case GREATER:
                return cmp > 0;
            case GREATER_EQ:
            default:
                return cmp >= 0;
        }
    }

    @Override
    public String automaticName() {
        return leftOperand.automaticName() + " " + operation.operator() + " " + rightOperand.automaticName();
    }

}
