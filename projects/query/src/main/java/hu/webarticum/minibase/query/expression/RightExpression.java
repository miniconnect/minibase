package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
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
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return String.class;
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

        String inputString = StringUtil.stringify(inputValue);
        int inputLength = inputString.length();
        int length = NumberUtil.asInt(lengthValue);

        if (length >= inputLength) {
            return inputString;
        }

        if (length < 0) {
            length = inputLength + length;
        }
        if (length <= 0) {
            return "";
        }

        return inputString.substring(inputLength - length);
    }

    @Override
    public String automaticName() {
        return "RIGHT(" + inputExpression.automaticName() + ", " + lengthExpression.automaticName() + ")";
    }

}
