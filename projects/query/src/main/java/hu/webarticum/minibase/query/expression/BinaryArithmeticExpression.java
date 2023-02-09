package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberParser;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BinaryArithmeticExpression implements Expression {
    
    public enum Operation {
        
        ADD("+"), SUB("-"), MUL("*"), DIV("DIV"), MOD("%"), RAT("/");
        
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
    
    
    public BinaryArithmeticExpression(Operation operation, Expression leftOperand, Expression rightOperand) {
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
        return Optional.empty();
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> leftNumericType = NumberParser.numberifyType(leftOperand.type(types));
        Class<?> rightNumericType = NumberParser.numberifyType(rightOperand.type(types));
        if (leftNumericType == Void.class || rightNumericType == Void.class) {
            return Void.class;
        } else if (operation == Operation.RAT) {
            return Double.class;
        } else if (leftNumericType == Double.class || rightNumericType == Double.class) {
            return Double.class;
        } else if (leftNumericType == BigDecimal.class || rightNumericType == BigDecimal.class) {
            return BigDecimal.class;
        } else if (leftNumericType == LargeInteger.class || rightNumericType == LargeInteger.class) {
            return LargeInteger.class;
        } else {
            throw new IllegalArgumentException("Type detection failed");
        }
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object leftValue = NumberParser.numberify(leftOperand.evaluate(values));
        Object rightValue = NumberParser.numberify(rightOperand.evaluate(values));
        if (leftValue == null || rightValue == null) {
            return null;
        } else if ((operation == Operation.RAT || operation == Operation.DIV) && isZero(rightValue)) {
            // TODO: raise SQL warning
            return null;
        } else if (operation == Operation.RAT) {
            double leftDouble = (Double) NumberParser.promote(leftValue, Double.class);
            double rightDouble = (Double) NumberParser.promote(rightValue, Double.class);
            return leftDouble / rightDouble;
        } else if (leftValue instanceof Double || rightValue instanceof Double) {
            double leftDouble = (Double) NumberParser.promote(leftValue, Double.class);
            double rightDouble = (Double) NumberParser.promote(rightValue, Double.class);
            return operate(leftDouble, rightDouble);
        } else if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            BigDecimal leftBigDecimal = (BigDecimal) NumberParser.promote(leftValue, BigDecimal.class);
            BigDecimal rightBigDecimal = (BigDecimal) NumberParser.promote(rightValue, BigDecimal.class);
            return operate(leftBigDecimal, rightBigDecimal);
        } else if (leftValue instanceof LargeInteger || rightValue instanceof LargeInteger) {
            LargeInteger leftLargeInteger = (LargeInteger) NumberParser.promote(leftValue, LargeInteger.class);
            LargeInteger rightLargeInteger = (LargeInteger) NumberParser.promote(rightValue, LargeInteger.class);
            return operate(leftLargeInteger, rightLargeInteger);
        } else {
            throw new IllegalArgumentException("Operation failed");
        }
    }
    
    private boolean isZero(Object convertedValue) {
        if (convertedValue instanceof LargeInteger) {
            return ((LargeInteger) convertedValue).equals(LargeInteger.ZERO);
        } else if (convertedValue instanceof BigDecimal) {
            return ((BigDecimal) convertedValue).equals(BigDecimal.ZERO);
        } else if (convertedValue instanceof Double) {
            return ((Double) convertedValue) == 0.0d;
        } else {
            return false;
        }
    }
    
    private double operate(double left, double right) {
        switch (operation) {
            case ADD: return left + right;
            case SUB: return left - right;
            case MUL: return left * right;
            case DIV: return Math.ceil(left / right);
            case MOD: return left - (Math.ceil(left / right) * right);
            default: throw new IllegalArgumentException("Invalid operation");
        }
    }

    private BigDecimal operate(BigDecimal left, BigDecimal right) {
        switch (operation) {
            case ADD: return left.add(right);
            case SUB: return left.subtract(right);
            case MUL: return left.multiply(right);
            case DIV: return left.divideToIntegralValue(right);
            case MOD: return left.remainder(right);
            default: throw new IllegalArgumentException("Invalid operation");
        }
    }

    private LargeInteger operate(LargeInteger left, LargeInteger right) {
        switch (operation) {
            case ADD: return left.add(right);
            case SUB: return left.subtract(right);
            case MUL: return left.multiply(right);
            case DIV: return left.divide(right);
            case MOD: return left.remainder(right);
            default: throw new IllegalArgumentException("Invalid operation");
        }
    }

    @Override
    public String automaticName() {
        return leftOperand.automaticName() + "_" + operation.operator() + "_" + rightOperand.automaticName();
    }
    
}
