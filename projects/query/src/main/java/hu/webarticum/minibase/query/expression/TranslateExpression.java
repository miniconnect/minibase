package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class TranslateExpression implements Expression {

    private final Expression contextOperand;

    private final Expression fromCharsOperand;

    private final Expression toCharsOperand;


    public TranslateExpression(Expression contextOperand, Expression fromCharsOperand, Expression toCharsOperand) {
        this.contextOperand = contextOperand;
        this.fromCharsOperand = fromCharsOperand;
        this.toCharsOperand = toCharsOperand;
    }


    public Expression contextOperand() {
        return contextOperand;
    }

    public Expression fromCharsOperand() {
        return fromCharsOperand;
    }

    public Expression toCharsOperand() {
        return toCharsOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return contextOperand.parameters().concat(fromCharsOperand.parameters()).concat(toCharsOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        return Optional.of(String.class);
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        return String.class;
    }

    @Override
    public boolean isNullable() {
        return contextOperand.isNullable() || fromCharsOperand.isNullable() || toCharsOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return
                contextOperand.isNullable(nullabilitySubstitutions) ||
                fromCharsOperand.isNullable(nullabilitySubstitutions) ||
                toCharsOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object contextValue = contextOperand.evaluate(substitutions);
        if (contextValue == null) {
            return null;
        }

        Object fromCharsValue = fromCharsOperand.evaluate(substitutions);
        if (fromCharsValue == null) {
            return null;
        }

        Object toCharsValue = toCharsOperand.evaluate(substitutions);
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
    public String automaticName(int columnIndex) {
        return "expr_trans_col" + columnIndex;
    }

}
