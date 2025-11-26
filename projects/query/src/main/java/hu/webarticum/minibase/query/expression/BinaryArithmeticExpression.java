package hu.webarticum.minibase.query.expression;

import java.math.BigDecimal;
import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BinaryArithmeticExpression implements Expression {
    
    public enum Operation {
        
        MUL("*"), DIV("DIV"), MOD("%"), RAT("/");
        
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
        Optional<Class<?>> leftType = leftOperand.type();
        Optional<Class<?>> rightType = rightOperand.type();
        if (!leftType.isPresent() || !rightType.isPresent()) {
            return Optional.empty();
        }
        Class<?> leftNumericType = NumberUtil.numberifyType(leftType.get());
        Class<?> rightNumericType = NumberUtil.numberifyType(rightType.get());
        if (leftNumericType == Void.class || rightNumericType == Void.class) {
            return Optional.of(Void.class);
        } else if (operation == Operation.RAT) {
            return Optional.of(Double.class);
        } else if (leftNumericType == Double.class || rightNumericType == Double.class) {
            return Optional.of(Double.class);
        } else if (leftNumericType == BigDecimal.class || rightNumericType == BigDecimal.class) {
            return Optional.of(BigDecimal.class);
        } else if (leftNumericType == LargeInteger.class || rightNumericType == LargeInteger.class) {
            return Optional.of(LargeInteger.class);
        } else {
            throw new IllegalArgumentException("Type detection failed");
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> leftNumericType = NumberUtil.numberifyType(leftOperand.type(types));
        Class<?> rightNumericType = NumberUtil.numberifyType(rightOperand.type(types));
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
    public boolean isNullable() {
        return leftOperand.isNullable() || rightOperand.isNullable() || canBeDivisionByZero();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return leftOperand.isNullable(nullabilities) || rightOperand.isNullable(nullabilities) || canBeDivisionByZero();
    }

    private boolean canBeDivisionByZero() {
        if (!isDivision(operation)) {
            return false;
        }
        
        ImmutableList<Parameter> rightParameters = rightOperand.parameters();
        if (!rightParameters.isEmpty()) {
            return true;
        }

        return isZero(NumberUtil.numberify(rightOperand.evaluate(ImmutableMap.empty())));
    }
    
    // TODO: support non-numbers (e.g. temporal values)
    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object leftValue = NumberUtil.numberify(leftOperand.evaluate(values));
        Object rightValue = NumberUtil.numberify(rightOperand.evaluate(values));
        if (leftValue == null || rightValue == null) {
            return null;
        } else if (isDivision(operation) && isZero(rightValue)) {
            // TODO: raise SQL warning
            return null;
        } else if (operation == Operation.RAT) {
            double leftDouble = (Double) NumberUtil.promote(leftValue, Double.class);
            double rightDouble = (Double) NumberUtil.promote(rightValue, Double.class);
            return leftDouble / rightDouble;
        } else if (leftValue instanceof Double || rightValue instanceof Double) {
            double leftDouble = (Double) NumberUtil.promote(leftValue, Double.class);
            double rightDouble = (Double) NumberUtil.promote(rightValue, Double.class);
            return operate(leftDouble, rightDouble);
        } else if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            BigDecimal leftBigDecimal = (BigDecimal) NumberUtil.promote(leftValue, BigDecimal.class);
            BigDecimal rightBigDecimal = (BigDecimal) NumberUtil.promote(rightValue, BigDecimal.class);
            return operate(leftBigDecimal, rightBigDecimal);
        } else if (leftValue instanceof LargeInteger || rightValue instanceof LargeInteger) {
            LargeInteger leftLargeInteger = (LargeInteger) NumberUtil.promote(leftValue, LargeInteger.class);
            LargeInteger rightLargeInteger = (LargeInteger) NumberUtil.promote(rightValue, LargeInteger.class);
            return operate(leftLargeInteger, rightLargeInteger);
        } else {
            throw new IllegalArgumentException("Operation failed");
        }
    }
    
    private boolean isDivision(Operation operation) {
        return operation == Operation.DIV || operation == Operation.MOD || operation == Operation.RAT;
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
            case MUL: return left * right;
            case DIV: return Math.ceil(left / right);
            case MOD: return left - (Math.ceil(left / right) * right);
            default: throw new IllegalArgumentException("Invalid operation");
        }
    }

    private BigDecimal operate(BigDecimal left, BigDecimal right) {
        switch (operation) {
            case MUL: return left.multiply(right);
            case DIV: return left.divideToIntegralValue(right);
            case MOD: return left.remainder(right);
            default: throw new IllegalArgumentException("Invalid operation");
        }
    }

    private LargeInteger operate(LargeInteger left, LargeInteger right) {
        switch (operation) {
            case MUL: return left.multiply(right);
            case DIV: return left.divide(right);
            case MOD: return left.remainder(right);
            default: throw new IllegalArgumentException("Invalid operation");
        }
    }

    @Override
    public String automaticName() {
        return leftOperand.automaticName() + " " + operation.operator() + " " + rightOperand.automaticName();
    }
    
}
