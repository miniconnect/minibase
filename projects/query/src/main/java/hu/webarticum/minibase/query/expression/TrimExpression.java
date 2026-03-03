package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class TrimExpression implements Expression {

    public enum TrimSpecification {
        LEADING, TRAILING, BOTH;
    }


    private static final String DEFAULT_CHARS = " ";

    private static final TrimSpecification DEFAULT_TRIM_SPECIFICATION = TrimSpecification.BOTH;


    private final Expression inputExpression;

    private final Optional<Expression> charsExpression;

    private final Optional<TrimSpecification> trimSpecification;


    public TrimExpression(
            Expression inputExpression,
            Optional<Expression> charsExpression,
            Optional<TrimSpecification> trimSpecification) {
        this.inputExpression = inputExpression;
        this.charsExpression = charsExpression;
        this.trimSpecification = trimSpecification;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public Optional<Expression> charsExpression() {
        return charsExpression;
    }

    public Optional<TrimSpecification> trimSpecification() {
        return trimSpecification;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters().concat(
                charsExpression.map(Expression::parameters).orElseGet(ImmutableList::empty));
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
        return inputExpression.isNullable() || charsExpression.map(Expression::isNullable).orElse(false);
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return inputExpression.isNullable(nullabilities) || charsExpression.map(e -> e.isNullable(nullabilities)).orElse(false);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        Object charsValue = charsExpression.orElseGet(() -> new ConstantExpression(DEFAULT_CHARS)).evaluate(values);
        if (charsValue == null) {
            return null;
        }

        TrimSpecification effectiveSpecification = trimSpecification.orElse(DEFAULT_TRIM_SPECIFICATION);

        String inputString = StringUtil.stringify(inputValue);
        String charsString = StringUtil.stringify(charsValue);
        int rightTrimPosition;
        if (effectiveSpecification != TrimSpecification.LEADING) {
            rightTrimPosition = getRightTrimPosition(inputString, charsString);
        } else {
            rightTrimPosition = inputString.length();
        }
        int leftTrimPosition;
        if (effectiveSpecification != TrimSpecification.TRAILING && rightTrimPosition > 0) {
            leftTrimPosition = getLeftTrimPosition(inputString, charsString);
        } else {
            leftTrimPosition = 0;
        }
        return inputString.substring(leftTrimPosition, rightTrimPosition);
    }

    private int getLeftTrimPosition(String inputString, String charsString) {
        int result = 0;
        int length = inputString.length();
        for (int i = 0; i < length; i++) {
            char c = inputString.charAt(i);
            if (charsString.indexOf(c) < 0) {
                break;
            }
            result = i + 1;
        }
        return result;
    }

    private int getRightTrimPosition(String inputString, String charsString) {
        int length = inputString.length();
        int result = length;
        for (int i = length - 1; i >= 0; i--) {
            char c = inputString.charAt(i);
            if (charsString.indexOf(c) < 0) {
                break;
            }
            result = i;
        }
        return result;
    }

    @Override
    public String automaticName() {
        return "TRIM(" +
                trimSpecification.map(s -> s.name() + " ").orElse("") +
                charsExpression.map(e -> e.automaticName() + " ").orElse("") +
                (trimSpecification.isPresent() || charsExpression.isPresent() ? "FROM " : "") +
                inputExpression.automaticName() + ")";
    }

}
