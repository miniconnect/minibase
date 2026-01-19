package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class SubstringExpression implements Expression {

    private final Expression inputExpression;

    private final Optional<Expression> fromExpression;

    private final Optional<Expression> forExpression;


    public SubstringExpression(
            Expression inputExpression,
            Optional<Expression> fromExpression,
            Optional<Expression> forExpression) {
        this.inputExpression = inputExpression;
        this.fromExpression = fromExpression;
        this.forExpression = forExpression;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public Optional<Expression> fromExpression() {
        return fromExpression;
    }

    public Optional<Expression> forExpression() {
        return forExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters()
                .concat(fromExpression.map(Expression::parameters).orElseGet(ImmutableList::empty))
                .concat(forExpression.map(Expression::parameters).orElseGet(ImmutableList::empty));
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
        return inputExpression.isNullable() ||
                fromExpression.map(Expression::isNullable).orElse(false) ||
                forExpression.map(Expression::isNullable).orElse(false);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return inputExpression.isNullable(nullabilities) ||
                fromExpression.map(e -> e.isNullable(nullabilities)).orElse(false) ||
                forExpression.map(e -> e.isNullable(nullabilities)).orElse(false);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        Object fromValue = evaluateOptionalExpression(fromExpression, values, 1);
        if (fromValue == null) {
            return null;
        }

        String inputString = StringUtil.stringify(inputValue);
        int length = inputString.length();
        int from = (Integer) ConvertUtil.convert(fromValue, Integer.class);
        int fromZeroBased = from - 1;

        Object forValue = evaluateOptionalExpression(forExpression, values, length - fromZeroBased);
        if (forValue == null) {
            return null;
        }

        int forInt = (Integer) ConvertUtil.convert(forValue, Integer.class);
        if (forInt <= 0) {
            return "";
        }

        if (fromZeroBased >= length) {
            return "";
        }

        int untilZeroBased = Math.min(length, fromZeroBased + forInt);
        if (untilZeroBased <= 0) {
            return "";
        }

        return inputString.substring(Math.max(0, fromZeroBased), untilZeroBased);
    }

    private Object evaluateOptionalExpression(
            Optional<Expression> optionalExpression, ImmutableMap<Parameter, Object> values, Object defaultValue) {
        if (!optionalExpression.isPresent()) {
            return defaultValue;
        }
        return optionalExpression.get().evaluate(values);
    }

    @Override
    public String automaticName() {
        return "SUBSTRING(" + inputExpression.automaticName() +
                fromExpression.map(e -> " FROM " + e.automaticName()).orElse("") +
                forExpression.map(e -> " FOR " + e.automaticName()).orElse("") + ")";
    }

}
