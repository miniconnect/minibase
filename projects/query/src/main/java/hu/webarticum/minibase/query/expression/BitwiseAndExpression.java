package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BitwiseAndExpression implements Expression {

    private final Expression leftOperand;

    private final Expression rightOperand;


    public BitwiseAndExpression(Expression leftOperand, Expression rightOperand) {
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
        Class<?> leftType = leftOperand.type().orElse(null);
        if (leftType == null) {
            return Optional.empty();
        } else if (!Number.class.isAssignableFrom(leftType)) {
            return Optional.of(BitString.class);
        }
        Class<?> rightType = rightOperand.type().orElse(null);
        if (rightType == null) {
            return Optional.empty();
        } else if (!Number.class.isAssignableFrom(rightType)) {
            return Optional.of(BitString.class);
        }
        return Optional.of(LargeInteger.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> leftType = leftOperand.type(types);
        if (!Number.class.isAssignableFrom(leftType)) {
            return BitString.class;
        }
        Class<?> rightType = rightOperand.type(types);
        if (!Number.class.isAssignableFrom(rightType)) {
            return BitString.class;
        }
        return LargeInteger.class;
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
        if (leftValue == null) {
            return null;
        }
        Object rightValue = rightOperand.evaluate(values);
        if (rightValue == null) {
            return null;
        }
        boolean leftIsNumber = leftValue instanceof Number;
        boolean rightIsNumber = rightValue instanceof Number;
        if (leftIsNumber && rightIsNumber) {
            LargeInteger leftLargeIntegerValue = (LargeInteger) ConvertUtil.convert(leftValue, LargeInteger.class);
            LargeInteger rightLargeIntegerValue = (LargeInteger) ConvertUtil.convert(rightValue, LargeInteger.class);
            return leftLargeIntegerValue.and(rightLargeIntegerValue);
        }

        BitString leftBitString = (BitString) ConvertUtil.convert(leftValue, BitString.class);
        BitString rightBitString = (BitString) ConvertUtil.convert(rightValue, BitString.class);
        if (leftIsNumber || rightIsNumber) {
            int coveringLength = Math.max(leftBitString.size(), rightBitString.size());
            leftBitString = leftBitString.padLeft(coveringLength);
            rightBitString = rightBitString.padLeft(coveringLength);
        }
        return leftBitString.and(rightBitString);
    }

    @Override
    public String automaticName() {
        return leftOperand.automaticName() + " & " + rightOperand.automaticName();
    }

}
