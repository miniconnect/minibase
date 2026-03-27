package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RepeatExpression implements Expression {

    private final Expression inputExpression;

    private final Expression countExpression;


    public RepeatExpression(Expression inputExpression, Expression countExpression) {
        this.inputExpression = inputExpression;
        this.countExpression = countExpression;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public Expression countExpression() {
        return countExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters().concat(countExpression.parameters());
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
        return inputExpression.isNullable() || countExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return inputExpression.isNullable(nullabilities) || countExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        Object countValue = countExpression.evaluate(values);
        if (countValue == null) {
            return null;
        }

        int count = NumberUtil.asInt(countValue);
        if (inputValue instanceof ByteString) {
            return operate((ByteString) inputValue, count);
        } else if (inputValue instanceof BitString) {
            return operate((BitString) inputValue, count);
        } else {
            return operate(StringUtil.stringify(inputValue), count);
        }
    }

    private String operate(String input, int count) {
        if (input.isEmpty()) {
            return "";
        }

        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            resultBuilder.append(input);
        }
        return resultBuilder.toString();
    }

    private ByteString operate(ByteString input, int count) {
        if (input.isEmpty()) {
            return ByteString.empty();
        }

        ByteString.Builder resultBuilder = ByteString.builder();
        for (int i = 0; i < count; i++) {
            resultBuilder.append(input);
        }
        return resultBuilder.build();
    }

    private BitString operate(BitString input, int count) {
        if (input.isEmpty()) {
            return BitString.empty();
        }

        BitString.Builder resultBuilder = BitString.builder();
        for (int i = 0; i < count; i++) {
            resultBuilder.append(input);
        }
        return resultBuilder.build();
    }

    @Override
    public String automaticName() {
        return "REPEAT(" + inputExpression.automaticName() + ", " + countExpression.automaticName() + ")";
    }

}
