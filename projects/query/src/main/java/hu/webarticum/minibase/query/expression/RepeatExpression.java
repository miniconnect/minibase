package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.BitString;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class RepeatExpression implements Expression {

    private final Expression subjectOperand;

    private final Expression countOperand;


    public RepeatExpression(Expression subjectOperand, Expression countOperand) {
        this.subjectOperand = subjectOperand;
        this.countOperand = countOperand;
    }


    public Expression subjectOperand() {
        return subjectOperand;
    }

    public Expression countOperand() {
        return countOperand;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return subjectOperand.parameters().concat(countOperand.parameters());
    }

    @Override
    public Optional<Class<?>> type() {
        Class<?> subjectType = subjectOperand.type().orElse(null);
        if (subjectType == null || subjectType == ByteString.class || subjectType == BitString.class) {
            return Optional.ofNullable(subjectType);
        } else {
            return Optional.of(String.class);
        }
    }

    @Override
    public Class<?> type(ImmutableMap<Parameter, Class<?>> typeSubstitutions) {
        Class<?> subjectType = subjectOperand.type(typeSubstitutions);
        if (subjectType == ByteString.class || subjectType == BitString.class) {
            return subjectType;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isNullable() {
        return subjectOperand.isNullable() || countOperand.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilitySubstitutions) {
        return subjectOperand.isNullable(nullabilitySubstitutions) || countOperand.isNullable(nullabilitySubstitutions);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> substitutions) {
        Object subjectValue = subjectOperand.evaluate(substitutions);
        if (subjectValue == null) {
            return null;
        }

        Object countValue = countOperand.evaluate(substitutions);
        if (countValue == null) {
            return null;
        }

        int count = NumberUtil.asInt(countValue);
        if (subjectValue instanceof ByteString) {
            return operate((ByteString) subjectValue, count);
        } else if (subjectValue instanceof BitString) {
            return operate((BitString) subjectValue, count);
        } else {
            return operate(StringUtil.stringify(subjectValue), count);
        }
    }

    private String operate(String subject, int count) {
        if (subject.isEmpty()) {
            return "";
        }

        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            resultBuilder.append(subject);
        }
        return resultBuilder.toString();
    }

    private ByteString operate(ByteString subject, int count) {
        if (subject.isEmpty()) {
            return ByteString.empty();
        }

        ByteString.Builder resultBuilder = ByteString.builder();
        for (int i = 0; i < count; i++) {
            resultBuilder.append(subject);
        }
        return resultBuilder.build();
    }

    private BitString operate(BitString subject, int count) {
        if (subject.isEmpty()) {
            return BitString.empty();
        }

        BitString.Builder resultBuilder = BitString.builder();
        for (int i = 0; i < count; i++) {
            resultBuilder.append(subject);
        }
        return resultBuilder.build();
    }

    @Override
    public String automaticName(int columnIndex) {
        return "expr_repeat_col" + columnIndex;
    }

}
