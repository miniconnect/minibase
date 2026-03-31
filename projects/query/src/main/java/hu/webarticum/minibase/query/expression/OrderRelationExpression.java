package hu.webarticum.minibase.query.expression;

import java.util.Optional;
import java.util.function.IntPredicate;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.minibase.query.util.UnifyUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class OrderRelationExpression implements Expression {

    public enum Operation {

        LESS(cmp -> cmp < 0),
        LEQ(cmp -> cmp <= 0),
        GREATER(cmp -> cmp > 0),
        GEQ(cmp -> cmp >= 0),
        ;

        private final IntPredicate cmpPredicate;

        private Operation(IntPredicate cmpPredicate) {
            this.cmpPredicate = cmpPredicate;
        }

        public boolean testCmp(int cmp) {
            return cmpPredicate.test(cmp);
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
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return Boolean.class;
    }

    @Override
    public boolean isNullable() {
        return leftOperand.isNullable() || rightOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return leftOperand.isNullable(nullabilitySubstitutions) || rightOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object leftValue = leftOperand.evaluate(substitutions);
        Object rightValue = rightOperand.evaluate(substitutions);
        if (leftValue == null || rightValue == null) {
            return null;
        }

        Class<?> commonType = UnifyUtil.unifyTypes(leftValue.getClass(), rightValue.getClass());
        if (commonType == null) {
            return compare(StringUtil.stringify(leftValue), StringUtil.stringify(rightValue));
        }

        Object convertedLeftValue;
        Object convertedRightValue;
        try {
            convertedLeftValue = ConvertUtil.convert(leftValue, commonType);
            convertedRightValue = ConvertUtil.convert(rightValue, commonType);
        } catch (IllegalArgumentException e) {
        return compare(StringUtil.stringify(leftValue), StringUtil.stringify(rightValue));
        }

        if (!(convertedLeftValue instanceof Comparable)) {
            return compare(StringUtil.stringify(leftValue), StringUtil.stringify(rightValue));
        }

        return compare(convertedLeftValue, convertedRightValue);
    }

    private boolean compare(Object value1, Object value2) {
        @SuppressWarnings("unchecked")
        int cmp = ((Comparable<Object>) value1).compareTo(value2);
        return operation.testCmp(cmp);
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_" + operation.name().toLowerCase() + "_" + columnIndex;
    }

}
