package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class SubstringExpression implements Expression {

    private final Expression inputExpression;

    private final Expression fromExpression;

    private final Expression forExpression;


    public SubstringExpression(Expression inputExpression, Expression fromExpression, Expression forExpression) {
        this.inputExpression = inputExpression;
        this.fromExpression = fromExpression;
        this.forExpression = forExpression;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public Expression fromExpression() {
        return fromExpression;
    }

    public Expression forExpression() {
        return forExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters().concat(fromExpression.parameters()).concat(forExpression.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return String.class;
    }

    @Override
    public boolean isNullable() {
        return inputExpression.isNullable() || fromExpression.isNullable() || forExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return
                inputExpression.isNullable(nullabilities) ||
                fromExpression.isNullable(nullabilities) ||
                forExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        String inputString = StringUtil.stringify(inputValue);
        int length = inputString.length();

        Object fromValue = fromExpression.evaluate(values);
        Integer fromInteger = (Integer) ConvertUtil.convert(fromValue, Integer.class);
        int fromZeroBased = fromInteger == null ? 0 : fromInteger - 1;
        if (fromZeroBased >= length) {
            return "";
        }

        Object forValue = forExpression.evaluate(values);
        Integer forInteger = (Integer) ConvertUtil.convert(forValue, Integer.class);
        int untilZeroBased = forInteger == null ? length : Math.min(length, fromZeroBased + forInteger);

        fromZeroBased = Math.max(0, fromZeroBased);
        if (untilZeroBased < fromZeroBased) {
            return "";
        }

        return inputString.substring(fromZeroBased, untilZeroBased);
    }

    @Override
    public String automaticName() {
        return "SUBSTRING(" + inputExpression.automaticName() +
                " FROM " + fromExpression.automaticName() +
                " FOR " + forExpression.automaticName() + ")";
    }

}
