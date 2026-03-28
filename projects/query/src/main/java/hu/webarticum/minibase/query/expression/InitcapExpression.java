package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class InitcapExpression implements Expression {

    private final Expression subExpression;


    public InitcapExpression(Expression subExpression) {
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
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> values) {
        return String.class;
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
        Object value = subExpression.evaluate(values);
        if (value == null) {
            return null;
        }

        String input = StringUtil.stringify(value);
        int length = input.length();
        StringBuilder resultBuilder = new StringBuilder();
        boolean wasLetter = false;
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                resultBuilder.append(c);
                wasLetter = false;
            } else if (wasLetter) {
                resultBuilder.append(Character.toLowerCase(c));
            } else {
                resultBuilder.append(Character.toUpperCase(c));
                wasLetter = true;
            }
        }
        return resultBuilder.toString();
    }

    @Override
    public String automaticName() {
        return "INITCAP(" + subExpression.automaticName() + ")";
    }

}
