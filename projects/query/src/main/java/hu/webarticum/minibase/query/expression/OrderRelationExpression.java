package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
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
    
    // TODO: support non-numbers (e.g. temporal values)
    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object leftValue = leftOperand.evaluate(values);
        Object rightValue = rightOperand.evaluate(values);
        if (leftValue == null || rightValue == null) {
            return null;
        }
        
        if (
            leftValue instanceof Number ||
            leftValue instanceof Boolean ||
            rightValue instanceof Number ||
            rightValue instanceof Boolean) {
                Number[] unifiedNumbers = NumberUtil.unify(leftValue, rightValue);
                return compare(unifiedNumbers[0], unifiedNumbers[1]);
        } else if (leftValue instanceof Comparable && leftValue.getClass() == rightValue.getClass()) {
            return compare(leftValue, rightValue);
        } else {
            return compare(leftValue.toString(), rightValue.toString());
        }
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
