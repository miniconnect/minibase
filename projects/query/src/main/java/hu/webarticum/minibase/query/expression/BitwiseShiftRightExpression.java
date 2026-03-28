package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BitwiseShiftRightExpression implements Expression {

    private final Expression subjectOperand;

    private final Expression shiftOperand;


    public BitwiseShiftRightExpression(Expression subjectOperand, Expression shiftOperand) {
        this.subjectOperand = subjectOperand;
        this.shiftOperand = shiftOperand;
    }


    public Expression subjectOperand() {
        return subjectOperand;
    }

    public Expression shiftOperand() {
        return shiftOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectOperand.parameters().concat(shiftOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> subjectType = subjectOperand.type().orElse(null);
        if (subjectType == null) {
            return Optional.empty();
        } else if (subjectType == Void.class || Number.class.isAssignableFrom(subjectType)) {
            return Optional.of(LargeInteger.class);
        } else {
            return Optional.of(BitString.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        Class<?> subjectType = subjectOperand.type(typeSubstitutions);
        if (Number.class.isAssignableFrom(subjectType)) {
            return subjectType;
        } else {
            return BitString.class;
        }
    }

    @Override
    public boolean isNullable() {
        return subjectOperand.isNullable() || shiftOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return subjectOperand.isNullable(nullabilitySubstitutions) || shiftOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subjectValue = subjectOperand.evaluate(substitutions);
        if (subjectValue == null) {
            return null;
        }
        Object shiftValue = shiftOperand.evaluate(substitutions);
        if (shiftValue == null) {
            return null;
        }
        LargeInteger rawShift = (LargeInteger) ConvertUtil.convert(shiftValue, LargeInteger.class);
        boolean isShiftOut = rawShift.isNegative() || !rawShift.isFittingInInt();
        int shift = rawShift.intValue();
        if (subjectValue instanceof Number) {
            LargeInteger largeIntegerValue = (LargeInteger) ConvertUtil.convert(subjectValue, LargeInteger.class);
            return isShiftOut ? 0 : largeIntegerValue.shiftRight(shift);
        }

        BitString bitStringValue = bitStringify(subjectValue);
        return isShiftOut ? BitString.empty().resize(bitStringValue.length()) : bitStringValue.shiftRight(shift);
    }

    private BitString bitStringify(Object value) {
        Object bitsValue = value instanceof String ? ByteString.of((String) value) : value;
        return BitStringUtil.bitStringify(bitsValue);
    }

    @Override
    public String automaticName() {
        return subjectOperand.automaticName() + " >> " + shiftOperand.automaticName();
    }

}
