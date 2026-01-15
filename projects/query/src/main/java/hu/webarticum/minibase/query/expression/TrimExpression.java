package hu.webarticum.minibase.query.expression;

import java.util.Optional;

import hu.webarticum.minibase.query.util.NumberUtil;
import hu.webarticum.minibase.query.util.StringUtil;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class TrimExpression implements Expression {

    public enum TrimSpecification {
        LEADING, TRAILING, BOTH;
    }


    private final Expression inputExpression;

    private final Expression charsExpression;

    private final TrimSpecification trimSpecification;


    public TrimExpression(Expression inputExpression, Expression charsExpression, TrimSpecification trimSpecification) {
        this.inputExpression = inputExpression;
        this.charsExpression = charsExpression;
        this.trimSpecification = trimSpecification;
    }


    public Expression inputExpression() {
        return inputExpression;
    }

    public Expression charsExpression() {
        return charsExpression;
    }

    public TrimSpecification trimSpecification() {
        return trimSpecification;
    }

    @Override
    public ImmutableList<Parameter> parameters() {
        return inputExpression.parameters().concat(charsExpression.parameters());
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
        return inputExpression.isNullable() || charsExpression.isNullable();
    }

    @Override
    public boolean isNullable(ImmutableMap<Parameter, Boolean> nullabilities) {
        return inputExpression.isNullable(nullabilities) || charsExpression.isNullable(nullabilities);
    }

    @Override
    public Object evaluate(ImmutableMap<Parameter, Object> values) {
        Object inputValue = inputExpression.evaluate(values);
        if (inputValue == null) {
            return null;
        }

        Object charsValue = charsExpression.evaluate(values);
        if (charsValue == null) {
            return null;
        }

        String inputString = inputValue.toString();
        String charsString = charsValue.toString();
        int rightTrimPosition;
        if (trimSpecification != TrimSpecification.LEADING) {
            rightTrimPosition = getRightTrimPosition(inputString, charsString);
        } else {
            rightTrimPosition = inputString.length();
        }
        int leftTrimPosition;
        if (trimSpecification != TrimSpecification.TRAILING && rightTrimPosition > 0) {
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
        return "TRIM(" + trimSpecification.name() + " " + charsExpression.automaticName() +
                " FROM " + inputExpression.automaticName() + ")";
    }

}
