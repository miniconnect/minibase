package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class InitcapExpression implements Expression {

    private final Expression operand;


    public InitcapExpression(Expression operand) {
        this.operand = operand;
    }


    public Expression operand() {
        return operand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return operand.parameters();
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
        return operand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return operand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object value = operand.evaluate(substitutions);
        if (value == null) {
            return null;
        }

        return operate(StringUtil.stringify(value));
    }

    private String operate(String text) {
        int length = text.length();
        StringBuilder resultBuilder = new StringBuilder();
        boolean wasLetter = false;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
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
        return "INITCAP(" + operand.automaticName() + ")";
    }

}
