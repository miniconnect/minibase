package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.ConvertUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
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
        Class<?> inputType = inputExpression.type().orElse(null);
        if (inputType == null || inputType == ByteString.class || inputType == BitString.class) {
            return Optional.ofNullable(inputType);
        } else {
            return Optional.of(String.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        Class<?> inputType = inputExpression.type(values);
        if (inputType == ByteString.class || inputType == BitString.class) {
            return inputType;
        } else {
            return String.class;
        }
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

        int from;
        if (fromExpression.isPresent()) {
            Object fromValue = fromExpression.get().evaluate(values);
            if (fromValue == null) {
                return null;
            }
            from = (Integer) ConvertUtil.convert(fromValue, Integer.class) - 1;
        } else {
            from = 0;
        }

        Object forValue = null;
        if (forExpression.isPresent()) {
            forValue = forExpression.get().evaluate(values);
            if (forValue == null) {
                return null;
            }
        }
        Integer length = (Integer) ConvertUtil.convert(forValue, Integer.class);

        if (inputValue instanceof ByteString) {
            return operate((ByteString) inputValue, from, length);
        } else if (inputValue instanceof BitString) {
            return operate((BitString) inputValue, from, length);
        } else {
            return operate(StringUtil.stringify(inputValue), from, length);
        }
    }

    private String operate(String input, int from, Integer length) {
        int[] slice = calculateSlice(input.length(), from, length);
        return input.substring(slice[0], slice[1]);
    }

    private ByteString operate(ByteString input, int from, Integer length) {
        int[] slice = calculateSlice(input.length(), from, length);
        return input.substring(slice[0], slice[1]);
    }

    private BitString operate(BitString input, int from, Integer length) {
        int[] slice = calculateSlice(input.length(), from, length);
        return input.substring(slice[0], slice[1]);
    }

    private int[] calculateSlice(int inputLength, int from, Integer sliceLength) {
        if (from >= inputLength) {
            return new int[] { inputLength, inputLength };
        }
        if (sliceLength == null) {
            return new int[] { Math.max(0, from), inputLength };
        } else if (sliceLength < 0) {
            return new int[] { 0, 0 };
        }
        int until = from + sliceLength;
        if (until < 0) {
            return new int[] { 0, 0 };
        } else {
            return new int[] { Math.max(0, from), Math.min(inputLength, until) };
        }
    }

    @Override
    public String automaticName() {
        return "SUBSTRING(" + inputExpression.automaticName() +
                fromExpression.map(e -> " FROM " + e.automaticName()).orElse("") +
                forExpression.map(e -> " FOR " + e.automaticName()).orElse("") + ")";
    }

}
