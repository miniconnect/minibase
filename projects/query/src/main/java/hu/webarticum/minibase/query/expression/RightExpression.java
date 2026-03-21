package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RightExpression implements Expression {

    private final Expression inputExpression;

    private final Expression lengthExpression;


    public RightExpression(Expression inputExpression, Expression lengthExpression) {
        this.inputExpression = inputExpression;
        this.lengthExpression = lengthExpression;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public Expression lengthExpression() {
        return lengthExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters().concat(lengthExpression.parameters());
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
        return inputExpression.isNullable() || lengthExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return inputExpression.isNullable(nullabilities) || lengthExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        Object lengthValue = lengthExpression.evaluate(values);
        if (lengthValue == null) {
            return null;
        }
        int length = NumberUtil.asInt(lengthValue);

        if (inputValue instanceof ByteString) {
            return operate((ByteString) inputValue, length);
        } else if (inputValue instanceof BitString) {
            return operate((BitString) inputValue, length);
        } else {
            return operate(StringUtil.stringify(inputValue), length);
        }
    }

    private String operate(String input, int length) {
        int inputLength = input.length();
        if (length >= inputLength) {
            return input;
        }
        if (length < 0) {
            length = inputLength + length;
        }
        return length > 0 ? input.substring(inputLength - length) : "";
    }

    private ByteString operate(ByteString input, int length) {
        int inputLength = input.length();
        if (length >= inputLength) {
            return input;
        }
        if (length < 0) {
            length = inputLength + length;
        }
        return length > 0 ? input.substring(inputLength - length) : ByteString.empty();
    }

    private BitString operate(BitString input, int length) {
        int inputLength = input.length();
        if (length >= inputLength) {
            return input;
        }
        if (length < 0) {
            length = inputLength + length;
        }
        return length > 0 ? input.substring(inputLength - length) : BitString.empty();
    }

    @Override
    public String automaticName() {
        return "RIGHT(" + inputExpression.automaticName() + ", " + lengthExpression.automaticName() + ")";
    }

}
