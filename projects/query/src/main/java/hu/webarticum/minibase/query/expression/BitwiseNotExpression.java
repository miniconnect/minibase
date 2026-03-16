package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BitwiseNotExpression implements Expression {

    private final Expression subExpression;


    public BitwiseNotExpression(Expression subExpression) {
        this.subExpression = subExpression;
    }


    public Expression subExpression() {
        return subExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subExpression.parameters();
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> subType = subExpression.type().orElse(null);
        if (subType == null) {
            return Optional.empty();
        } else if (Number.class.isAssignableFrom(subType)) {
            return Optional.of(LargeInteger.class);
        } else {
            return Optional.of(BitString.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> types) {
        Class<?> subType = subExpression.type(types);
        if (Number.class.isAssignableFrom(subType)) {
            return subType;
        } else {
            return BitString.class;
        }
    }

    @Override
    public boolean isNullable() {
        return subExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return subExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object subValue = subExpression.evaluate(values);
        if (subValue == null) {
            return null;
        } else if (subValue instanceof Number) {
            LargeInteger largeIntegerValue = (LargeInteger) ConvertUtil.convert(subValue, LargeInteger.class);
            return largeIntegerValue.not();
        }

        BitString bitStringValue = (BitString) ConvertUtil.convert(subValue, BitString.class);
        return bitStringValue.not();
    }

    @Override
    public String automaticName() {
        return "~" + subExpression.automaticName();
    }

}
