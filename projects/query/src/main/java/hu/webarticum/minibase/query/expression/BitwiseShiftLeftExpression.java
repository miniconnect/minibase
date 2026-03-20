package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.BitStringUtil;
import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BitwiseShiftLeftExpression implements Expression {

    private final Expression baseExpression;

    private final Expression shiftExpression;


    public BitwiseShiftLeftExpression(Expression baseExpression, Expression shiftExpression) {
        this.baseExpression = baseExpression;
        this.shiftExpression = shiftExpression;
    }


    public Expression baseExpression() {
        return baseExpression;
    }

    public Expression rightOperand() {
        return shiftExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return baseExpression.parameters().concat(shiftExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> baseType = baseExpression.type().orElse(null);
        if (baseType == null) {
            return Optional.empty();
        } else if (baseType == Void.class || Number.class.isAssignableFrom(baseType)) {
            return Optional.of(LargeInteger.class);
        } else {
            return Optional.of(BitString.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> baseType = baseExpression.type(types);
        if (Number.class.isAssignableFrom(baseType)) {
            return baseType;
        } else {
            return BitString.class;
        }
    }

    @Override
    public boolean isNullable() {
        return baseExpression.isNullable() || shiftExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return baseExpression.isNullable(nullabilities) || shiftExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object baseValue = baseExpression.evaluate(values);
        if (baseValue == null) {
            return null;
        }
        Object shiftValue = shiftExpression.evaluate(values);
        if (shiftValue == null) {
            return null;
        }
        LargeInteger rawShift = (LargeInteger) ConvertUtil.convert(shiftValue, LargeInteger.class);
        boolean isShiftOut = rawShift.isNegative() || !rawShift.isFittingInInt();
        int shift = rawShift.intValue();
        if (baseValue instanceof Number) {
            LargeInteger largeIntegerValue = (LargeInteger) ConvertUtil.convert(baseValue, LargeInteger.class);
            return isShiftOut ? 0 : largeIntegerValue.shiftLeft(shift);
        }

        BitString bitStringValue = bitStringify(baseValue);
        return isShiftOut ? BitString.empty().resize(bitStringValue.length()) : bitStringValue.shiftLeft(shift);
    }

    private BitString bitStringify(Object value) {
        Object bitsValue = value instanceof String ? ByteString.of((String) value) : value;
        return BitStringUtil.bitStringify(bitsValue);
    }

    @Override
    public String automaticName() {
        return baseExpression.automaticName() + " << " + shiftExpression.automaticName();
    }

}
