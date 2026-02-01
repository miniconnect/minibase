package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
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
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return String.class;
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

        String inputString = StringUtil.stringify(inputValue);
        if (inputString.isEmpty()) {
            return "";
        }

        int count = NumberUtil.asInt(countValue);

        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            resultBuilder.append(inputString);
        }
        return resultBuilder.toString();
    }

    @Override
    public String automaticName() {
        return "REPEAT(" + inputExpression.automaticName() + ", " + countExpression.automaticName() + ")";
    }

}
