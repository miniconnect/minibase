package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class TranslateExpression implements Expression {

    private final Expression contextExpression;

    private final Expression fromCharsExpression;

    private final Expression toCharsExpression;


    public TranslateExpression(Expression contextExpression, Expression fromCharsExpression, Expression toCharsExpression) {
        this.contextExpression = contextExpression;
        this.fromCharsExpression = fromCharsExpression;
        this.toCharsExpression = toCharsExpression;
    }


    public Expression contextExpression() {
        return contextExpression;
    }

    public Expression fromCharsExpression() {
        return fromCharsExpression;
    }

    public Expression toCharsExpression() {
        return toCharsExpression;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextExpression.parameters().concat(fromCharsExpression.parameters()).concat(toCharsExpression.parameters());
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
        return contextExpression.isNullable() || fromCharsExpression.isNullable() || toCharsExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return
                contextExpression.isNullable(nullabilities) ||
                fromCharsExpression.isNullable(nullabilities) ||
                toCharsExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object contextValue = contextExpression.evaluate(values);
        if (contextValue == null) {
            return null;
        }

        Object fromCharsValue = fromCharsExpression.evaluate(values);
        if (fromCharsValue == null) {
            return null;
        }

        Object toCharsValue = toCharsExpression.evaluate(values);
        if (toCharsValue == null) {
            return null;
        }

        String contextString = StringUtil.stringify(contextValue);
        String fromCharsString = StringUtil.stringify(fromCharsValue);
        if (fromCharsString.isEmpty()) {
            return contextString;
        }
        String toCharsString = StringUtil.stringify(toCharsValue);
        int length = contextString.length();
        int toCharsLength = toCharsString.length();

        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = contextString.charAt(i);
            int charIndex = fromCharsString.indexOf(c);
            if (charIndex < 0) {
                resultBuilder.append(c);
            } else if (charIndex < toCharsLength) {
                char replacement = toCharsString.charAt(charIndex);
                resultBuilder.append(replacement);
            }
        }
        return resultBuilder.toString();
    }

    @Override
    public String automaticName() {
        return "TRANSLATE(" + contextExpression.automaticName() + ", " +
                fromCharsExpression.automaticName() + ", " +
                toCharsExpression.automaticName() + ")";
    }

}
